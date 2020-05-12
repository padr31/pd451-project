package uk.ac.cam.pd451.dissertation.analysis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.dissertation.pipeline.Pipeline;
import uk.ac.cam.pd451.dissertation.pipeline.run.CompilerStep;
import uk.ac.cam.pd451.dissertation.pipeline.run.ExtractorStep;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AndersenPointsToAnalysisExtractorTest {

    static List<Relation>  extractedRelations = new ArrayList<>();

    @BeforeAll
    static void setup() {
        String inputDirectory = "/Users/padr/repos/pd451-project/feature-exporter/src/main/java/uk/ac/cam/pd451/dissertation/examples";
        Pipeline pipeline = new Pipeline<>(
                new CompilerStep()
        ).addStep(new ExtractorStep());
        extractedRelations = (List<Relation>) pipeline.process(new CompilerStep.CompilerPipeInput(inputDirectory,inputDirectory));
        System.out.println();
    }

    void assertContains(String name, String...elements){
        assertTrue(extractedRelations.get(extractedRelations.indexOf(new Relation(name, elements.length))).contains(new RelationEntry(elements)));
    }

    @Test
    void testActuralArgRelation() {
        assertContains("ACTUALARG", "45","1","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).beee@1406");
        assertContains("ACTUALARG", "57","1","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f@1661");
        assertContains("ACTUALARG", "39","2","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).f1@1322");
        assertContains("ACTUALARG", "29","1","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f1@575");
        assertContains("ACTUALARG", "39","1","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).f@1172");
    }

    @Test
    void testActualReturnRelation() {
        assertContains("ACTUALRETURN", "59","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.name");
        assertContains("ACTUALRETURN", "60","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.name");
        assertContains("ACTUALRETURN", "61","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.name");
    }

    @Test
    void testAllocRelation() {
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).featureWorld@1103","34","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).f1@1322","38","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).f@1172","36","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).f@1172","37","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).a@1440","43","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).beee@1406","41","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.features", "14", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.FeatureWorld(java.lang.String)");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).a@1440","44","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B).theB@106","6","uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B)");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).temp@609","19","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).f@1172","35","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).a@1440","42","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("ALLOC", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.name","49","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
    }

    @Test
    void testFormalArgRelation() {
        assertContains("FORMALARG","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])","1","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).args@1074");
        assertContains("FORMALARG","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","1","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f@2051");
        assertContains("FORMALARG","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","2","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f2@587");
        assertContains("FORMALARG","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.FeatureWorld(java.lang.String)","1","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.FeatureWorld(java.lang.String).name@223");
        assertContains("FORMALARG","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","1","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f@1661");
        assertContains("FORMALARG","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","1","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f1@575");
        assertContains("FORMALARG","uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B)","1","uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B).bro@89");
    }

    @Test
    void testFormalReturnRelation() {
        assertContains("FORMALRETURN", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getName()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.name");
        assertContains("FORMALRETURN", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getString()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.name");
    }

    @Test
    void testHeapTypeRelation() {
        assertContains("HEAPTYPE", "14","java.util.ArrayList<uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature>");
        assertContains("HEAPTYPE", "44","uk.ac.cam.pd451.dissertation.examples.A");
        assertContains("HEAPTYPE", "34","uk.ac.cam.pd451.dissertation.examples.FeatureWorld");
        assertContains("HEAPTYPE", "37","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature");
        assertContains("HEAPTYPE", "38","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature");
        assertContains("HEAPTYPE", "43","null");
        assertContains("HEAPTYPE", "41","uk.ac.cam.pd451.dissertation.examples.B");
        assertContains("HEAPTYPE", "36","null");
        assertContains("HEAPTYPE", "6","uk.ac.cam.pd451.dissertation.examples.B");
        assertContains("HEAPTYPE", "19","null");
        assertContains("HEAPTYPE", "35","null");
        assertContains("HEAPTYPE", "42","null");
        assertContains("HEAPTYPE", "49","null");
    }

    @Test
    void testLoadRelation() {
        assertContains("LOAD", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.name","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f@2051","name");
        assertContains("LOAD", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.name","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.this","getName");
    }

    @Test
    void testLookupRelation() {
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature","Feature()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature()");
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.FeatureWorld","changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.FeatureWorld","main(java.lang.String[])","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature","getName()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getName()");
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature","Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.FeatureWorld","FeatureWorld(java.lang.String)","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.FeatureWorld(java.lang.String)");
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature","getString()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getString()");
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature","changeName(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature","resetName()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.resetName()");
        assertContains("LOOKUP","uk.ac.cam.pd451.dissertation.examples.A","doSomething(uk.ac.cam.pd451.dissertation.examples.B)","uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B)");
    }

    @Test
    void testMoveRelation() {
        assertContains("MOVE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f2@587","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).temp@609");
        assertContains("MOVE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).temp@609","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f1@575");
        assertContains("MOVE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f1@575","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f2@587");
        assertContains("MOVE", "uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B).theB@106","uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B).bro@89");
    }

    @Test
    void testReachableRelation() {
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.B.B()");
        assertContains("REACHABLE", "java.lang.Object.Object()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.A.A()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.B.B()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature()");
        assertContains("REACHABLE", "java.util.ArrayList.ArrayList()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.FeatureWorld(java.lang.String)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.resetName()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getName()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getString()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.A.A()");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B)");
        assertContains("REACHABLE", "uk.ac.cam.pd451.dissertation.examples.B.B()");
    }

    @Test
    void testStoreRelation() {
        assertContains("STORE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.this","name","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.FeatureWorld(java.lang.String).name@223");
        assertContains("STORE", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f1@575","name","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).newName@701");
    }

    @Test
    void testThisVarRelation() {
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.FeatureWorld(java.lang.String)","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.FeatureWorld(java.lang.String)-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.resetName()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.resetName()-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getName()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getName()-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getString()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.getString()-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature()","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature()-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.changeName(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)-this");
        assertContains("THISVAR", "uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B)","uk.ac.cam.pd451.dissertation.examples.A.doSomething(uk.ac.cam.pd451.dissertation.examples.B)-this");

    }

    @Test
    void testVCallRelation() {
        assertContains("VCALL", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.this","getName()","61","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("VCALL", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f2@587","changeName(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","29","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("VCALL", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).a@1440","doSomething(uk.ac.cam.pd451.dissertation.examples.B)","45","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
        assertContains("VCALL", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.this","getName()","59","uk.ac.cam.pd451.dissertation.examples.FeatureWorld$Feature.Feature(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("VCALL", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature).f2@587","resetName()","27","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)");
        assertContains("VCALL", "uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[]).featureWorld@1103","changeFeatures(uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature,uk.ac.cam.pd451.dissertation.examples.FeatureWorld.Feature)","39","uk.ac.cam.pd451.dissertation.examples.FeatureWorld.main(java.lang.String[])");
    }

    @Test
    void testNumberOfExtractions() {
        for(Relation rel : extractedRelations) {
            System.out.println(rel.getName() + ": " + rel.getSize());
        }
    }
}