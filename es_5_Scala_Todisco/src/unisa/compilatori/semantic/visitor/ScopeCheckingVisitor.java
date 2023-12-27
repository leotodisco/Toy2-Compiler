package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public class ScopeCheckingVisitor implements Visitor {
    private SymbolTable table;

    @Override
    public Object visit(ProgramOp program) {
        //inizializzo la tabella dei simboli alla ROOT
        table = new SymbolTable();
        table.setScope(SymbolTable.NAME_ROOT);
        table.setFather(null);

        program.getIterWithoutProcedure().accept(this);
        program.getProc().accept(this);
        program.getIterOp().accept(this);


        /*this.table = new SymbolTable();
        program.getIterWithoutProcedure().getFunctions().forEach(fun -> {
            try {
                fun.accept(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });*/

        return null;
    }

    @Override
    public Object visit(IterOp iterOP) {
        if(!iterOP.getFunctions().isEmpty()) {
            iterOP.getFunctions().forEach(function -> {
                String identificatore = function.getId().getLessema();
                CallableFieldType fieldType = new CallableFieldType();

                fieldType.setInputParams(function.getParametersList());

                SymbolTableRecord record = new SymbolTableRecord(identificatore, function, fieldType,"" /*TODO*/);

                try {
                    table.addEntry(record);
                    function.accept(this);
                } catch (Exception e) {
                    System.out.println("id di funzione già dichiarata");
                }
                
            });

        //su tutte le funzioni chiami accept
        }

        if(!iterOP.getDeclarations().isEmpty()) {
            //per ogni decl dobbiamo chiamare accept e addentry
            iterOP.getDeclarations()
                    .stream()
                    .forEach(varDecl -> varDecl.accept(this));

            for(VarDecl varDecl : iterOP.getDeclarations()) {
                for(Decl decl : varDecl.getDecls()) {
                   VarFieldType varFieldType = new VarFieldType(decl.getTipoDecl().toString());
                   //per ogni id fai un record, poi aggiungi tutti i record alla tabella
                    decl.getIds()
                            .parallelStream()
                            .map(id -> new SymbolTableRecord(id.getLessema(), id, varFieldType, "placeholder"))
                            .forEach(record -> {
                                try {
                                    table.addEntry(record);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });

                   //per ogni costante devo fare un record, poi aggiungi tutti i record alla tabella


                }
            }
        }

        if(!iterOP.getProcedures().isEmpty()) {
            //su tutte le procedure chiami accept
            iterOP.getProcedures()
                    .parallelStream()
                    .forEach(procedure -> procedure.accept(this));

            for(Procedure proc : iterOP.getProcedures()) {
                //prendi i parametri output della procedura
                var outParams = (ArrayList<CallableParam>) proc.getProcParamDeclList()
                                .stream()
                                .filter(callableParam -> callableParam.getId().getMode().toString().equals("PARAMSOUT"))
                                .toList();

                var inputParams = (ArrayList<CallableParam>) proc.getProcParamDeclList()
                        .stream()
                        .filter(callableParam -> !callableParam.getId().getMode().toString().equals("PARAMSOUT"))
                        .toList();

                var fieldType = new CallableFieldType(inputParams, outParams);
                SymbolTableRecord record = new SymbolTableRecord(proc.getId().getLessema(), proc, fieldType, "placeholder");

                try {
                    this.table.addEntry(record);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


            }

        }



        return null;
    }

    @Override
    public Object visit(IterWithoutProcedure iterWithoutProcedure) {

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
        ArrayList<SymbolTableRecord> listaVar = new ArrayList<>();

        for ( Decl vars : dichiarazione.getDecls()) {
            listaVar.addAll((ArrayList<SymbolTableRecord>)vars.accept(this));
        }

        return listaVar;
    }

    @Override
    public Object visit(Decl decl) {
        ArrayList<SymbolTableRecord> listaVar = new ArrayList<>();

        Iterator<ConstOP> itConst = decl.getConsts().iterator();
        Iterator<Identifier> itIds = decl.getIds().iterator();

        // se il tipo di dichiarazione è del tipo var a ^=2;\
        if(decl.getTipoDecl().equals(Decl.TipoDecl.ASSIGN)) {
            while(itConst.hasNext() && itIds.hasNext()) {
                Identifier id = itIds.next();
                ConstOP costante = itConst.next();
                listaVar.add(new SymbolTableRecord(id.getLessema(), decl, new VarFieldType(costante.getType().toString()), costante.getLessema()));
            }
        // se il tipo di dichiarazione è del tipo var a : string;\
        } else {
            while(itIds.hasNext()) {
                Identifier id = itIds.next();
                ConstOP costante = itConst.next();
                listaVar.add(new SymbolTableRecord(id.getLessema(), decl, new VarFieldType(decl.getTipo().toString()), ""));
            }
        }


        return listaVar;
    }

    /**
     *
     *
     */
    java.util.function.Function<CallableParam, SymbolTableRecord> mapperFunctionParamToEntry =  functionParam -> {
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
                            funzioneTable.addEntry(symbolTableRecord);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            //funzione.setTable(table);
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

        //set up della symbol table
        ifStat.setSymbolTableThen(new SymbolTable());
        SymbolTable symbolTableThen = ifStat.getSymbolTableThen();
        symbolTableThen.setScope("IF-THEN");
        symbolTableThen.setFather(this.table);

        //Symbol Table THEN
        if(ifStat.getBody()!=null) {
            this.table = ifStat.getSymbolTableThen();
            ifStat.getBody().accept(this);
        }

        //chiamo il visitor sulla lista di elseif
        ifStat.getElseIfOPList().stream().forEach(elseIfOP -> elseIfOP.accept(this));

        //chiamo il visitor su else
        ifStat.getElseOP().accept(this);

        return null;
    }

    @Override
    public Object visit(ElseOP elseOP) {
        //set up della symbol table
        elseOP.setSymbolTableElseOp(new SymbolTable());
        SymbolTable symbolTableThen = elseOP.getSymbolTableElseOp();
        symbolTableThen.setScope("IF-ELSE");
        symbolTableThen.setFather(this.table);

        if(elseOP.getBody()!=null) {
            this.table = elseOP.getSymbolTableElseOp();
            elseOP.getBody().accept(this);
        }

        return null;
    }

    @Override
    public Object visit(ElseIfOP elseIfOP) {

        //set up della symbol table
        elseIfOP.setSymbolTableElseIF(new SymbolTable());
        SymbolTable symbolTableThen = elseIfOP.getSymbolTableElseIF();
        symbolTableThen.setScope("IF-ELIF");
        symbolTableThen.setFather(this.table);

        if(elseIfOP.getBody()!=null) {
            this.table = elseIfOP.getSymbolTableElseIF();
            elseIfOP.getBody().accept(this);
        }

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
