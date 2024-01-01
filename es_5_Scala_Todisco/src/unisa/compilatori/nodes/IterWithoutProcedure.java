package unisa.compilatori.nodes;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

public class IterWithoutProcedure extends DefaultMutableTreeNode implements Visitable {
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


    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(ArrayList<Function> functions) {
        this.functions = functions;
    }

    public ArrayList<VarDecl> getDeclarations() {
        return declarations;
    }

    public void setDeclarations(ArrayList<VarDecl> declarations) {
        this.declarations = declarations;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws RuntimeException {
        return visitor.visit(this);
    }
}
