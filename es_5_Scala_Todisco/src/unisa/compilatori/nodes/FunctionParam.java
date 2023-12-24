package unisa.compilatori.nodes;

import unisa.compilatori.nodes.expr.Identifier;

import javax.swing.tree.DefaultMutableTreeNode;

public class FunctionParam extends DefaultMutableTreeNode {
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
}
