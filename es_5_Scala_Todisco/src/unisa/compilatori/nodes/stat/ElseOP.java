package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.Body;

import javax.swing.tree.DefaultMutableTreeNode;

public class ElseOP extends DefaultMutableTreeNode {

    private Body body;

    public ElseOP(Body body) {
        this.body = body;
        super.add(body);
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ElseOP{" +
                "body=" + body +
                '}';
    }
}
