package uk.ac.cam.pd451.feature.exporter.example;
public class Main {
    public static void main(String[] args) {
        Object a = null;
        if(a != null) {
            Object b;
            b = new Object();
            b = a;

            Object c;
            c = new Object();
            c = b;

            System.out.println(c.toString());
        }
    }
}
