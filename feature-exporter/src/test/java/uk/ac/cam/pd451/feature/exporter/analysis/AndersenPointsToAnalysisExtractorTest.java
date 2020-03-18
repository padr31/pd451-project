package uk.ac.cam.pd451.feature.exporter.analysis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.feature.exporter.pipeline.run.CompilerStep;
import uk.ac.cam.pd451.feature.exporter.pipeline.run.ExtractorStep;
import uk.ac.cam.pd451.feature.exporter.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AndersenPointsToAnalysisExtractorTest {

    static List<Relation>  extractedRelations = new ArrayList<>();

    @BeforeAll
    static void setup() {
        String inputDirectory = "/Users/padr/repos/pd451-project/feature-exporter/src/main/java/uk/ac/cam/pd451/feature/exporter/examples";
        Pipeline pipeline = new Pipeline<>(
                new CompilerStep()
        ).addStep(new ExtractorStep());
        extractedRelations = (List<Relation>) pipeline.process(new CompilerStep.CompilerPipeInput(inputDirectory,inputDirectory + "/target"));
        System.out.println();
    }

    void assertContains(String name, String...elements){
        assertTrue(extractedRelations.get(extractedRelations.indexOf(new Relation(name, elements.length))).contains(new RelationEntry(elements)));
    }

    @Test
    void testActuralArgRelation() {
        assertContains("ACTUALARG", "45","1","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).beee@1410");
        assertContains("ACTUALARG", "57","1","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f@1665");
        assertContains("ACTUALARG", "39","2","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).f1@1326");
        assertContains("ACTUALARG", "29","1","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f1@579");
        assertContains("ACTUALARG", "39","1","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).f@1176");
    }

    @Test
    void testActualReturnRelation() {
        assertContains("ACTUALRETURN", "59","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.name");
        assertContains("ACTUALRETURN", "60","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.name");
        assertContains("ACTUALRETURN", "61","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.name");
    }

    @Test
    void testAllocRelation() {
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).featureWorld@1107","34","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).f1@1326","38","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).f@1176","36","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).f@1176","37","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).a@1444","43","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).beee@1410","41","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.features", "14", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.FeatureWorld(java.lang.String)");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).a@1444","44","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B).theB@109","5","uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B)");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).temp@613","19","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).f@1176","35","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).a@1444","42","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.name","49","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
    }

    @Test
    void testFormalArgRelation() {
        assertContains("FORMALARG","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])","1","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).args@1078");
        assertContains("FORMALARG","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","1","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f@2055");
        assertContains("FORMALARG","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","2","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f2@591");
        assertContains("FORMALARG","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.FeatureWorld(java.lang.String)","1","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.FeatureWorld(java.lang.String).name@227");
        assertContains("FORMALARG","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","1","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f@1665");
        assertContains("FORMALARG","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","1","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f1@579");
        assertContains("FORMALARG","uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B)","1","uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B).bro@92");
    }

    @Test
    void testFormalReturnRelation() {
        assertContains("FORMALRETURN", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getName()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.name");
        assertContains("FORMALRETURN", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getString()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.name");
    }

    @Test
    void testHeapTypeRelation() {
        assertContains("HEAPTYPE", "14","java.util.ArrayList<uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature>");
        assertContains("HEAPTYPE", "44","uk.ac.cam.pd451.feature.exporter.examples.A");
        assertContains("HEAPTYPE", "34","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld");
        assertContains("HEAPTYPE", "37","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature");
        assertContains("HEAPTYPE", "38","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature");
        assertContains("HEAPTYPE", "43","null");
        assertContains("HEAPTYPE", "41","uk.ac.cam.pd451.feature.exporter.examples.B");
        assertContains("HEAPTYPE", "36","null");
        assertContains("HEAPTYPE", "5","uk.ac.cam.pd451.feature.exporter.examples.B");
        assertContains("HEAPTYPE", "19","null");
        assertContains("HEAPTYPE", "35","null");
        assertContains("HEAPTYPE", "42","null");
        assertContains("HEAPTYPE", "49","null");
    }

    @Test
    void testLoadRelation() {
        assertContains("LOAD", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.name","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f@2055","name");
        assertContains("LOAD", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.name","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.this","getName");
    }

    @Test
    void testLookupRelation() {
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature","Feature()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature()");
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld","changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld","main(java.lang.String[])","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature","getName()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getName()");
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature","Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld","FeatureWorld(java.lang.String)","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.FeatureWorld(java.lang.String)");
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature","getString()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getString()");
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature","changeName(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature","resetName()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.resetName()");
        assertContains("LOOKUP","uk.ac.cam.pd451.feature.exporter.examples.A","doSomething(uk.ac.cam.pd451.feature.exporter.examples.B)","uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B)");
    }

    @Test
    void testMoveRelation() {
        assertContains("MOVE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f2@591","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).temp@613");
        assertContains("MOVE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).temp@613","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f1@579");
        assertContains("MOVE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f1@579","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f2@591");
        assertContains("MOVE", "uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B).theB@109","uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B).bro@92");
    }

    @Test
    void testReachableRelation() {
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.B.B()");
        assertContains("REACHABLE", "java.lang.Object.Object()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.A.A()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.B.B()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature()");
        assertContains("REACHABLE", "java.util.ArrayList.ArrayList()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.FeatureWorld(java.lang.String)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.resetName()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getName()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getString()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.A.A()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.feature.exporter.examples.B.B()");
    }

    @Test
    void testStoreRelation() {
        assertContains("STORE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.this","name","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.FeatureWorld(java.lang.String).name@227");
        assertContains("STORE", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f1@579","name","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).newName@705");
    }

    @Test
    void testThisVarRelation() {
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.FeatureWorld(java.lang.String)","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.FeatureWorld(java.lang.String)-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.resetName()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.resetName()-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getName()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getName()-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getString()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.getString()-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature()","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature()-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B)","uk.ac.cam.pd451.feature.exporter.examples.A.doSomething(uk.ac.cam.pd451.feature.exporter.examples.B)-this");

    }

    @Test
    void testVCallRelation() {
        assertContains("VCALL", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.this","getName()","61","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("VCALL", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f2@591","changeName(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","29","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("VCALL", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).a@1444","doSomething(uk.ac.cam.pd451.feature.exporter.examples.B)","45","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("VCALL", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.this","getName()","59","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("VCALL", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature).f2@591","resetName()","27","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)");
        assertContains("VCALL", "uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[]).featureWorld@1107","changeFeatures(uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature,uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.Feature)","39","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.main(java.lang.String[])");
    }

    @Test
    void testNumberOfExtractions() {
        for(Relation rel : extractedRelations) {
            System.out.println(rel.getName() + ": " + rel.getSize());
        }
    }
}