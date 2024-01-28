package unisa.compilatori.utils;

public class Exceptions {
    public static class MultipleDeclaration extends RuntimeException {
        public MultipleDeclaration(String symbol) {
            super("Il simbolo '"+symbol+"' è dichiarato più di una volta nello scope");
        }
    }

    public static class LackOfMain extends RuntimeException {
        public LackOfMain() {
            super("Manca la procedura main");
        }
    }

    public static class NoDeclarationError extends RuntimeException {
        public NoDeclarationError(String id) {
            super("Dichiarazione assente per id: " + id);
        }
    }
    public static class NoReturnError extends RuntimeException {
        /**
         *
         * @param id id della funzione
         */
        public NoReturnError(String id) {
            super("Non hai un returrn nella funzione: " + id);
        }
    }

    public static class TypesMismatch extends RuntimeException {
        /**
         *
         * @param id
         * @param tipo1
         * @param tipo2
         */
        public TypesMismatch(String id, String tipo1, String tipo2) {
            super("Type Mismatch per id: " + id + "\nL'hai dichiarato con tipo " + tipo1 +" ma gli assegni " + tipo2);
        }

        public TypesMismatch(String tipo1, String tipo2) {
            super("Type Mismatch per id: " + "\nL'hai dichiarato con tipo " + tipo1 +" ma gli assegni " + tipo2);
        }
    }

    public static class InvalidOperation extends RuntimeException {
        public InvalidOperation(String op, String tipo1, String tipo2) {
            super("Operazione binaria " + op +   " non consentita tra il tipo : " + tipo1 + "e il tipo : " + tipo2);
        }

        public InvalidOperation(String op, String tipo) {
            super("Operazione unaria " + op + " non consentita con il tipo : " + tipo);
        }

    }

    public static class InvalidCondition extends RuntimeException {
        public InvalidCondition(String tipoCondizione) {
            super("Condizione non valida, tipo previsto: Boolean, tipo ottenuto: " + tipoCondizione);
        }
    }

    public static class SemanticError extends RuntimeException {
        public SemanticError() {
            super("Errore semantico: hai dichiarato una procedura con un return");
        }
    }

    public static class EmptyBodyError extends RuntimeException {
        public EmptyBodyError() {
            super("Errore semantico: hai un body senza statements.");
        }
    }

}
