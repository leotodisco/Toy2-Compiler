package unisa.compilatori;

public class Token {
    public static final int EOF=0;
    public static final int VAR = 1;
    public static final int COLON = 2;
    public static final int ASSIGN = 3;
    public static final int SEMI = 4;
    public static final int ID = 5;
    public static final int COMMA = 6;
    public static final int NOT = 7;
    public static final int OR = 8;
    public static final int AND = 9;
    public static final int GE = 10;
    public static final int GT = 11;
    public static final int LE = 12;
    public static final int LT = 13;
    public static final int INTEGER = 14;
    public static final int STRING = 15;
    public static final int BOOLEAN = 16;
    public static final int NE = 17;
    public static final int EQ = 18;
    public static final int RETURN = 19;
    public static final int FUNCTION = 20;
    public static final int DIV = 21;
    public static final int TIMES = 22;
    public static final int MINUS = 23;
    public static final int TYPERETURN = 24;
    public static final int PLUS = 25;
    public static final int ENDFUNCTION = 26;
    public static final int ENDWHILE = 27;
    public static final int DO = 28;
    public static final int LPAR = 29;
    public static final int RPAR = 30;
    public static final int WHILE = 31;
    public static final int ELIF = 32;
    public static final int ENDIF = 33;
    public static final int PROCEDURE = 34;
    public static final int ENDPROCEDURE = 35;
    public static final int ELSE = 36;
    public static final int OUT = 37;
    public static final int WRITE = 38;
    public static final int WRITERETURN = 39;
    public static final int THEN = 40;
    public static final int DOLLARSIGN = 41;
    public static final int READ = 42;
    public static final int IF = 43;
    public static final int TRUE = 44;
    public static final int FALSE = 45;
    public static final int REAL = 46;
    public static final int STRINGCONST = 47;
    public static final int INTEGERCONST = 48;
    public static final int REALCONST = 49;

    private String name;     // questo è un identificativo di token: potrebbe anche essere un intero
    private String attribute;

    public Token(String name, String attribute){
        this.name = name;
        this.attribute = attribute;
    }

    public Token(String name){
        this.name = name;
        this.attribute = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String toString(){
        return attribute==null? name : "("+name+", \""+attribute+"\")";

    }

}

