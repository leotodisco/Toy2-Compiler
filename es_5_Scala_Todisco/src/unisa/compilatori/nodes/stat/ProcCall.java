package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.nodes.expr.Identifier;

import java.util.ArrayList;

public class ProcCall extends Stat {

    private Identifier identifier;
    private ArrayList<ExprOP> exprs;

    public ProcCall(Identifier identifier, ArrayList<ExprOP> exprs) {
        this.identifier = identifier;
        this.exprs = exprs;

        super.add(identifier);
        exprs.forEach(exprOP -> super.add(exprOP));
    }

    public ProcCall(Identifier identifier) {
        this.identifier = identifier;
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
}
