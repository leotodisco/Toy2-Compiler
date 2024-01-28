package unisa.compilatori.semantic.symboltable;

public class VarFieldType implements FieldType {
    private String type;

    public VarFieldType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }
}
