package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.nodes.expr.Identifier;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import java.util.ArrayList;

public class ProcCall extends Stat implements Visitable {

    private Identifier identifier;
    private ArrayList<ExprOP> exprs;

    public ProcCall(Identifier identifier, ArrayList<ExprOP> exprs, Mode mode) {
        this.identifier = identifier;
        this.exprs = exprs;
        super.setTipo(mode);

        super.add(identifier);
        exprs.forEach(exprOP -> super.add(exprOP));
    }

    public ProcCall(Identifier identifier, Mode mode) {
        this.identifier = identifier;
        this.exprs = new ArrayList<>();
        super.add(identifier);
        super.setTipo(mode);
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
        return "ProcCall{" +
                "identifier=" + identifier + ", " +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws RuntimeException {
            return visitor.visit(this);
    }
}
