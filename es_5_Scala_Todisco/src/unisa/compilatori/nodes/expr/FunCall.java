package unisa.compilatori.nodes.expr;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import java.util.ArrayList;

public class FunCall extends ExprOP implements Visitable {
    private Identifier identifier;
    private ArrayList<ExprOP> exprs;

    public FunCall(Identifier identifier, ArrayList<ExprOP> exprs) {
        super(Mode.FUNCALL);
        this.identifier = identifier;
        this.exprs = exprs;

        super.add(identifier);
        exprs.forEach(exprOP -> super.add(exprOP));
    }

    public FunCall(Identifier identifier) {
        super(Mode.FUNCALL);
        this.identifier = identifier;
        this.exprs = new ArrayList<>();

        super.add(identifier);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public ArrayList<ExprOP> getExprs() {
        return exprs;
    }

    public void setExprs(ArrayList<ExprOP> exprs) {
        this.exprs = exprs;
    }

    @Override
    public String toString() {
        return "FunCall{" +
                "identifier=" + identifier +
                ", exprs=" + exprs +
                '}';
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
