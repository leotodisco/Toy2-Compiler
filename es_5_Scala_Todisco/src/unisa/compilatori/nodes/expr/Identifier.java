package unisa.compilatori.nodes.expr;

import unisa.compilatori.Token;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class Identifier extends ExprOP implements Visitable {
    private String lessema;

    private Mode mode;
    public enum Mode {
        PARAMSREF, //modalità ref in procexprs
        PARAMSOUT,  //modalità out per id in procparamid
        FUNCTIONIDENTIFIER,
        PROCEDUREIDENTIFIER,
        VARIABLENAME,
        PARAMS,
        NONE; //id senza niente
    }

    public Identifier(Object tkn, Mode mode) {
        super();
        var token = (Token) tkn;
        this.lessema = token.getAttribute();
        this.mode = mode;

        super.add(new DefaultMutableTreeNode(lessema));
    }

    public String getLessema() {return this.lessema;}

    public void setLessema(String lessema) {
        this.lessema = lessema;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "lessema='" + lessema + '\'' +
                ", mode=" + mode +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
