package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.Body;
import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;
import unisa.compilatori.semantic.symboltable.*;

import java.util.ArrayList;

public class IfStat extends Stat implements Visitable {
    private ExprOP expr;
    private Body body;
    private ElseOP elseOP;

    private ArrayList<ElseIfOP> elseIfOPList;

    private SymbolTable symbolTableThen;

    private SymbolTable symbolTableElse;

    private SymbolTable symbolTableElseIf;

    public IfStat(ExprOP expr, Body body, ElseOP elseOP, ArrayList<ElseIfOP> elseIfOPList, Mode mode) {
        this.expr = expr;
        this.body = body;
        this.elseOP = elseOP;
        this.elseIfOPList = elseIfOPList;
        super.setTipo(mode);

        super.add(expr);
        super.add(body);

        if (elseOP != null) {
            super.add(elseOP);
        }

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

    public SymbolTable getSymbolTableThen() {
        return symbolTableThen;
    }

    public void setSymbolTableThen(SymbolTable symbolTableThen) {
        this.symbolTableThen = symbolTableThen;
    }

    public SymbolTable getSymbolTableElse() {
        return symbolTableElse;
    }

    public void setSymbolTableElse(SymbolTable symbolTableElse) {
        this.symbolTableElse = symbolTableElse;
    }

    public SymbolTable getSymbolTableElseIf() {
        return symbolTableElseIf;
    }

    public void setSymbolTableElseIf(SymbolTable symbolTableElseIf) {
        this.symbolTableElseIf = symbolTableElseIf;
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

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
