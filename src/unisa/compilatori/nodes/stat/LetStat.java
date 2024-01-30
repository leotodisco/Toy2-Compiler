package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.VarDecl;
import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import java.util.ArrayList;

public class LetStat extends Stat implements Visitable {
    private VarDecl dichiarazioni;
    private ExprOP condizione1;
    private ArrayList<Stat> statements1;

    private ExprOP condizione2;
    private ArrayList<Stat> statements2;

    private ArrayList<Stat> statementsOtherwise;

    private SymbolTable table;

    public SymbolTable getTable() {
        return table;
    }

    public void setTable(SymbolTable table) {
        this.table = table;
    }

    public LetStat(VarDecl dichiarazioni,
                   ExprOP condizione1,
                   ArrayList<Stat> statements1,
                   ExprOP condizione2,
                   ArrayList<Stat> statements2,
                   ArrayList<Stat> statementsOtherwise) {
        this.dichiarazioni = dichiarazioni;
        this.condizione1 = condizione1;
        this.statements1 = statements1;
        this.condizione2 = condizione2;
        this.statements2 = statements2;
        this.statementsOtherwise = statementsOtherwise;

        super.setTipo(Mode.LET);
        super.add(dichiarazioni);
        super.add(condizione1);
        statements1.forEach(super::add);
        super.add(condizione2);
        statements2.forEach(super::add);
        statementsOtherwise.forEach(super::add);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws RuntimeException {
        return visitor.visit(this);
    }

    public VarDecl getDichiarazioni() {
        return dichiarazioni;
    }

    public void setDichiarazioni(VarDecl dichiarazioni) {
        this.dichiarazioni = dichiarazioni;
    }

    public ExprOP getCondizione1() {
        return condizione1;
    }

    public void setCondizione1(ExprOP condizione1) {
        this.condizione1 = condizione1;
    }

    public ArrayList<Stat> getStatements1() {
        return statements1;
    }

    public void setStatements1(ArrayList<Stat> statements1) {
        this.statements1 = statements1;
    }

    public ExprOP getCondizione2() {
        return condizione2;
    }

    public void setCondizione2(ExprOP condizione2) {
        this.condizione2 = condizione2;
    }

    public ArrayList<Stat> getStatements2() {
        return statements2;
    }

    public void setStatements2(ArrayList<Stat> statements2) {
        this.statements2 = statements2;
    }

    public ArrayList<Stat> getStatementsOtherwise() {
        return statementsOtherwise;
    }

    public void setStatementsOtherwise(ArrayList<Stat> statementsOtherwise) {
        this.statementsOtherwise = statementsOtherwise;
    }
}
