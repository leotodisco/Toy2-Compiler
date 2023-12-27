package unisa.compilatori.nodes;

import unisa.compilatori.Token;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class Type extends DefaultMutableTreeNode implements Visitable {
    private String tipo;

    public Type(final Object lessema) {
        super("Type");
        var token = (Token) lessema;
        this.tipo = token.getAttribute();
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Type{" +
                "tipo='" + tipo + '\'' +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
