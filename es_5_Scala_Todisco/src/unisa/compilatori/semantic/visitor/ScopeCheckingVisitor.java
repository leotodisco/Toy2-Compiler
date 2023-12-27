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

        System.out.println("PROGRAM OP " + table);


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
                                    //System.out.println("\n\n\nRECORD IN ITER \n\n");
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
                if(proc.getProcParamDeclList() == null) {
                    continue;
                }
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

        //this.table

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
            var tbl = this.table;
            this.table = funzione.getTable(); //entri nello scope
            funzione.getBody().accept(this);

            this.table = tbl;


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
        whileStat.setTable(new SymbolTable());
        SymbolTable symbolTable = whileStat.getTable();
        symbolTable.setScope("While");
        symbolTable.setFather(this.table);

        if(whileStat.getBody() != null) {
            this.table = whileStat.getTable();
            whileStat.getBody().accept(this);
        }

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

    java.util.function.Function<CallableParam, SymbolTableRecord> mapProcParamToEntry =  procParam -> {
        return new SymbolTableRecord(procParam.getId().getLessema(),
                procParam,
                new VarFieldType(procParam.getTipo().toString()),
                "placeholder");
    };

    @Override
    public Object visit(Procedure procedure) {
        //in questa parte si inizializza la symbol table di procedure
        procedure.setTable(new SymbolTable());
        SymbolTable procedureTable = procedure.getTable();
        procedureTable.setFather(this.table); //link
        procedureTable.setScope(procedure.getId().getLessema());

        //se sono presenti dei parametri si aggiungono allo scope
        if(procedure.getProcParamDeclList() != null) {
            procedure.getProcParamDeclList()
                    .stream()
                    .map(mapProcParamToEntry)
                    .forEach(symbolTableRecord -> {
                        try {
                            procedureTable.addEntry(symbolTableRecord);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        if(procedure.getBody()!=null) {
            var tbl = this.table;
            this.table = procedure.getTable();
            try {
                procedure.getBody().accept(this);
                this.table = tbl; //esce dallo socper
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    @Override
    public Object visit(ConstOP constOP) {
        String type = constOP.getType().toString();
        if(type.equals("INTEGER_CONST")) {
            return "integer";
        }
        if(type.equals("BOOLEAN_CONST")) {
            return "boolean";
        }
        if(type.equals("REAL_CONST")) {
            return "real";
        }
        if(type.equals("STRING_CONST")) {
            return "string";
        }

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
        if(exprOP instanceof UnaryOP) {
            var exp = (UnaryOP) exprOP;
            exp.accept(this);
        } else if(exprOP instanceof BinaryOP){
            var exp = (UnaryOP) exprOP;
            exp.accept(this);
        } else if(exprOP instanceof Identifier){
            var exp = (Identifier) exprOP;
            exp.accept(this);
        }

        return null;
    }

    @Override
    public Object visit(CallableParam callableParam) {
        return null;
    }

    @Override
    public Object visit(Stat s) {
        //qui si gestrisce dove deve andare
        if(s instanceof WhileStat) {
            var stat = (WhileStat) s;
            stat.accept(this);
        } else if(s instanceof IfStat) {
            var stat = (IfStat) s;
            stat.accept(this);
        } else if(s instanceof ElseIfOP) {
            var stat = (ElseIfOP) s;
            stat.accept(this);
        } else if(s instanceof ElseOP) {
            var stat = (ElseOP) s;
            stat.accept(this);
        }


        return null;
    }

    @Override
    public Object visit(Body body) {
        //se il body ha una lista di dichiarazioni non vuota
        //mettiamo nella symbol table le variabili
        if(body.getVarDeclList() != null) {
            ArrayList<SymbolTableRecord> listaVar;
            for (VarDecl var : body.getVarDeclList()) {
                listaVar = (ArrayList<SymbolTableRecord>) var.accept(this);
                for ( SymbolTableRecord record: listaVar) {
                    try{
                    table.addEntry(record);
                    } catch (Exception e) {
                        System.out.println("error table.addEntry()");
                    }
                }
            }

        }


        //per ogni statement si chiama la accept corretta
        if(body.getStatList() != null) {
            for(Stat s : body.getStatList()) {
                s.accept(this);
            }
        }
        
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
