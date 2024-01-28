package unisa.compilatori.nodes.expr;

import unisa.compilatori.Token;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class Identifier extends ExprOP implements Visitable {
    private String lessema;



    public Identifier(Object tkn, Mode mode) {
        super(mode);
        var token = (Token) tkn;
        this.lessema = token.getAttribute();



        super.add(new DefaultMutableTreeNode(lessema));
    }

    public String getLessema() {return this.lessema;}

    public void setLessema(String lessema) {
        this.lessema = lessema;
    }


    @Override
    public String toString() {
        return "Identifier{" +
                "lessema='" + lessema + '\'' +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
