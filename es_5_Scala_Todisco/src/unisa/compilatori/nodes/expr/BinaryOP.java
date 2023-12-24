package unisa.compilatori.nodes.expr;


public class BinaryOP extends ExprOP {
    private String name; //indica che espressione binaria è: plus,...
    private ExprOP expr1, expr2;

    public BinaryOP(String name, ExprOP expr1, ExprOP expr2) {
        super();
        this.name = name;
        this.expr1 = expr1;
        this.expr2 = expr2;
        super.add(expr1); //aggiunge una espressione al nodo
        super.add(expr2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExprOP getExpr1() {
        return expr1;
    }

    public void setExpr1(ExprOP expr1) {
        this.expr1 = expr1;
    }

    public ExprOP getExpr2() {
        return expr2;
    }

    public void setExpr2(ExprOP expr2) {
        this.expr2 = expr2;
    }

    @Override
    public String toString() {
        return "BinaryOP{" +
                "name='" + name + '\'' +
                ", expr1=" + expr1 +
                ", expr2=" + expr2 +
                '}';
    }
}
