package unisa.compilatori.semantic.symboltable;

import unisa.compilatori.Token;
import unisa.compilatori.semantic.visitor.Visitable;

/**
 * Classe che rappresenta una entry della tabella dei simboli
 */
public class SymbolTableRecord  {
    private String simbolo;
    private Visitable nodo;
    private FieldType fieldType;
    private String properties;

    public SymbolTableRecord(String simbolo,
                             Visitable nodo,
                             FieldType fieldType,
                             String properties) {
        this.simbolo = simbolo;
        this.nodo = nodo;
        this.fieldType = fieldType;
        this.properties = properties;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public Visitable getNodo(){
        return this.nodo;
    }

    public void setNodo(Visitable nodo){
        this.nodo = nodo;
    }

    public FieldType setFieldType(){
        return fieldType;
    }

    public void setFieldType(FieldType fieldType){
        this.fieldType = fieldType;
    }

    public void setProperties(String properties){
        this.properties = properties;
    }

    public String getProperties(){
        return properties;
    }
    public FieldType getFieldType( ){return this.fieldType;}

    @Override
    public String toString() {
        return "\nSymbolTableRecord{" +
                "simbolo=" + simbolo +
                ", Visitable=" + nodo +
                ", FieldType=" + fieldType +
                ", properties=" + properties +
                "} \n";
    }
}
