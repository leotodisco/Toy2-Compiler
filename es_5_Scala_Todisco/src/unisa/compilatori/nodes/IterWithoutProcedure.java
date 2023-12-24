package unisa.compilatori.nodes;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

public class IterWithoutProcedure extends DefaultMutableTreeNode {
    private ArrayList<Function> functions;
    private ArrayList<VarDecl> declarations;

    public IterWithoutProcedure() {
        this.declarations = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public void addFunction(Function f) {
        this.functions.add(f);
        super.add(f);
    }

    public void addDecl(VarDecl f) {
        this.declarations.add(f);
        super.add(f);
    }

    @Override
    public String toString() {
        return "IterWithoutProcedure{" +
                "functions=" + functions +
                ", declarations=" + declarations +
                '}';
    }
}
