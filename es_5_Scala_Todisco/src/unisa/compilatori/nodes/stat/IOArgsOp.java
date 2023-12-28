package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.nodes.expr.IOArgsExpr;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import java.util.ArrayList;

public class IOArgsOp extends Stat implements Visitable {
    private ArrayList<ExprOP> listaEspressioni;
    private ArrayList<IOArgsExpr> listaIOArgsExpr;

    public IOArgsOp() {
        this.listaEspressioni = new ArrayList<>();
        this.listaIOArgsExpr = new ArrayList<>();
    }

    public ArrayList<IOArgsExpr> getListaIOArgsExpr() {
        return this.listaIOArgsExpr;
    }


    public void addExpr(ExprOP ex) {
        listaEspressioni.add(ex);
        super.add(ex);
    }

    public void addIOArgsExpr(IOArgsExpr ex) {
        listaIOArgsExpr.add(ex);
        super.add(ex);
    }

    public IOArgsOp(ArrayList<ExprOP> listaEspressioni, ArrayList<IOArgsExpr> listaIOArgsExpr) {
        this.listaEspressioni = listaEspressioni;
        this.listaIOArgsExpr = listaIOArgsExpr;

        this.listaEspressioni.forEach(exprOP -> super.add(exprOP));
        this.listaIOArgsExpr.forEach(ioArgsExpr -> super.add(ioArgsExpr));
    }

    @Override
    public String toString() {
        return "IOArgsOp{" +
                "listaEspressioni=" + listaEspressioni +
                ", listaIOArgsExpr=" + listaIOArgsExpr +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
