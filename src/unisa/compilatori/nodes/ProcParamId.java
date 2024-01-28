package unisa.compilatori.nodes;

import unisa.compilatori.nodes.expr.Identifier;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class ProcParamId extends DefaultMutableTreeNode implements Visitable {

    private Identifier Id;
    private Boolean outMode;

    public ProcParamId(Identifier id, Boolean outMode) {
        Id = id;
        this.outMode = outMode;

        super.add(id);
    }

    public Identifier getId() {
        return Id;
    }

    public void setId(Identifier id) {
        Id = id;
    }

    public Boolean getOutMode() {
        return outMode;
    }

    public void setOutMode(Boolean outMode) {
        this.outMode = outMode;
    }

    @Override
    public String toString() {
        return "ProcParamId{" +
                "Id='" + Id + '\'' +
                ", outMode=" + outMode +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}
