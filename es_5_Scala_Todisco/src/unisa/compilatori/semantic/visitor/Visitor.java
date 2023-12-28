package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.nodes.*;

public interface Visitor<T> {
    T visit(ProgramOp program);
    T visit(BinaryOP operazioneBinaria);
    T visit(UnaryOP operazioneUnaria);
    T visit(VarDecl dichiarazione);
    T visit(Function funzione) throws Exception;
    T visit(Stat statement);
    T visit(IfStat ifStat);
    T visit(ElseOP elseOP);
    T visit(ElseIfOP elseIfOP);
    T visit(ProcCall procCall) throws Exception;
    T visit(WhileStat whileStat) throws Exception;
    T visit(IOArgsOp ioArgsOp);
    T visit(Body body);
    T visit(Type type);
    T visit(ProcParamDecl procParam);
    T visit(FunctionParam functionParam);
    T visit(Decl decl);
    T visit(Identifier id);
    T visit(IterOp iterOP);
    T visit(IterWithoutProcedure iterWithoutProcedure);
    T visit(Procedure procedure);
    T visit(ConstOP constOP);
    T visit(FunCall funCall) throws Exception;
    T visit(IOArgsExpr ioArgsExpr);
    T visit(ExprOP exprOP);
    T visit(CallableParam callableParam);
}
