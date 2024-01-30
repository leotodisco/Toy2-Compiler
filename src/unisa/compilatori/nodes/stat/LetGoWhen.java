package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.Body;
import unisa.compilatori.nodes.VarDecl;
import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import java.util.ArrayList;

public class LetGoWhen extends Stat implements Visitable {

    private SymbolTable symbolTable;
    private ArrayList<When> listWhens;

    private VarDecl varDecl;

    private ExprOP condizione;

    private Body otherwiseBody;

    public LetGoWhen(ArrayList<When> listWhens, VarDecl varDecl, Body otherwiseBody) {
        this.listWhens = listWhens;
        this.varDecl = varDecl;
        this.otherwiseBody = otherwiseBody;

        if(!otherwiseBody.getVarDeclList().isEmpty())
            throw new RuntimeException("non puoi usare dichiarazione nell'otherwise");

        this.listWhens.forEach(super::add);
        super.add(varDecl);
        super.add(otherwiseBody);
        super.setTipo(Mode.LETGO);

    }

    public ArrayList<When> getListWhens() {
        return listWhens;
    }

    public void setListWhens(ArrayList<When> listWhens) {
        this.listWhens = listWhens;
    }

    public VarDecl getVarDecl() {
        return varDecl;
    }

    public void setVarDecl(VarDecl varDecl) {
        this.varDecl = varDecl;
    }

    public ExprOP getCondizione() {
        return condizione;
    }

    public void setCondizione(ExprOP condizione) {
        this.condizione = condizione;
    }

    public Body getOtherwiseBody() {
        return otherwiseBody;
    }

    public void setOtherwiseBody(Body otherwiseBody) {
        this.otherwiseBody = otherwiseBody;
    }


    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public String toString() {
        return "LetGoWhen{" +
                "listWhens=" + listWhens +
                ", varDecl=" + varDecl +
                ", condizione=" + condizione +
                ", otherwiseBody=" + otherwiseBody +
                '}';
    }

    public <T> T accept(Visitor<T> visitor) throws RuntimeException {
        return visitor.visit(this);
    }
}