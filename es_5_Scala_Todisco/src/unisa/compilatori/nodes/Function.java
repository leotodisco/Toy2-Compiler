package unisa.compilatori.nodes;

import unisa.compilatori.nodes.expr.Identifier;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Optional;

public class Function extends DefaultMutableTreeNode implements Visitable {
    private Identifier id;
    private ArrayList<Type> returnTypes;
    private ArrayList<FunctionParam> parametersList;
    private Body body;

    private SymbolTable table;

    public Function(Identifier id,
                    ArrayList<Type> returnTypes,
                    ArrayList<FunctionParam> parametersList,
                    Body body) {
        this.id = id;
        this.returnTypes = returnTypes;
        this.parametersList = parametersList;
        this.body = body;
        super.add(id);
        super.add(body);
        Optional.ofNullable(parametersList).ifPresent(items -> items.forEach(item -> super.add(item)));
        returnTypes.forEach(type -> super.add(type));
    }

    @Override
    public String toString() {
        return "Function{" +
                "id=" + id +
                ", returnTypes=" + returnTypes +
                ", parametersList=" + parametersList +
                ", body=NON STAMPATO"  +
                "} FINE \n";
    }

    public Identifier getId() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public ArrayList<Type> getReturnTypes() {
        return returnTypes;
    }

    public void setReturnTypes(ArrayList<Type> returnTypes) {
        this.returnTypes = returnTypes;
    }

    public ArrayList<FunctionParam> getParametersList() {
        return parametersList;
    }

    public void setParametersList(ArrayList<FunctionParam> parametersList) {
        this.parametersList = parametersList;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public SymbolTable getTable() {
        return table;
    }

    public void setTable(SymbolTable table) {
        this.table = table;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
