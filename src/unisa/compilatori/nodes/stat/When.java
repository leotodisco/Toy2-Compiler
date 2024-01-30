package unisa.compilatori.nodes.stat;

import unisa.compilatori.nodes.expr.ExprOP;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

public class When extends DefaultMutableTreeNode implements Visitable {
    private ExprOP condizione;
    private ArrayList<Stat> stats;

    public When(ExprOP condizione, ArrayList<Stat> stats) {
        this.condizione = condizione;
        this.stats = stats;
    }

    public ExprOP getCondizione() {
        return condizione;
    }

    public void setCondizione(ExprOP condizione) {
        this.condizione = condizione;
    }

    public ArrayList<Stat> getStats() {
        return stats;
    }

    public void setStats(ArrayList<Stat> stats) {
        this.stats = stats;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws RuntimeException {
        return visitor.visit(this);
    }
}
