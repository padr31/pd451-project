package uk.ac.cam.pd451.dissertation.example;
public class Main {
    public static void main(String[] args) {
        Object a = null;
        a = new Object();
        if(a != null) {
            Object b = new Object();
            b = a;

            Object c = new Object();
            c = b;
        }
    }
}
