package unisa.compilatori.nodes;


import unisa.compilatori.semantic.visitor.Visitable;

import unisa.compilatori.semantic.symboltable.*;
import unisa.compilatori.semantic.visitor.Visitor;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

public class ProgramOp extends DefaultMutableTreeNode implements Visitable {
    private IterOp iterOp;

    private SymbolTable table;

    public void setTable(SymbolTable table) {
        this.table = table;
    }

    public SymbolTable getTable() {
        return this.table;
}

    public static IterOp mergeIterOps (IterOp iterOp1,Procedure proc,  IterOp iterOp2) {
        ArrayList<Function> functions = new ArrayList<>();
        functions.addAll(iterOp1.getFunctions());
        functions.addAll(iterOp2.getFunctions());

        ArrayList<Procedure> procedures = new ArrayList<>();
        procedures.addAll(iterOp1.getProcedures());
        procedures.add(proc);
        procedures.addAll(iterOp2.getProcedures());

        ArrayList<VarDecl> varDecls = new ArrayList<VarDecl>();
        varDecls.addAll(iterOp1.getDeclarations());
        varDecls.addAll(iterOp2.getDeclarations());

        return new IterOp(functions, procedures, varDecls);
    }


    public ProgramOp(Procedure proc,
                     IterOp iterWithoutProcedure,
                     IterOp iterOp) {
        //this.proc = proc;
        //this.iterWithoutProcedure = iterWithoutProcedure;
        //iterOp.getProcedures().add(proc);
        this.iterOp = mergeIterOps(iterOp,proc, iterWithoutProcedure);

        super.add(this.iterOp);
    }





    public IterOp getIterOp() {
        return iterOp;
    }

    public void setIterOp(IterOp iterOp) {
        this.iterOp = iterOp;
    }

    @Override
    public String toString() {
        return "ProgramOp{" +
                ", iterOp=" + iterOp +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws RuntimeException {
        return visitor.visit(this);
    }
}
