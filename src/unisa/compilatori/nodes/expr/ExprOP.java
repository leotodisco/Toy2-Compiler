package unisa.compilatori.nodes.expr;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class ExprOP extends DefaultMutableTreeNode implements Visitable {
    private Mode mode;
    private String tipo;
    public enum Mode {
        PARAMSREF, //modalità ref in procexprs
        PARAMSOUT,  //modalità out per id in procparamid
        FUNCTIONIDENTIFIER,
        VARIABLENAME,
        PARAMS,
        UNARY,
        BINARY,
        PROCEDUREIDENTIFIER,
        CONST,
        IOARGS,
        IOARGSDOLLAR,
        FUNCALL,
        NONE;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public ExprOP(Mode mode) {
        super();
        this.mode = mode;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Expr{" + "Mode = " + mode + " }";
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {

        return visitor.visit(this);
    }
}
