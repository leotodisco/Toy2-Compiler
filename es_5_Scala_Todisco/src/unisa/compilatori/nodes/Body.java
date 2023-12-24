package unisa.compilatori.nodes;

import unisa.compilatori.nodes.stat.Stat;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

public class Body extends DefaultMutableTreeNode {
    private ArrayList<Stat> statList;
    private ArrayList<VarDecl> varDeclList;

    public Body(ArrayList<Stat> statementsList, ArrayList<VarDecl> varDeclsList) {
        super("Body");
        this.statList = statementsList;
        this.varDeclList = varDeclsList;

        this.statList.forEach(stat -> super.add(stat));
        this.varDeclList.forEach(decl -> super.add(decl));
    }

    public Body() {
        super("Body");
        this.statList = null;
        this.varDeclList = null;
    }

    public void addStatement(Stat stat) {
        this.statList.add(stat);
        super.add(stat);
    }

    public void addDecl(VarDecl decl) {
        this.varDeclList.add(decl);
        super.add(decl);
    }

    public ArrayList<Stat> getStatList() {
        return this.statList;
    }

    public ArrayList<VarDecl> getVarDeclList() {
        return this.varDeclList;
    }

    public void setStatList(ArrayList<Stat> statList) {
        this.statList = statList;
    }

    public void setVarDeclList(ArrayList<VarDecl> varDeclList ) {
        this.varDeclList = varDeclList;
    }

    @Override
    public String toString() {
        return "Body{" +
                "statList=" + statList +
                ", varDeclList=" + varDeclList +
                '}';
    }
}
