package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.nodes.expr.Identifier;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Optional;

public class Stat extends DefaultMutableTreeNode implements Visitable {
    public enum Mode {
        READ,
        ASSIGN,
        RETURN,
        WRITE,
        WRITE_RETURN,
        WHILE,
        IF
    }
    private Mode tipo;
    private ArrayList<ExprOP> espressioniList;
    private ArrayList<Identifier> idsList;

    public Stat() {
        super();
    }


    public Stat(Mode tipo,
                ArrayList<Identifier> idList,
                ArrayList<ExprOP> espressioniList
                ) {
        this.tipo = tipo;
        this.espressioniList = espressioniList;
        this.idsList =idList;

        super.add(new DefaultMutableTreeNode(tipo.toString()));
        Optional.ofNullable(idList).ifPresent(idLista -> idLista.forEach(arg -> super.add(arg)));
        Optional.ofNullable(this.espressioniList).ifPresent(list -> list.forEach(exprOP -> super.add(exprOP)));
    }

    public Stat(Mode tipo, ArrayList<ExprOP> espressioniList) {
        super.add(new DefaultMutableTreeNode(tipo.toString()));
        this.tipo = tipo;
        this.espressioniList = espressioniList;
        espressioniList.forEach(exprOP -> super.add(exprOP));
    }

    public Mode getTipo() {
        return tipo;
    }

    public void setTipo(Mode tipo) {
        this.tipo = tipo;
    }

    public ArrayList<ExprOP> getEspressioniList() {
        return espressioniList;
    }

    public void setEspressioniList(ArrayList<ExprOP> espressioniList) {
        this.espressioniList = espressioniList;
    }


    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Stat{" +
                "tipo=" + tipo +
                ", espressioniList=" + espressioniList +
                '}';
    }

    public ArrayList<Identifier> getIdsList() {
        return idsList;
    }

    public void setIdsList(ArrayList<Identifier> idsList) {
        this.idsList = idsList;
    }
}
