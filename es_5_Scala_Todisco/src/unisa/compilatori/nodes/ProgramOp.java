package unisa.compilatori.nodes;

import javax.swing.tree.DefaultMutableTreeNode;

public class ProgramOp extends DefaultMutableTreeNode {
    private Procedure proc;
    private IterWithoutProcedure iterWithoutProcedure;
    private IterOp iterOp;

    public ProgramOp(Procedure proc,
                     IterWithoutProcedure iterWithoutProcedure,
                     IterOp iterOp) {
        this.proc = proc;
        this.iterWithoutProcedure = iterWithoutProcedure;
        this.iterOp = iterOp;

        super.add(proc);
        super.add(iterWithoutProcedure);
        super.add(iterOp);
    }

    @Override
    public String toString() {
        return "ProgramOp{" +
                "proc=" + proc +
                ", iterWithoutProcedure=" + iterWithoutProcedure +
                ", iterOp=" + iterOp +
                '}';
    }
}
