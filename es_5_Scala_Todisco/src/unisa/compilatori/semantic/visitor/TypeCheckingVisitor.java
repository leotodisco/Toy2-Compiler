package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;

public class TypeCheckingVisitor implements Visitor {
    /**
     * Metodo che dati due tipi e un'operazione restituisce il tipo del risultato
     * @param type1
     * @param type2
     * @param op
     * @return
     */
    private String evaluateType(String type1, String type2, String op) throws Exception{
        switch (op){
            case "plus", "times", "div", "minus", "pow":
                if (type1.equals("integer") && type2.equals("integer"))
                    return "integer";
                else if (type1.equals("integer") && type2.equals("float"))
                    return new String("float");
                else if (type1.equals("float") && type2.equals("integer"))
                    return new String("float");
                else if (type1.equals("float") && type2.equals("float"))
                    return new String("float");
                else
                    throw new Exception("errore");

            case "or", "and":
                if(type1.equals("bool") && type2.equals("bool"))
                    return new String("bool");
                else
                    throw new Exception("errore");

            case "stringConcat":
                if(type1.equals("string") && type2.equals("string"))
                    return new String("string");
                else
                    throw new Exception("errore");

            case "gt", "ge", "lt", "le":
                if(type1.equals("integer") && type2.equals("integer"))
                    return new String("bool");
                else if(type1.equals("float") && type2.equals("integer"))
                    return new String("bool");
                else if(type1.equals("integer") && type2.equals("float"))
                    return new String("bool");
                else if(type1.equals("float") && type2.equals("float"))
                    return new String("bool");
                else
                    throw new Exception("errore");

            case "eq", "ne":
                if(type1.equals(type2))
                    return new String("bool");
                else
                    throw new Exception("errore");
        }
        return null;
    }

    @Override
    public Object visit(ProgramOp program) {
        return null;
    }

    @Override
    public Object visit(BinaryOP operazioneBinaria) {
        return null;
    }

    @Override
    public Object visit(UnaryOP operazioneUnaria) {
        return null;
    }

    @Override
    public Object visit(VarDecl dichiarazione) {
        return null;
    }

    @Override
    public Object visit(Function funzione) throws Exception {
        return null;
    }

    @Override
    public Object visit(Stat statement) {
        return null;
    }

    @Override
    public Object visit(IfStat ifStat) {
        return null;
    }

    @Override
    public Object visit(ElseOP elseOP) {
        return null;
    }

    @Override
    public Object visit(ElseIfOP elseIfOP) {
        return null;
    }

    @Override
    public Object visit(ProcCall procCall) {
        return null;
    }

    @Override
    public Object visit(WhileStat whileStat) {
        return null;
    }

    @Override
    public Object visit(IOArgsOp ioArgsOp) {
        return null;
    }

    @Override
    public Object visit(Body body) {
        return null;
    }

    @Override
    public Object visit(Type type) {
        return null;
    }

    @Override
    public Object visit(ProcParamDecl procParam) {
        return null;
    }

    @Override
    public Object visit(FunctionParam functionParam) {
        return null;
    }

    @Override
    public Object visit(Decl decl) {
        return null;
    }

    @Override
    public Object visit(Identifier id) {
        return null;
    }

    @Override
    public Object visit(IterOp iterOP) {
        return null;
    }

    @Override
    public Object visit(IterWithoutProcedure iterWithoutProcedure) {
        return null;
    }

    @Override
    public Object visit(Procedure procedure) {
        return null;
    }

    @Override
    public Object visit(ConstOP constOP) {
        return null;
    }

    @Override
    public Object visit(FunCall funCall) {
        return null;
    }

    @Override
    public Object visit(IOArgsExpr ioArgsExpr) {
        return null;
    }

    @Override
    public Object visit(ExprOP exprOP) {
        return null;
    }

    @Override
    public Object visit(CallableParam callableParam) {
        return null;
    }
}
