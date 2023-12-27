package unisa.compilatori.semantic.symboltable;

import java.util.Optional;

public interface SymbolTable {
    void enterScope();

    void exitScope();

    int getScopeLevel();

    boolean probe(String lexeme);

    Optional<SymbolTableRecord> lookup(String lexeme);

    Optional<SymbolTableRecord> lookupForMoreElement(String lexeme);

    void addEntry(String lexeme, SymbolTableRecord str);
}
