package unisa.compilatori.utils;

public class Exceptions {
    public static class MultipleDeclaration extends Exception {
        public MultipleDeclaration(String symbol) {
            super("Il simbolo '"+symbol+"' è dichiarato più di una volta nello scope");
        }
    }

    public static class LackOfMain extends Exception {
        public LackOfMain() {
            super("Manca la procedura main");
        }
    }

    public static class NoDeclarationError extends Exception {
        public NoDeclarationError(String id) {
            super("Dichiarazione assente per id: " + id);
        }
    }


}
