package unisa.compilatori.nodes;

import unisa.compilatori.nodes.expr.Identifier;

import javax.swing.tree.DefaultMutableTreeNode;

public class ProcParamDecl extends DefaultMutableTreeNode {

    private Identifier procParamId;

    private Type type;


    public ProcParamDecl(Identifier id, Type type) {
        this.procParamId = id;
        this.type = type;

        super.add(id);
        super.add(type);
    }

    public Identifier getId() {
        return procParamId;
    }

    public void setId(Identifier id) {
        this.procParamId = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ProcParamDecl{" +
                "id='" + procParamId + '\'' +
                ", type=" + type +
                '}';
    }
}
