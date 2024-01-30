package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.nodes.*;

public interface Visitor<T> {
    default T visit(LetStat l) throws RuntimeException {
        return null;
    };
    T visit(ProgramOp program) throws RuntimeException;
    T visit(BinaryOP operazioneBinaria) throws RuntimeException;
    T visit(UnaryOP operazioneUnaria) throws RuntimeException;
    T visit(VarDecl dichiarazione) throws RuntimeException;
    T visit(Function funzione) throws RuntimeException;
    T visit(Stat statement) throws RuntimeException;
    T visit(IfStat ifStat) throws RuntimeException;
    T visit(ElseOP elseOP) throws RuntimeException;
    T visit(ElseIfOP elseIfOP) throws RuntimeException;
    T visit(ProcCall procCall) throws RuntimeException;
    T visit(WhileStat whileStat) throws RuntimeException;
    T visit(Body body) throws RuntimeException;
    T visit(Type type) throws RuntimeException;
    T visit(ProcParamDecl procParam) throws RuntimeException;
    T visit(FunctionParam functionParam) throws RuntimeException;
    T visit(Decl decl) throws RuntimeException;
    T visit(Identifier id) throws RuntimeException;
    T visit(IterOp iterOP) throws RuntimeException;
    T visit(Procedure procedure) throws RuntimeException;
    T visit(ConstOP constOP) throws RuntimeException;
    T visit(FunCall funCall) throws RuntimeException;
    T visit(ExprOP exprOP) throws RuntimeException;
    T visit(CallableParam callableParam) throws RuntimeException;
}
