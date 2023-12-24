package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.Body;
import unisa.compilatori.nodes.expr.ExprOP;

public class WhileStat extends Stat{

    private ExprOP expr;
    private Body body;

    public WhileStat(ExprOP expr, Body body) {
        this.expr = expr;
        this.body = body;

        super.add(body);
        super.add(expr);
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
    public String toString() {
        return "WhileStat{" +
                "expr=" + expr +
                ", body=" + body +
                '}';
    }
}
