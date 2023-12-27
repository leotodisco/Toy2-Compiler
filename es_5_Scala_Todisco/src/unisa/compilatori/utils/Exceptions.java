package unisa.compilatori.utils;

public class Exceptions {
    public static class MultipleDeclaration extends Exception {
        public MultipleDeclaration(String symbol) {
            super("Il simbolo '"+symbol+"' è dichiarato più di una volta nello scope");
        }
    }

}
