package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.VarDecl;
import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import java.util.ArrayList;

public class LetStat extends Stat implements Visitable {
    private VarDecl dichiarazioni;
    private ArrayList<When> listaWhens;
    private ArrayList<Stat> statementsOtherwise;

    private SymbolTable table;

    @Override
    public <T> T accept(Visitor<T> visitor) throws RuntimeException {
        return visitor.visit(this);
    }

    public SymbolTable getTable() {
        return table;
    }

    public void setTable(SymbolTable table) {
        this.table = table;
    }

    public LetStat(VarDecl dichiarazioni,
                   ArrayList<When> listaWhens,
                   ArrayList<Stat> statementsOtherwise) {
        super.setTipo(Mode.LET);
        this.dichiarazioni = dichiarazioni;
        this.listaWhens = listaWhens;
        this.statementsOtherwise = statementsOtherwise;
    }

    public VarDecl getDichiarazioni() {
        return dichiarazioni;
    }

    public void setDichiarazioni(VarDecl dichiarazioni) {
        this.dichiarazioni = dichiarazioni;
    }

    public ArrayList<When> getListaWhens() {
        return listaWhens;
    }

    public void setListaWhens(ArrayList<When> listaWhens) {
        this.listaWhens = listaWhens;
    }

    public ArrayList<Stat> getStatementsOtherwise() {
        return statementsOtherwise;
    }

    public void setStatementsOtherwise(ArrayList<Stat> statementsOtherwise) {
        this.statementsOtherwise = statementsOtherwise;
    }
}
