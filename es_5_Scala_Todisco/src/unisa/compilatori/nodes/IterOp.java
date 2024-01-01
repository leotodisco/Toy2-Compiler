package unisa.compilatori.nodes;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

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
        if(proceduresList != null) {
            this.procedures = proceduresList;
        } else {
            this.procedures = new ArrayList<>();
        }

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

    public static IterOp mergeIterOps (IterOp iterOp1,Procedure proc,  IterOp iterOp2) {
        ArrayList<Function> functions = new ArrayList<>();
        functions.addAll(iterOp1.functions);
        functions.addAll(iterOp2.functions);

        ArrayList<Procedure> procedures = new ArrayList<>();
        procedures.addAll(iterOp1.procedures);
        procedures.add(proc);
        procedures.addAll(iterOp2.procedures);

        ArrayList<VarDecl> varDecls = new ArrayList<VarDecl>();
        varDecls.addAll(iterOp1.declarations);
        varDecls.addAll(iterOp2.declarations);

        return new IterOp(functions, procedures, varDecls);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
