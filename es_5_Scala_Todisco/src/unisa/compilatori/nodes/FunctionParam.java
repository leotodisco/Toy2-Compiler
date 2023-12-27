package unisa.compilatori.nodes;

import unisa.compilatori.nodes.expr.Identifier;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class FunctionParam extends DefaultMutableTreeNode implements Visitable {
    private Identifier id;
    private Type tipo;

    public FunctionParam(Identifier id, Type tipo) {
        this.id = id;
        this.tipo = tipo;
        super.add(id);
        super.add(tipo);
    }

    public Identifier getId() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public Type getTipo() {
        return tipo;
    }

    public void setTipo(Type tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "FunctionParam{" +
                "id=" + id +
                ", tipo=" + tipo +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
