package unisa.compilatori.nodes.expr;

import javax.swing.tree.DefaultMutableTreeNode;

public class UnaryOP extends ExprOP {
    private String simbolo; //nome può essere NOT o MINUS (UMINUS)
    private ExprOP expr;

    public UnaryOP(String simbolo, ExprOP expr) {
        super();
        this.simbolo = simbolo;
        this.expr = expr;

        super.add(expr);
        super.add(new DefaultMutableTreeNode(simbolo));
    }

    public ExprOP getExpr() {
        return expr;
    }

    public void setExpr(ExprOP expr) {
        this.expr = expr;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    @Override
    public String toString() {
        return "UnaryOP{" +
                "simbolo='" + simbolo + '\'' +
                ", expr=" + expr +
                '}';
    }
}
