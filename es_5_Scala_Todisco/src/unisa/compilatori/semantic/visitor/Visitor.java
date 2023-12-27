package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.nodes.*;

public interface Visitor<T> {
    T visit(ProgramOp program);
    T visit(BinaryOP operazioneBinaria);
    T visit(UnaryOP operazioneUnaria);
    T visit(VarDecl dichiarazione);
    T visit(Function funzione);
    T visit(Stat statement);
    T visit(Body body);


}
