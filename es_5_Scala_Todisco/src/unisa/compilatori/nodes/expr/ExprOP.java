package unisa.compilatori.nodes.expr;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class ExprOP extends DefaultMutableTreeNode implements Visitable {
    private Mode mode;
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
        FUNCALL,
        NONE;
    }

    public Mode getMode() {
        return this.mode;
    }


    public ExprOP(Mode mode) {
        super();
        this.mode = mode;
    }


    @Override
    public String toString() {
        return "Expr{" + "Mode = " + mode + " }";
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws Exception {

        return visitor.visit(this);
    }
}
