package unisa.compilatori.nodes;

import unisa.compilatori.nodes.expr.Identifier;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Optional;

public class Function extends DefaultMutableTreeNode {
    private Identifier id;
    private ArrayList<Type> returnTypes;
    private ArrayList<FunctionParam> parametersList;
    private Body body;

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
}
