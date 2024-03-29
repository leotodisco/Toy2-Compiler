package unisa.compilatori.semantic.symboltable;

import java_cup.runtime.Symbol;
import unisa.compilatori.Token;
import unisa.compilatori.utils.Exceptions;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Symbol Table
 */
public class SymbolTable implements ISymbolTable {
    private SymbolTable root;
    private ArrayList<SymbolTableRecord> recordsList;
    /** serve a controllare se nello scope c'è già l'id */
    private static boolean shadowing = true;
    private String scope;
    public static final String NAME_ROOT = "ROOT";
    private ISymbolTable father;

    public SymbolTable(){
        root = null;
        recordsList = new ArrayList<SymbolTableRecord>();
    }

    public SymbolTable(SymbolTable rootTable, ArrayList<SymbolTableRecord> list, String scope){
        this.root = rootTable;
        this.recordsList = list;
        this.scope = scope;
    }

    /**
     *
     * @param symbol è il lessema da controllare se è presente nella tabella attuale
     * Metodo che controlla se nella tabella attuale esiste il lessema
     *
     * */
    @Override
    public boolean probe(String symbol) {
        return this.recordsList
                .stream()
                .anyMatch(record -> record.getSimbolo().equals(symbol));
    }

    /**
     * Controlla nella lista delle symbol table se esiste già il lessema da qualche parte
     * @param sym
     * @retur
     */
    @Override
    public Optional<SymbolTableRecord> lookup(String symbol) {
        SymbolTable symbolTable = this;
        Optional<SymbolTableRecord> result = Optional.ofNullable(null);

        while (symbolTable != null) {
            for (SymbolTableRecord r : symbolTable.recordsList) {
                if (r.getSimbolo().equals(symbol)){
                    result = Optional.ofNullable(r);

                    symbolTable = (SymbolTable) symbolTable.father;
                    return result;
                }
            }
            symbolTable = (SymbolTable) symbolTable.father;
        }
        return result;
    }
        /**
         *SymbolTable symbolTable=this;
         *         while (symbolTable!=null){
         *             for(TableRow r:symbolTable.listRow) {
         *                 if (r.getSymbol().equals(id))
         *                     return r;
         *             }
         *             symbolTable=symbolTable.father;
         *         }
         *         return null;
         *
         * ;
         *
         */


    /**
     *
     * @param record
     * Questo metodo aggiunge un record alla nostra tabella dei simboli
     * @throws Exception
     */
    @Override
    public void addEntry(SymbolTableRecord record) throws RuntimeException {

        if(shadowing) {
            if(isRecordDeclared(record)) {
                throw new Exceptions.MultipleDeclaration(record.getSimbolo());
            } else {
                recordsList.add(record);
            }
        } else {
            // se lo shadowing è true controlla la tabella attuale
            //controlla la tabella attuale e se può aggiunge
            ISymbolTable symbolTable = this;
            while (symbolTable != null) {
                if(symbolTable.isRecordDeclared(record)) {
                    throw new Exceptions.MultipleDeclaration(record.getSimbolo());
                }
                symbolTable = symbolTable.getFather();
            }
            recordsList.add(record);
        }
    }

    @Override
    public boolean isRecordDeclared(SymbolTableRecord record){
        return this.probe(record.getSimbolo()) || record.getSimbolo().equals(this.scope) || record.getSimbolo().equals(NAME_ROOT);
    }

    public SymbolTable getRoot() {
        return root;
    }

    public void setRoot(SymbolTable root) {
        this.root = root;
    }

    public ArrayList<SymbolTableRecord> getRecordsList() {
        return recordsList;
    }

    public void setRecordsList(ArrayList<SymbolTableRecord> recordsList) {
        this.recordsList = recordsList;
    }

    public static boolean isShadowing() {
        return shadowing;
    }

    public static void setShadowing(boolean shadowing) {
        SymbolTable.shadowing = shadowing;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public ISymbolTable getFather(){
        return this.father;
    }

    @Override
    public void setFather(ISymbolTable father){
        this.father = father;
    }

    @Override
    public String toString() {
        return "SymbolTable{"
                + "\n"+
                ", recordsList=" + recordsList + "\n"+

                '}';
    }
}
