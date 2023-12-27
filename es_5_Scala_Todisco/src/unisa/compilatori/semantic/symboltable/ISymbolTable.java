package unisa.compilatori.semantic.symboltable;

import unisa.compilatori.Token;

import java.util.Optional;

public interface ISymbolTable {
    /**
     *
     * @param symbol è il lessema da controllare se è presente nella tabella
     * Metodo che controlla se nella tabella attuale esiste il lessema
     *
     * */
    boolean probe(String symbol);

    /**
     *
     * @param symbol
     * @return
     *
     */
    Optional<SymbolTableRecord> lookup(String symbol);

    /**
     * @param record
     * Questo metodo aggiunge un record alla nostra tabella dei simboli
     */
    void addEntry(SymbolTableRecord record) throws Exception;

    /**
     *
     * @return La symboltable immediatamente sotto la symboltable corrente nello stack dello scope
     */
    ISymbolTable getFather();

    /**
     * Setta il father a father
     * @param father
     */
    void setFather(ISymbolTable father);

    public boolean isRecordDeclared(SymbolTableRecord record);
}
