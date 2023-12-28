package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.Body;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;


public class ElseOP extends Stat implements Visitable {

    private Body body;

    private SymbolTable symbolTableElseOp;

    public ElseOP(Body body) {
        this.body = body;
        super.add(body);
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ElseOP{" +
                "body=" + body +
                '}';
    }

    public SymbolTable getSymbolTableElseOp() {
        return symbolTableElseOp;
    }

    public void setSymbolTableElseOp(SymbolTable symbolTableElseOp) {
        this.symbolTableElseOp = symbolTableElseOp;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
