package uk.ac.cam.pd451.dissertation.examples;

import java.util.ArrayList;
import java.util.List;

public class FeatureWorld {

    private String name;
    private List<Feature> features;


    public FeatureWorld(String name) {
        this.name = name;
        features = new ArrayList<>(); // ALLOC(features, line-number, ArrayList()) //HEAPTYPE(line-number, ArrayList<>())
    }

    //FORMALARG(changeFeatures, 1, f1), FORMALARG(changeFeatures, 2, f2), LOOKUP(FeatureWorld, changeFeatures(f1, f2), FeatureWorld-changeFeatures())
    public void changeFeatures(Feature f1, Feature f2) {
        Feature temp;
        temp = f1;
        f1 = f2; //MOVE(f1, f2)
        f2 = temp;

        String newName = "newName";
        f1.name = newName; //STORE(f1, name, newName)

        f2.resetName(); //VCALL(f2, resetName(), line-number, FeatureWorld-changeFeatures())
        //ACTUALARG(changeName-1, 1, f1)
        f2.changeName(f1); //VCALL(f2, changeName(), line-number, FeatureWorld-changeFeatures())
    }

    //REACHABLE(main)
    public static void main(String[] args) {
        FeatureWorld featureWorld = new FeatureWorld("features"); //ALLOC
        Feature f;
        f = null; //ALLOC
        f = new Feature(); //ALLOC(f, line-number, FeatureWorld-main) //HEAPTYPE(line-number, Feature())
        Feature f1 = new Feature(); // ALLOC
        featureWorld.changeFeatures(f, f1);

        B beee = new B(); //ALLOC
        A a;
        a = null; //ALLOC
        a = new A(); //ALLOC
        a.doSomething(beee);
    }

    static class Feature {
        private String name;

        public Feature() {

        }

        public Feature(Feature f) {
            //ACTUALARG(changeName-2, 1, f)
            changeName(f);
            this.name = f.name; //THISVAR(Feature-Feature(), Feature-Feature()-this)
            name = this.getName(); //ACTUALRETURN(line-number, x)
            this.name = getName();
            this.name = this.getName();
        }

        //FORMALARG(changeName, 1, f)
        public void changeName(Feature f) {
            name = f.name; //LOAD(name, f, name)
            this.name = f.name; //THISVAR(Feature-changeName(), Feature-changeName()-this).
        }

        public void resetName() {
            this.name = "";
        }

        public String getName() {
            return name; //FORMALRETURN(getName, name)
        }

        public String getString() {
            this.name = "name";
            if(false) {
                return "false";
            } else {
                return this.name; //FORMALRETURN(getString, name)
            }
        }
    }
}
