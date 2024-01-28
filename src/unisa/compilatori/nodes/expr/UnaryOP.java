package unisa.compilatori.nodes.expr;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class UnaryOP extends ExprOP implements Visitable {
    private String simbolo; //nome può essere NOT o MINUS (UMINUS)
    private ExprOP expr;
    private String returnType;

    public UnaryOP(String simbolo, ExprOP expr) {
        super(Mode.UNARY);
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

    public String getReturnType() {
        return this.returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "UnaryOP{" +
                "simbolo='" + simbolo + '\'' +
                ", expr=" + expr +
                '}';
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
