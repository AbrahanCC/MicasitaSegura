package service;

public class Validador {
    public static boolean noVacio(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
