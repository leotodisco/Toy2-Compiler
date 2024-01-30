package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.Body;
import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class When extends DefaultMutableTreeNode implements Visitable {

    private ExprOP condizioneWhen;

    private Body bodyStatements;

    public When(ExprOP condizioneWhen, Body bodyStatements) {
        this.condizioneWhen = condizioneWhen;
        this.bodyStatements = bodyStatements;


        if(!bodyStatements.getVarDeclList().isEmpty())
            throw new RuntimeException("Non puoi usare dichiarazioni nel when");
        super.add(condizioneWhen);

        super.add(bodyStatements);
    }

    public ExprOP getCondizioneWhen() {
        return condizioneWhen;
    }

    public void setCondizioneWhen(ExprOP condizioneWhen) {
        this.condizioneWhen = condizioneWhen;
    }

    public Body getBodyStatements() {
        return bodyStatements;
    }

    public void setBodyStatements(Body bodyStatements) {
        this.bodyStatements = bodyStatements;
    }

    @Override
    public String toString() {
        return "When{" +
                "condizioneWhen=" + condizioneWhen +
                ", bodyStatements=" + bodyStatements +
                '}';
    }


    @Override
    public <T> T accept(Visitor<T> visitor) throws RuntimeException {
        return visitor.visit(this);
    }
}
