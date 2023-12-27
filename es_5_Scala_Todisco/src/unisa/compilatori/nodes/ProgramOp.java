package unisa.compilatori.nodes;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class ProgramOp extends DefaultMutableTreeNode implements Visitable {
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

    public Procedure getProc() {
        return proc;
    }

    public void setProc(Procedure proc) {
        this.proc = proc;
    }

    public IterWithoutProcedure getIterWithoutProcedure() {
        return iterWithoutProcedure;
    }

    public void setIterWithoutProcedure(IterWithoutProcedure iterWithoutProcedure) {
        this.iterWithoutProcedure = iterWithoutProcedure;
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
                "proc=" + proc +
                ", iterWithoutProcedure=" + iterWithoutProcedure +
                ", iterOp=" + iterOp +
                '}';
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
