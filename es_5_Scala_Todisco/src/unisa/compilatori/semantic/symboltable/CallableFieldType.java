package unisa.compilatori.semantic.symboltable;

import unisa.compilatori.nodes.CallableParam;
import unisa.compilatori.nodes.Type;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Questa classe mantiene le informazioni su Procedure e Function
 */
public class CallableFieldType implements FieldType {
    private ArrayList<CallableParam> params;


    public CallableFieldType(){

    }
    public CallableFieldType(ArrayList<CallableParam> params) {
        if(params == null){
            this.params = new ArrayList<>();
        }else {
            this.params = params;
        }
    }

    public ArrayList<CallableParam> getParams() {
        return params;
    }

    public void setParams(ArrayList<CallableParam> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "CallableFieldType{" +
                "params=" + params +
                '}';
    }
}
