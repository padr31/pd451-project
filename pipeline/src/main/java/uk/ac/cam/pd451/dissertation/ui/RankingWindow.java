package uk.ac.cam.pd451.dissertation.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import uk.ac.cam.pd451.dissertation.datalog.Predicate;
import uk.ac.cam.pd451.dissertation.datalog.ProvenanceGraph;
import uk.ac.cam.pd451.dissertation.inference.BayessianGibbsSamplingInference;
import uk.ac.cam.pd451.dissertation.inference.Event;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;
import uk.ac.cam.pd451.dissertation.pipeline.Pipeline;
import uk.ac.cam.pd451.dissertation.pipeline.Step;
import uk.ac.cam.pd451.dissertation.pipeline.io.InspectedPredicate;
import uk.ac.cam.pd451.dissertation.pipeline.io.RankingStatistics;
import uk.ac.cam.pd451.dissertation.pipeline.optimisations.CycleEliminationStep;
import uk.ac.cam.pd451.dissertation.pipeline.optimisations.FullNarrowingStep;
import uk.ac.cam.pd451.dissertation.pipeline.optimisations.ProvenancePruningStep;
import uk.ac.cam.pd451.dissertation.pipeline.optimisations.SingularChainCompressionStep;
import uk.ac.cam.pd451.dissertation.pipeline.run.*;
import uk.ac.cam.pd451.dissertation.utils.Timer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class RankingWindow implements Step<ProvenanceGraph, RankingStatistics> {
    private JTextField textSource;
    private JButton btnStartAnalysis;
    private JTextArea textLogs;
    private JList<String> listAlarms;
    private JTextField txtTopAlarm;
    private JRadioButton btnFalsePos;
    private JRadioButton btnTruePos;
    private JButton btnFeedback;
    private JPanel panelMain;
    private JPanel panelFeedback;
    private JButton btnEnd;

    private DefaultListModel<Set<String>> model;

    private Thread rankingThread = new Thread();
    private Object rankingThreadMonitor = new Object();

    public RankingWindow() {
        PrintStream printStream = new PrintStream(new CustomOutputStream(this.textLogs));
        System.setOut(printStream);
        System.setErr(printStream);
    }

    public void initWindow() {
        btnStartAnalysis.addActionListener(actionEvent -> performRanking());
    }

    private void performRanking() {
        String inputDirectory = textSource.getText();
        if (!(new File(inputDirectory).exists())) {
            System.out.println("Source folder does not exist: " + inputDirectory);
            return;
        }

        Pipeline optimisationsPipeline = new Pipeline(
                new CycleEliminationStep())
                .addStep(new ProvenancePruningStep())
                .addStep(new SingularChainCompressionStep())
                .addStep(new FullNarrowingStep());

        Pipeline pipeline = new Pipeline<>(
                new CompilerStep())
                .addStep(new ExtractorStep())
                .addStep(new DatalogStep())
                .addStep(new ProvenanceImportStep())
                .addStep(optimisationsPipeline)
                .addStep(new NetworkCreationStep())
                .addStep(this) //the ranking step is performed by this window
                .addStep(new RankingProcessorStep());

        rankingThread = new Thread(() -> {
            pipeline.process(new CompilerStep.CompilerPipeInput(inputDirectory, inputDirectory));
        });
        rankingThread.start();
    }

    @Override
    public RankingStatistics process(ProvenanceGraph g) throws PipeException {
        List<InspectedPredicate> inspectedPredicates = new ArrayList<>();
        List<Map<Predicate, Double>> overallRanks = new ArrayList<>();

        //collect varPointsTo variables
        System.out.println("Collecting nullPointer variables");
        Map<Predicate, Variable> pointsToSet = g.getPredicateToNode()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getName().equals("nullPointer"))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getVariable()));

        System.out.println("Initialising inference algorithm");

        BayessianGibbsSamplingInference i = new BayessianGibbsSamplingInference();
        i.setModel(g.getBayesianNetwork());

        Map<Predicate, Event> evidence = new HashMap<>();
        List<Boolean> trueFalse = new ArrayList<>();
        Map<Predicate, Double> alarmProbabilities = new HashMap<>();

        // insert all alarms with their prior probabilities
        System.out.println("Inferring prior probabilities");

        Timer t = new Timer();
        Map<Event, Double> probs = i.infer(pointsToSet.values().stream().map(v -> new Event(v, 1)).collect(Collectors.toList()));
        t.printLastTimeSegment("Inferred in: ");

        for (Map.Entry<Predicate, Variable> pointsToVar : pointsToSet.entrySet()) {
            alarmProbabilities.put(pointsToVar.getKey(), probs.get(new Event(pointsToVar.getValue(), 1)));
        }

        DefaultListModel<String> displayList = new DefaultListModel<>();
        alarmProbabilities.forEach((key, value) -> displayList.addElement("Prob: " + value + ", alarm: " + key.getTerms()));
        listAlarms.setModel(displayList);

        // re-rank based on user feedback (y/n)
        int rank = 0;
        while (alarmProbabilities.size() != 0) {
            overallRanks.add(new HashMap<>(alarmProbabilities));

            // pick alarm with largest probability and present for inspection
            Predicate topAlarm = alarmProbabilities.entrySet().stream().min((a, b) -> b.getValue() - a.getValue() < 0 ? -1 : 1).get().getKey();
            System.out.println("Is this alarm a true positive? Set the appropriate radio button and hit the feedback button.");
            this.txtTopAlarm.setText(topAlarm.getTerms());

            int finalRank = rank;
            ActionListener listener = actionEvent -> {
                if (btnTruePos.isSelected()) {
                    // true positive
                    inspectedPredicates.add(new InspectedPredicate(topAlarm, finalRank, alarmProbabilities.get(topAlarm), true));
                    Event e = new Event(pointsToSet.get(topAlarm), 1);
                    evidence.put(topAlarm, e);
                    trueFalse.add(true);
                    i.addEvidence(e);
                } else {
                    // false positive
                    inspectedPredicates.add(new InspectedPredicate(topAlarm, finalRank, alarmProbabilities.get(topAlarm), false));
                    Event e = new Event(pointsToSet.get(topAlarm), 0);
                    evidence.put(topAlarm, e);
                    trueFalse.add(false);
                    i.addEvidence(e);
                }
                synchronized (rankingThreadMonitor) {
                    rankingThreadMonitor.notifyAll();
                }
            };
            this.btnFeedback.addActionListener(listener);
            try {
                synchronized (rankingThreadMonitor) {
                    rankingThreadMonitor.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.btnFeedback.removeActionListener(listener);

            // remove the inspected alarm
            alarmProbabilities.remove(topAlarm);

            //re-calculate probabilities of remaining alarms by inference
            System.out.println("Recalculating...");

            Map<Event, Double> newProbs = i.infer(alarmProbabilities.keySet().stream().map(p -> new Event(pointsToSet.get(p), 1)).collect(Collectors.toList()));
            alarmProbabilities.replaceAll((alarm, prob) -> newProbs.get(new Event(pointsToSet.get(alarm), 1)));

            DefaultListModel<String> l2 = new DefaultListModel<>();
            alarmProbabilities.forEach((key, value) -> l2.addElement("Prob: " + value + ", alarm: " + key.getTerms()));
            listAlarms.setModel(l2);

            rank++;
        }
        System.out.println("Ranking done");
        System.out.println("Alarms: " + trueFalse.size());
        System.out.println("Last true positive: " + trueFalse.lastIndexOf(true));
        System.out.println("True positives in first half: " + trueFalse.subList(0, trueFalse.size() / 2).stream().filter(b -> b).count());
        System.out.println("True positives in second half: " + trueFalse.subList(trueFalse.size() / 2, trueFalse.size()).stream().filter(b -> b).count());

        return new RankingStatistics(inspectedPredicates, overallRanks);
    }

    public static void main(String[] args) {
        RankingWindow ranker = new RankingWindow();
        JFrame rankingWindow = new JFrame("RankingWindow");
        rankingWindow.setSize(500, 1000);
        rankingWindow.setContentPane(ranker.panelMain);
        rankingWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        rankingWindow.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        rankingWindow.pack();
        rankingWindow.setVisible(true);
        ranker.initWindow();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new GridLayoutManager(9, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.setMinimumSize(new Dimension(500, 400));
        panelMain.setPreferredSize(new Dimension(500, 400));
        panelMain.setRequestFocusEnabled(true);
        textSource = new JTextField();
        textSource.setText("");
        textSource.setToolTipText("Java source folder location");
        panelMain.add(textSource, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        btnStartAnalysis = new JButton();
        btnStartAnalysis.setText("Start Null Pointer Analysis");
        panelMain.add(btnStartAnalysis, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Logs");
        panelMain.add(label1, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Ranked alarms with null pointer likelihoods");
        panelMain.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Top alarm displayed below for inspection:");
        panelMain.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelFeedback = new JPanel();
        panelFeedback.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(panelFeedback, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnFalsePos = new JRadioButton();
        btnFalsePos.setSelected(true);
        btnFalsePos.setText("False Poistive");
        panelFeedback.add(btnFalsePos, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnTruePos = new JRadioButton();
        btnTruePos.setText("True Positive");
        panelFeedback.add(btnTruePos, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnFeedback = new JButton();
        btnFeedback.setText("Feedback");
        panelFeedback.add(btnFeedback, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEnd = new JButton();
        btnEnd.setText("End Ranking");
        panelFeedback.add(btnEnd, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelMain.add(scrollPane1, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textLogs = new JTextArea();
        textLogs.setEditable(false);
        textLogs.setLineWrap(false);
        textLogs.setToolTipText("Logs");
        scrollPane1.setViewportView(textLogs);
        txtTopAlarm = new JTextField();
        txtTopAlarm.setEditable(false);
        panelMain.add(txtTopAlarm, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panelMain.add(scrollPane2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        listAlarms = new JList();
        scrollPane2.setViewportView(listAlarms);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(btnFalsePos);
        buttonGroup.add(btnTruePos);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }

    static class CustomOutputStream extends OutputStream {
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            // redirects data to the text area
            textArea.append(String.valueOf((char) b));
            // keeps the textArea up to date
            //textArea.update(textArea.getGraphics());
        }
    }
}
//Users/padr/repos/pd451-project/pipeline/src/main/java/uk/ac/cam/pd451/dissertation/examples