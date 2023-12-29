package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.*;
import unisa.compilatori.utils.Exceptions;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ScopeCheckingVisitor implements Visitor {
    private SymbolTable table;
    private Stack<SymbolTable> TempSymbolTable = new Stack<>();

    public void enterScope(SymbolTable scope) {
        TempSymbolTable.push(this.table);
        this.table = scope;
    }

    public void exitScope() {
        this.table = TempSymbolTable.pop();
    }
    @Override
    public Object visit(ProgramOp program) {
        //inizializzo la tabella dei simboli alla ROOT
        table = new SymbolTable();
        table.setScope(SymbolTable.NAME_ROOT);
        table.setFather(null);

        program.getIterWithoutProcedure().accept(this);

        Procedure proc = program.getProc();

        CallableFieldType fieldTypeProc = new CallableFieldType();

        var fieldType = new CallableFieldType(proc.getProcParamDeclList());

        SymbolTableRecord recordProc = new SymbolTableRecord(proc.getId().getLessema(), proc, fieldType, "");
        try {
            table.addEntry(recordProc);
        } catch (Exception e) {
            e.printStackTrace();
        }


        program.getProc().accept(this);
        program.getIterOp().accept(this);

        return null;
    }

    /**
     * Metodo che permette di trasformare una lista di tipi in una Stringa
     * @param lista
     * @return
     */
    private String returnTypeListToString(ArrayList<Type> lista) {
        StringBuilder res = new StringBuilder();
        for (Type tipo : lista) {
            res.append(tipo.getTipo() + ";");
        }

        return res.toString();
    }

    @Override
    public Object visit(IterWithoutProcedure iterOP) {
        if(!iterOP.getFunctions().isEmpty()) {
            iterOP.getFunctions().forEach(function -> {
                String identificatore = function.getId().getLessema();
                CallableFieldType fieldType = new CallableFieldType();

                fieldType.setParams(function.getParametersList());

                SymbolTableRecord record = new SymbolTableRecord(identificatore, function, fieldType, returnTypeListToString(function.getReturnTypes()));

                try {
                    table.addEntry(record);
                    function.accept(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }

        if(!iterOP.getDeclarations().isEmpty()) {
            //per ogni decl dobbiamo chiamare accept e addentry
            iterOP.getDeclarations()
                    .stream()
                    .forEach(varDecl -> varDecl.accept(this));

            for(VarDecl varDecl : iterOP.getDeclarations()) {
                for(Decl decl : varDecl.getDecls()) {
                    VarFieldType varFieldType = new VarFieldType(decl.getTipo().getTipo());
                    //per ogni id fai un record, poi aggiungi tutti i record alla tabella
                    decl.getIds()
                            .stream()
                            .map(id -> new SymbolTableRecord(id.getLessema(), id, varFieldType, ""))
                            .forEach(record -> {
                                try {
                                    //System.out.println("\n\n\nRECORD IN ITER \n\n");
                                    table.addEntry(record);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });

                }
            }
        }


        return null;
    }

    @Override
    public Object visit(IterOp iterOP) {
        if(!iterOP.getFunctions().isEmpty()) {
            iterOP.getFunctions().forEach(function -> {
                String identificatore = function.getId().getLessema();
                CallableFieldType fieldType = new CallableFieldType();

                fieldType.setParams(function.getParametersList());

                SymbolTableRecord record = new SymbolTableRecord(identificatore, function, fieldType,returnTypeListToString(function.getReturnTypes()));

                try {
                    table.addEntry(record);
                    function.accept(this);
                } catch (Exception e) {
                    e.printStackTrace();
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
                   VarFieldType varFieldType = new VarFieldType(decl.getTipo().getTipo());
                   //per ogni id fai un record, poi aggiungi tutti i record alla tabella
                    decl.getIds()
                            .stream()
                            .map(id -> new SymbolTableRecord(id.getLessema(), id, varFieldType, ""))
                            .forEach(record -> {
                                try {
                                    //System.out.println("\n\n\nRECORD IN ITER \n\n");
                                    table.addEntry(record);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });

                   //per ogni costante devo fare un record, poi aggiungi tutti i record alla tabella


                }
            }
        }

        if(!iterOP.getProcedures().isEmpty()) {
            //su tutte le procedure chiami accept
            iterOP.getProcedures()
                    .stream()
                    .forEach(procedure -> procedure.accept(this));

            for(Procedure proc : iterOP.getProcedures()) {

                var fieldType = new CallableFieldType(proc.getProcParamDeclList());
                SymbolTableRecord record = new SymbolTableRecord(proc.getId().getLessema(), proc, fieldType, "");

                try {
                    this.table.addEntry(record);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    public Object visit(BinaryOP operazioneBinaria) {
        try {

            operazioneBinaria.getExpr1().accept(this);
            operazioneBinaria.getExpr2().accept(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        Iterator<ConstOP> itConst;
        Iterator<Identifier> itIds;

        // se il tipo di dichiarazione è del tipo var a ^=2;\
        if(decl.getTipoDecl().equals(Decl.TipoDecl.ASSIGN)) {
            itConst = decl.getConsts().iterator();
            itIds = decl.getIds().iterator();
            while(itConst.hasNext() && itIds.hasNext()) {
                Identifier id = itIds.next();
                ConstOP costante = itConst.next();
                int lunghezzaStringa = costante.getType().toString().length();
                var substring = costante.getType().toString();
                listaVar.add(new SymbolTableRecord(id.getLessema(), decl, new VarFieldType(substring), costante.getLessema()));
            }
        // se il tipo di dichiarazione è del tipo var a : string;\
        } else {
            itIds = decl.getIds().iterator();
            while(itIds.hasNext()) {
                Identifier id = itIds.next();
                //System.out.println("nel vecchio e caro scope il lexema = " + id.getLessema() + "= " + decl.getTipo().getTipo());
                listaVar.add(new SymbolTableRecord(id.getLessema(), decl, new VarFieldType(decl.getTipo().getTipo()), ""));
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
            new VarFieldType(functionParam.getTipo().getTipo().toString()),
            "");
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
        }

        System.out.println(funzioneTable);
        if(funzione.getBody()!=null) {
            enterScope(funzione.getTable());
            funzione.getBody().accept(this);
            exitScope();
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
        System.out.println("ciao");
        //set up della symbol table
        ifStat.setSymbolTableThen(new SymbolTable());
        SymbolTable symbolTableThen = ifStat.getSymbolTableThen();
        symbolTableThen.setScope("IF-THEN");
        symbolTableThen.setFather(this.table);


        //Symbol Table THEN
        if(ifStat.getBody()!=null) {
            enterScope(ifStat.getSymbolTableThen());
            ifStat.getBody().accept(this);
            exitScope();
        }

        if(!ifStat.getElseIfOPList().isEmpty()) {
            for (ElseIfOP elseIfOP : ifStat.getElseIfOPList() ) {
                //set up della symbol table

                elseIfOP.setSymbolTableElseIF(new SymbolTable());
                SymbolTable symbolTableElseIf = elseIfOP.getSymbolTableElseIF();
                symbolTableElseIf.setScope("IF-ELIF");
                symbolTableElseIf.setFather(this.table);

                this.enterScope(elseIfOP.getSymbolTableElseIF());
                elseIfOP.accept(this);
                this.exitScope();
            }
        }


        if(ifStat.getElseOP()==null)
            return null;

        //set up della symbol table
        ifStat.getElseOP().setSymbolTableElseOp(new SymbolTable());
        SymbolTable symbolTableElse = ifStat.getElseOP().getSymbolTableElseOp();
        symbolTableElse.setScope("IF-ELSE");
        symbolTableElse.setFather(this.table);

        //chiamo il visitor su else
        enterScope(symbolTableElse);
        ifStat.getElseOP().accept(this);
        exitScope();

        return null;
    }

    @Override
    public Object visit(ElseOP elseOP) {


        if(elseOP.getBody()!=null) {
            elseOP.getBody().accept(this);
        }

        return null;
    }

    @Override
    public Object visit(ElseIfOP elseIfOP) {

        if(elseIfOP.getBody() != null) {
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
            enterScope(whileStat.getTable());
            whileStat.getBody().accept(this);
            exitScope();
        }

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

    java.util.function.Function<CallableParam, SymbolTableRecord> mapProcParamToEntry =  param -> {
        return new SymbolTableRecord(param.getId().getLessema(),
                param,
                new VarFieldType(param.getTipo().getTipo().toString()),
                "");
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
                            e.printStackTrace();
                        }
                    });
        }

        System.out.println(procedureTable);
        if(procedure.getBody() != null) {
            try {
                enterScope(procedure.getTable()); //entro nello scope
                procedure.getBody().accept(this);
                exitScope(); //esce dallo scope
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    @Override
    public Object visit(ConstOP constOP) {
        String type = constOP.getType().toString();
        System.out.println("IL TIPO DI COSTANTE " + type);

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
                        e.printStackTrace();
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
