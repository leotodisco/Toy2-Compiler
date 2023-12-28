package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.Body;
import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;
import unisa.compilatori.semantic.symboltable.*;

public class WhileStat extends Stat implements Visitable {

    private ExprOP expr;
    private Body body;

    private SymbolTable table;

    public WhileStat(ExprOP expr, Body body) {
        this.expr = expr;
        this.body = body;

        super.add(body);
        super.add(expr);
    }

    public void setTable(SymbolTable table) {
        this.table = table;
    }

    public SymbolTable getTable() {
        return table;
    }

    public ExprOP getExpr() {
        return expr;
    }

    public void setExpr(ExprOP expr) {
        this.expr = expr;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        try {
            return visitor.visit(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "WhileStat{" +
                "expr=" + expr +
                ", body=" + body +
                '}';
    }
}
