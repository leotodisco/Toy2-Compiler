package unisa.compilatori.semantic.symboltable;

import java.util.ArrayList;

/**
 * Questa classe mantiene le informazioni su Procedure e Function
 */
public class CallableFieldType implements FieldType {
    private ArrayList<String> inputParams;

    private ArrayList<String> outputParams;

    public CallableFieldType() {
        inputParams =new ArrayList<String>();
        outputParams =new ArrayList<String>();
    }

    public CallableFieldType(ArrayList<String> inputParams, ArrayList<String> outputParams){
        this.inputParams = inputParams;
        this.outputParams = outputParams;
    }

    public ArrayList<String> getInputParams() {
        return inputParams;
    }

    public void setInputParams(ArrayList<String> inputParams) {
        this.inputParams = inputParams;
    }

    public ArrayList<String> getOutputParams() {
        return outputParams;
    }

    public void setOutputParams(ArrayList<String> outputParams) {
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
