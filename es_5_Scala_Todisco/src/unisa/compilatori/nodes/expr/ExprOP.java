package unisa.compilatori.nodes.expr;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class ExprOP extends DefaultMutableTreeNode implements Visitable {


    public ExprOP() {
        super();
    }

    @Override
    public String toString() {
        return "Expr{}";
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {

        return visitor.visit(this);
    }
}
