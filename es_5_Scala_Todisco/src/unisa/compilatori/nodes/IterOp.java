package unisa.compilatori.nodes;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

public class IterOp extends DefaultMutableTreeNode implements Visitable {
    private ArrayList<Function> functions;
    private ArrayList<VarDecl> declarations;
    private ArrayList<Procedure> procedures;

    public IterOp() {
        this.functions = new ArrayList<>();
        this.declarations = new ArrayList<>();
        this.procedures = new ArrayList<>();

        super.add(new DefaultMutableTreeNode());
    }

    public IterOp(ArrayList<Function> functions,
                  ArrayList<Procedure> proceduresList,
                  ArrayList<VarDecl> declaration) {
        this.functions = functions;
        this.declarations = declaration;
        this.procedures = proceduresList;

        functions.forEach(s->super.add(s));
        this.declarations.forEach(s->super.add(s));
        this.procedures.forEach(proce -> super.add(proce));
    }

    public void addFunction(Function f) {
        this.functions.add(f);
        super.add(f);
    }

    public void addProcedure(Procedure f) {
        this.procedures.add(f);
        super.add(f);
    }

    public void addDecl(VarDecl f) {
        this.declarations.add(f);
        super.add(f);
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(ArrayList<Function> functions) {
        this.functions = functions;
    }

    public ArrayList<Procedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(ArrayList<Procedure> procedures) {
        this.procedures = procedures;
    }

    public ArrayList<VarDecl> getDeclarations() {
        return declarations;
    }

    public void setDeclarations(ArrayList<VarDecl> declarations) {
        this.declarations = declarations;
    }

    @Override
    public String toString() {
        return "IterOp{" +
                "functions=" + functions +
                ", procedures=" + procedures +
                ", declarations=" + declarations +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
