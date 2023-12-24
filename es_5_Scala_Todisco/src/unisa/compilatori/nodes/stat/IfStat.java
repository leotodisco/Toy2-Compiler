package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.Body;
import unisa.compilatori.nodes.expr.ExprOP;

import java.util.ArrayList;

public class IfStat extends Stat{
    private ExprOP expr;
    private Body body;
    private ElseOP elseOP;

    private ArrayList<ElseIfOP> elseIfOPList;

    public IfStat(ExprOP expr, Body body, ElseOP elseOP, ArrayList<ElseIfOP> elseIfOPList) {
        this.expr = expr;
        this.body = body;
        this.elseOP = elseOP;
        this.elseIfOPList = elseIfOPList;

        super.add(expr);
        super.add(body);
        super.add(elseOP);

        elseIfOPList.forEach(elseIfOP -> super.add(elseIfOP));
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

    public ElseOP getElseOP() {
        return elseOP;
    }

    public void setElseOP(ElseOP elseOP) {
        this.elseOP = elseOP;
    }

    public ArrayList<ElseIfOP> getElseIfOPList() {
        return elseIfOPList;
    }

    public void setElseIfOPList(ArrayList<ElseIfOP> elseIfOPList) {
        this.elseIfOPList = elseIfOPList;
    }

    @Override
    public String toString() {
        return "IfStat{" +
                "expr=" + expr +
                ", body=" + body +
                ", elseOP=" + elseOP +
                ", elseIfOPList=" + elseIfOPList +
                '}';
    }
}
