package unisa.compilatori.semantic.symboltable;

import unisa.compilatori.nodes.CallableParam;
import unisa.compilatori.nodes.Type;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Questa classe mantiene le informazioni su Procedure e Function
 */
public class CallableFieldType implements FieldType {
    private ArrayList<CallableParam> inputParams;

    private ArrayList<CallableParam> outputParams;

    public CallableFieldType() {
        inputParams = new ArrayList<CallableParam>();
        outputParams = new ArrayList<CallableParam>();
    }

    public CallableFieldType(ArrayList<CallableParam> inputParams, ArrayList<CallableParam> outputParams){
        this.inputParams = inputParams;
        this.outputParams = outputParams;
    }

    public ArrayList<CallableParam> getInputParams() {
        return inputParams;
    }

    public void setInputParams(ArrayList<CallableParam> inputParams) {
        this.inputParams = inputParams;
    }

    public ArrayList<CallableParam> getOutputParams() {
        return outputParams;
    }

    public void setOutputParams(ArrayList<CallableParam> outputParams) {
        this.outputParams = outputParams;
    }

    @Override
    public String toString() {
        return "CallableFieldType{" +
                "inputParam=" + inputParams +
                ", outputParam=" + outputParams +
                '}';
    }
}
