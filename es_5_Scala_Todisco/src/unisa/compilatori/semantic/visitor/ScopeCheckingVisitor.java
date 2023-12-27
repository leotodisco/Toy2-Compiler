package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.symboltable.SymbolTableRecord;
import unisa.compilatori.semantic.symboltable.VarFieldType;

public class ScopeCheckingVisitor implements Visitor {
    private SymbolTable table;

    @Override
    public Object visit(ProgramOp program) {
            this.table = new SymbolTable();
        program.getIterWithoutProcedure().getFunctions().forEach(fun -> {
            try {
                fun.accept(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return null;
    }

    @Override
    public Object visit(BinaryOP operazioneBinaria) {
        operazioneBinaria.getExpr1().accept(this);
        operazioneBinaria.getExpr2().accept(this);

        return null;
    }

    @Override
    public Object visit(UnaryOP operazioneUnaria) {
        operazioneUnaria.accept(this);
        return null;
    }

    @Override
    public Object visit(VarDecl dichiarazione) {

        return null;
    }

    /**
     *
     *
     */
    java.util.function.Function<FunctionParam, SymbolTableRecord> mapperFunctionParamToEntry =  functionParam -> {
    return new SymbolTableRecord(functionParam.getId().getLessema(),
            functionParam,
            new VarFieldType(functionParam.getTipo().toString()),
            "placeholder");
};

    @Override
    public Object visit(Function funzione) throws Exception {
        funzione.setTable(new SymbolTable());
        SymbolTable funzioneTable = funzione.getTable();
        funzioneTable.setFather(this.table);
        funzioneTable.setScope(funzione.getId().getLessema());

        //se sono presenti dei parametri si aggiungono allo scope
        if(funzione.getParametersList() != null) {
            funzione.getParametersList()
                    .stream()
                    .map(mapperFunctionParamToEntry)
                    .forEach(symbolTableRecord -> {
                        try {
                            table.addEntry(symbolTableRecord);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            funzione.setTable(table);
        }

        if(funzione.getBody()!=null) {
            this.table = funzione.getTable();
            funzione.getBody().accept(this);
        }
        System.out.println("TABLE = " + funzione.getTable());
        return null;
    }

    @Override
    public Object visit(FunctionParam functionParam) {
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
    public Object visit(Type type) {
        return null;
    }

    @Override
    public Object visit(ProcParamDecl procParam) {
        return null;
    }



    @Override
    public Object visit(Decl decl) {
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
    public Object visit(Stat statement) {
        return null;
    }

    @Override
    public Object visit(Body body) {
        return null;
    }

    @Override
    public Object visit(Identifier id) {
        if(table.lookup(id.getLessema()).isEmpty()){
            //TODO CUSTOM EXCEPTION
        }
        return null;
    }
}
