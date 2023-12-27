package unisa.compilatori.nodes;

import unisa.compilatori.nodes.expr.Identifier;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Optional;

public class Procedure extends DefaultMutableTreeNode implements Visitable {
    private Identifier id;
    private ArrayList<ProcParamDecl> procParamDeclList;
    private Body body;

    public Procedure(Identifier id,
                     ArrayList<ProcParamDecl> procParamDeclList,
                     Body body) {
        this.id = id;
        Optional.of(procParamDeclList).ifPresent(lista -> this.procParamDeclList=lista);

        this.body = body;

        super.add(id);
        Optional.of(procParamDeclList).ifPresent(param -> param.forEach(p->super.add(p)));
        //procParamDeclList.forEach(param -> super.add(param));
        super.add(body);
    }

    public Procedure(Identifier id, Body body) {
        this.id = id;
        this.body = body;

        super.add(id);
        super.add(body);
    }

    public Identifier getId() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public ArrayList<ProcParamDecl> getProcParamDeclList() {
        return procParamDeclList;
    }

    public void setProcParamDeclList(ArrayList<ProcParamDecl> procParamDeclList) {
        this.procParamDeclList = procParamDeclList;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Procedure{" +
                "id=" + id +
                ", procParamDeclList=" + procParamDeclList +
                ", body=" + body +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
