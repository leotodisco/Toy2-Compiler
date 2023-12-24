package unisa.compilatori.nodes;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

public class VarDecl extends DefaultMutableTreeNode{
    private ArrayList<Decl> decls;

    public VarDecl(ArrayList<Decl> decls) {
        this.decls = decls;

        this.decls.forEach(decl -> super.add(decl));
    }

    public ArrayList<Decl> getDecls() {
        return decls;
    }

    public void setDecls(ArrayList<Decl> decls) {
        this.decls = decls;
    }

    @Override
    public String toString() {
        return "VarDecl{" +
                "decls=" + decls +
                '}';
    }
}
