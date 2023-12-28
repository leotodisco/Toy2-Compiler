package unisa.compilatori.nodes;

import unisa.compilatori.nodes.expr.ConstOP;
import unisa.compilatori.nodes.expr.Identifier;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Optional;

public class Decl extends DefaultMutableTreeNode implements Visitable {

    private ArrayList<ConstOP> consts;
    private ArrayList<Identifier> ids;
    private Type tipo;
    private TipoDecl tipoDecl;

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    /*
    * le modalità di una dichiarazio possono essere 2.
    * MODE.ASSIGN se stai anche assegnando un valore e non hai bisogno di scrivere il tipo esplicitamente;
    * MODE.TYPE se stai scrivendo esplicitamente il tipo ma non il valore.
    * */
    public enum TipoDecl {
        ASSIGN,
        TYPE;
    }

    public Decl(ArrayList<ConstOP> consts, ArrayList<Identifier> ids, Type tipo, TipoDecl tipoDecl) {
        super();
        this.consts = consts;
        this.ids = ids;
        this.tipo = tipo;
        this.tipoDecl = tipoDecl;

        this.consts.forEach(constant -> super.add(constant));
        this.ids.forEach(identifier -> super.add(identifier));

        //uso un optional per gestire se il tipo viene passato o meno
        Optional.ofNullable(tipo).ifPresent(type -> super.add(tipo));

    }

    public Decl(ArrayList<Identifier> ids, Type tipo, TipoDecl mode) {
        super();
        this.consts = null;
        this.tipoDecl = mode;
        this.ids = ids;
        this.tipo = tipo;

        this.ids.forEach(identifier -> super.add(identifier));

        Optional.ofNullable(tipo).ifPresent(type -> super.add(tipo));

    }

    public ArrayList<ConstOP> getConsts() {
        return consts;
    }

    public void setConsts(ArrayList<ConstOP> consts) {
        this.consts = consts;
    }

    public ArrayList<Identifier> getIds() {
        return ids;
    }

    public void setIds(ArrayList<Identifier> ids) {
        this.ids = ids;
    }

    public Type getTipo() {
        return tipo;
    }

    public void setTipo(Type tipo) {
        this.tipo = tipo;
    }

    public TipoDecl getTipoDecl() {
        return tipoDecl;
    }

    public void setTipoDecl(TipoDecl tipoDecl) {
        this.tipoDecl = tipoDecl;
    }

    @Override
    public String toString() {
        return "Decl{" +
                "consts=" + consts +
                ", ids=" + ids +
                ", tipo=" + tipo +
                ", tipoDecl=" + tipoDecl +
                '}';
    }
}
