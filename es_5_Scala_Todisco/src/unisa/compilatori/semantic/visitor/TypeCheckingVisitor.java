package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.CallableFieldType;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.symboltable.SymbolTableRecord;
import unisa.compilatori.semantic.symboltable.VarFieldType;
import unisa.compilatori.utils.Exceptions;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class TypeCheckingVisitor implements Visitor {

    private SymbolTable currentScope;

    public void enterScope(SymbolTable scope) {
        this.currentScope= scope;
    }

    public void exitScope() {
        this.currentScope = (SymbolTable) this.currentScope.getFather();
    }

    /**
     * Metodo che dati due tipi e un'operazione restituisce il tipo del risultato
     * per le binary expr
     * @param type1
     * @param type2
     * @param op
     * @return
     */
    private String evaluateType(String type1, String type2, String op) throws Exception {
        switch (op){
            case "plus_op", "times_op", "div_op", "minus_op":
                if (type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("INTEGER"))
                    return "integer";
                else if (type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("REAL"))
                    return new String("REAL");
                else if (type1.equalsIgnoreCase("REAL") && type2.equalsIgnoreCase("INTEGER"))
                    return new String("REAL");
                else if (type1.equalsIgnoreCase("REAL") && type2.equalsIgnoreCase("REAL"))
                    return new String("REAL");
                else
                    throw new Exception("errore di tipo nella evaluate type");

            case "OR", "AND":
                if(type1.equalsIgnoreCase("BOOLEAN") && type2.equalsIgnoreCase("BOOLEAN"))
                    return new String("bool");
                else
                    throw new Exception("errore");

            case "stringConcat":
                if(type1.equals("STRING_CONST") && type2.equals("STRING_CONST"))
                    return new String("STRING_CONST");
                else
                    throw new Exception("errore");

            case "gt_op", "ge_op", "lt_op", "le_op":
                if(type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("INTEGER"))
                    return new String("BOOLEAN");
                else if(type1.equalsIgnoreCase("REAL") && type2.equals("INTEGER"))
                    return new String("BOOLEAN");
                else if(type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("REAL"))
                    return new String("BOOLEAN");
                else if(type1.equalsIgnoreCase("REAL") && type2.equalsIgnoreCase("REAL"))
                    return new String("BOOLEAN");
                else
                    throw new Exception("errore 11111");

            case "eq", "ne":
                if(type1.equals(type2))
                    return new String("BOOLEAN");
                else
                    throw new Exception("errore");
        }
        return null;
    }

    /**
     * Overloading per le unary expressions
     * @param type1 è il tipo dell'id dell'operazione unaria
     * @param op è l'operazione
     * @return
     * @throws Exception
     */
    private String evaluateType(String type1, String op) throws Exception {
        switch(op) {
            case "UMINUS":
                if(type1.equals("INTEGER_CONST")) {
                    return "INTEGER_CONST";
                }
                else if(type1.equals("REAL_CONST")) {
                    return "REAL_CONST";
                }
                else
                    throw new Exception("TIPO NON COMPATIBILE"); //TODO CUSTOM EXCEPTION
            case "NOT":
                if(type1.equals("BOOLEAN_CONST")) {
                    return "BOOLEAN_CONST";
                }
                else
                    throw new Exception("TIPO NON COMPATIBILE"); //TODO CUSTOM EXCEPTION

            default:
                throw new Exception("ERRORE COI TIPI"); //TODO custom exception
        }
    }



    @Override
    public Object visit(ProgramOp program) {
        currentScope = program.getTable();

        program.getIterWithoutProcedure().accept(this);

        program.getProc().accept(this);

        program.getIterOp().accept(this);

        return null;
    }

    @Override
    public Object visit(BinaryOP operazioneBinaria) {
        //faccio il lookup di expr1
        String typeExpr1 = "";
        try{
            typeExpr1 = (String) operazioneBinaria.getExpr1().accept(this);
            System.out.println("EXPR1 = " + typeExpr1);
        }catch(Exception e){
            e.printStackTrace();
        }
        //faccio il lookup di expr2
        String typeExpr2 = "";
        try {
            typeExpr2 = (String) operazioneBinaria.getExpr2().accept(this);
        } catch(Exception e){
            e.printStackTrace();
        }

        //faccio il lookup dell'operazione
        String typeOp = operazioneBinaria.getName();
        String risultato = "";
        try {
            //controllo di che tipo l'operazione binaria
            risultato = evaluateType(typeExpr1, typeExpr2, typeOp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //restituisco il tipo dell'operazione binaria
        return risultato;
    }

    @Override
    public Object visit(UnaryOP operazioneUnaria) {
        String expr1 = "";
        try {
            expr1 = (String) operazioneUnaria.getExpr().accept(this);
        } catch(Exception e) {
            e.printStackTrace();
        }
        String expName = operazioneUnaria.getSimbolo(); //TODO accertarsi che UMINUS funziona altrimenti metti MINUS
        String risultato = "";
        try {
            risultato = evaluateType(expr1, expName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return risultato;
    }


    @Override
    public Object visit(VarDecl dichiarazione) {
        for(Decl vars : dichiarazione.getDecls()) {
            vars.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(Function funzione) throws Exception {
        ArrayList<String> tipiDichiarati;
        ArrayList<String> tipiRestituiti = null;

        //in tipi dichiarati ho delle stringhe con i tipi dei parametri dichiarati
        tipiDichiarati = funzione.getReturnTypes()
                .stream()
                .map(type -> type.toString())
                .collect(Collectors.toCollection(ArrayList::new));


        //prendo i tipi effettivamente restituiti
        for(Stat stmt: funzione.getBody().getStatList()) {
            if (stmt.getTipo().equals(Stat.Mode.RETURN)){
                tipiRestituiti = stmt
                        .getEspressioniList()
                        .stream()
                        .map(stat -> {
                            try {
                                return (String) stat.accept(this); //TODO assicurarsi che questo return non causa problemi
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toCollection(ArrayList<String>::new));
                //c'è solo un return in tutto il body della funzione
                break;
            }
        }

        //controllo sulla lunghezza dei tipi dichiarati e restituiti
        if(tipiDichiarati.size() != tipiRestituiti.size()){
            throw new RuntimeException("I tipi dichiarati ed effettivamente restituiti non matchano nella funzione");
        }

        //controllo tipo per tipo se corrispondono i tipi dichiarati con quelli restituiti
        //TODO POTREBBE NON FUNZIONARE PER VIA DELL'ORDINE IN CUI SONO MESSI I TIPI DICHAIRATI E RESTITUITI
        Iterator<String> itTipiDichiarati = tipiDichiarati.iterator();
        Iterator<String> itTipiRestituiti = tipiRestituiti.iterator();

        while(itTipiDichiarati.hasNext() && itTipiRestituiti.hasNext()) {
            String tipoRestituito = itTipiRestituiti.next();
            String tipoDichiarato = itTipiDichiarati.next();
            if(!tipoDichiarato.equals(tipoRestituito)){
                throw new RuntimeException("I tipi dichiarati ed effettivamente restituiti non matchano nella funzione");
            }
        }

        //controlli sul body della funzione
        enterScope(funzione.getTable());
        funzione.getBody().accept(this);
        exitScope();

        return null;
    }

    @Override
    public Object visit(Stat statement) {
        if (statement instanceof WhileStat) {
            ((WhileStat) statement).accept(this);
        }
        if(statement instanceof IfStat) {
            ((WhileStat) statement).accept(this);
        }
        if(statement instanceof IOArgsOp) {
            ((IOArgsOp) statement).accept(this);
        }
        if(statement instanceof ProcCall) {
            ((ProcCall) statement).accept(this);
        }
        if(statement instanceof IOArgsOp) {
            ((IOArgsOp) statement).accept(this);
        }

        return null;
    }

    @Override
    public Object visit(IfStat ifStat) {
        String tipoExpr = "";
        try {
            tipoExpr = (String) ifStat.getExpr().accept(this);
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (!tipoExpr.equalsIgnoreCase("boolean")) {
            //TODO ECCEZZIONE;
            throw new RuntimeException("tipo condizione non valido");
        }

        //Controllo nel body di if stat
        //entro nello scope del body di ifstat
        enterScope(ifStat.getSymbolTableThen());
        ifStat.getBody().accept(this);
        //esco dallo scope del body di ifStat
        exitScope();

        //controllo sugli eventuali else if
        if(!ifStat.getElseIfOPList().isEmpty()){
            for(ElseIfOP elseIfOP : ifStat.getElseIfOPList()) {
                //controllo l'else if
                elseIfOP.accept(this);
            }
        }

        //controllo sull'eventuale else
        if(ifStat.getElseOP() != null) {
            //controllo l'else
            ifStat.getElseOP().accept(this);
        }

        return null;
    }

    @Override
    public Object visit(ElseOP elseOP) {

        //entro nello scope dell'else op
        enterScope(elseOP.getSymbolTableElseOp());
        //controllo il body dell'else
        elseOP.getBody().accept(this);
        //esco dallo scope dell'else
        exitScope();
        return null;
    }

    @Override
    public Object visit(ElseIfOP elseIfOP) {
        String tipoExpr = "";
        //controllo sulla condizione
        try {
            tipoExpr = (String) elseIfOP.getExpr().accept(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!tipoExpr.equals("boolean"))
            throw new RuntimeException("condizione nell'expr dell'else if errata");//TODO ECCEZIONE CUSTOM

        enterScope(elseIfOP.getSymbolTableElseIF());
        elseIfOP.getBody().accept(this);
        exitScope();

        return null;
    }

    @Override
    public Object visit(ProcCall procCall) throws Exception {

        SymbolTableRecord record = null;
        try {
            currentScope.lookup(procCall.getIdentifier().getLessema());

            record = currentScope
                    .lookup(procCall.getIdentifier().getLessema())
                    .orElseThrow(() -> new Exceptions.NoDeclarationError(procCall.getIdentifier().getLessema()));

        } catch (Exception e){
            e.printStackTrace();
        }

        //mi assicuro che il numero di parametri input della chiamata e della dichiarazione sono uguali
        ArrayList<String> tipiDichiaratiOut;
        ArrayList<String> tipiParametriRef = new ArrayList<>();
        CallableFieldType fieldType = (CallableFieldType) record.getFieldType();
        ArrayList<CallableParam> inputParams =  fieldType.getInputParams();
        ArrayList<CallableParam> outputParams = fieldType.getOutputParams();
        ArrayList<ExprOP> listaParametriNellaChiamata = (ArrayList<ExprOP>) procCall.getExprs().clone();



        int length = procCall.getExprs().size();
        for(int i = 0; i < length; i++) {
            ExprOP exprOP = procCall.getExprs().get(i);
            if (exprOP instanceof Identifier) {
                Identifier exId = (Identifier) exprOP;

                /**RICORDA: funziona assumendo che tutto sia posizionale, quando troviamo
                 * il primo inputParam sappiamo che abbiamo già attraversato tutti
                 * gli output param: ecco perche facciamo i - inputParams.size()
                 * */
                CallableParam param = i >= inputParams.size() ? outputParams.get(i - inputParams.size()) : inputParams.get(i);

                var tipoId = ((VarFieldType) currentScope.lookup(exId.getLessema())
                        .orElseThrow(() -> new Exceptions.NoDeclarationError(exId.getLessema()))
                        .getFieldType()).getType();
                if (!tipoId.equals(param.getTipo().getTipo()))
                    throw new RuntimeException("Tipi non matchano");

            } else {
                CallableParam parametroInDichiarazione = inputParams.get(i);
                ExprOP parametroInChiamata = listaParametriNellaChiamata.get(length-i-1);

                String tipoCallableParam = parametroInDichiarazione.getTipo().getTipo();
                String tipoExpr = (String) parametroInChiamata.accept(this);

                //controlla i tipi
                if(!tipoCallableParam.equals(tipoExpr)) {
                    throw new Exception("I TIPI NON MATCHANO"); //TODO CUSTOM EXCEPTION
                }
            }
        }





        return null;
    }

    @Override
    public Object visit(WhileStat whileStat) throws Exception {
        String condition = (String) whileStat.getExpr().accept(this);

        if( !condition.equalsIgnoreCase("boolean")){
            //TODO ECCEZIONE
            throw new Exception("Errore, tipo della condizione in while stat non corretto");
        }

        enterScope(whileStat.getTable());
        whileStat.getBody().accept(this);
        exitScope();
        return null;
    }

    /**
     * Delega al giusto visit
     * @param ioArgsOp
     * @return
     */
    @Override
    public Object visit(IOArgsOp ioArgsOp) {
        var expressionsList = ioArgsOp.getEspressioniList();
        for(ExprOP exp : expressionsList) {
            try {
                exp.accept(this);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        var ioArgsExpr = ioArgsOp.getListaIOArgsExpr();
        for(IOArgsExpr exp : ioArgsExpr) {
            try {
                exp.accept(this);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public Object visit(Body body) {

        //se il body ha delle dichiarazioni di variabili, controllale
        if(body.getVarDeclList()!=null) {
            body.getVarDeclList().forEach(var -> var.accept(this));
        }

        //se il body ha degli statement, controllali
        if(body.getStatList()!=null) {
            body.getStatList().forEach(stat -> stat.accept(this));
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

    @Override
    public Object visit(FunctionParam functionParam) {
        return null;
    }

    /**
     * Qui si controlla che il tipo matcha col valore effettivo
     * @param decl dichiarazione che contiene vari id e un tipo.
     * @return
     */
    @Override
    public Object visit(Decl decl) {
        var tipo = decl.getTipo().getTipo();

        if(decl.getTipoDecl().toString().equals("ASSIGN")) {
            if (!decl.getConsts().isEmpty()) {
                //per ogni costante si vede se matcha col tipo.
                decl.getConsts()
                        .stream()
                        .map(ConstOP::getType)
                        .forEach(kind -> {
                            var len = "_CONST".length();
                            //qui trasformo l'enum nella stringa senza "_CONST" per fare la compare col tipo
                            var kindToString = kind.toString().substring(0, kind.toString().length()-len);
                            if (!kindToString.equals(tipo)) {
                                try {
                                    throw new Exception("TIPI NON MATCHANO"); //TODO custom exception
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        }


        return null;
    }


    /**
     * In questo metodo si vede nella tabella dei simboli se l'id è stato dichiarato
     * se non è stato dichiarato si lancia l'eccezione.
     *
     * @param id
     * @return
     */
    @Override
    public Object visit(Identifier id) {
        SymbolTableRecord record = new SymbolTableRecord();

        try{
        record = currentScope
                .lookup(id.getLessema())
                .orElseThrow(() -> new Exceptions.NoDeclarationError(id.getLessema()));
        } catch(Exception e) {
            e.printStackTrace();
        }

        var varFieldType = (VarFieldType) record.getFieldType();
        return varFieldType.getType();

    }

    @Override
    public Object visit(IterOp iterOP) {
        iterOP.getProcedures().forEach(procedure -> procedure.accept(this));

        iterOP.getDeclarations().forEach(s->s.accept(this));
            iterOP.getFunctions().forEach(s -> {
                try {
                    s.accept(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        return null;
    }

    @Override
    public Object visit(IterWithoutProcedure iterWithoutProcedure) {
        return null;
    }

    @Override
    public Object visit(Procedure procedure) {

        //entro nello scope di procedure
        enterScope(procedure.getTable());
        //controllo sul body di procedure
        procedure.getBody().accept(this);
        //esco dallo scope di procedure
        exitScope();

        return null;
    }

    /**
     *
     * @param constOP
     * @return il tipo della costante senza "_CONST"
     */
    @Override
    public Object visit(ConstOP constOP) {

        int len = "_CONST".length();
        var typeAsString = constOP.getType().toString();
        //Prendo solo la parte che mi interessa di type ossia quella senza "_CONST"
        String type = typeAsString.substring(0, typeAsString.length()-len);


        return type;
    }

    /**
     * Qui si controlla che il numero di parametri della funcall
     * coincide col numero di parametri nella tabella dei simboli.
     * Si controlla che il tipo dei parametri sia uguale al
     * tipo di parametri nella tabella dei simboli.
     *
     * @param funCall
     * @return
     */
    @Override
    public Object visit(FunCall funCall) throws Exception {
        //1. controlllo il numero di parametri se coincide con quello nella table
        //se record è null vuol dire che la funzione non è mai stata dichiarata
        SymbolTableRecord record;
        record = currentScope
                .lookup(funCall.getIdentifier().getLessema())
                .orElseThrow(() -> new Exceptions.NoDeclarationError(funCall.getIdentifier().getLessema()));


        var fieldType = (CallableFieldType) record.getFieldType();

        var listaParametriNellaChiamata = funCall.getExprs();
        var listaParametriDichiarazione = fieldType.getInputParams();

        var nParamsChiamata = listaParametriNellaChiamata.size();
        var nParamsDichiarati = listaParametriDichiarazione.size();

        var isEqual = (nParamsChiamata == nParamsDichiarati) ? true : false ;

        if(!isEqual) {
            throw new Exception("NUMERO DI PARAMETRI DIVERSO DALLA DECL"); //TODO CUSTOM EXC
        }

        //2. per ogni expr controllo che il tipo sia uguale a quello nella decl
        for(int i = 0; i < nParamsChiamata; i++) {
            //se l'i-esimo parametro nella chiamata non ha lo stesso tipo
            //dell'i-esimo parametro nella dichiarazione
            //fermati e throw exception
            CallableParam parametroInDichiarazione = listaParametriDichiarazione.get(i);
            ExprOP parametroInChiamata = listaParametriNellaChiamata.get(i);

            String tipoCallableParam = parametroInDichiarazione.getTipo().getTipo();
            String tipoExpr = (String) parametroInChiamata.accept(this);

           //controlla i tipi
            if(!tipoCallableParam.equals(tipoExpr)) {
                throw new Exception("I TIPI NON MATCHANO"); //TODO CUSTOM EXCEPTION
            }

        }

        return null;
    }

    /**
     * Si controlla che
     * @param ioArgsExpr
     * @return
     */
    @Override
    public Object visit(IOArgsExpr ioArgsExpr) {

        return null;
    }

    /**
     * Capisce se è una unary o una BinaryExpr
     * o se è una Const
     * e delega al giusto figlio.
     * @param exprOP
     * @return
     */
    @Override
    public Object visit(ExprOP exprOP) {
        if(exprOP instanceof ConstOP) {
            return ((ConstOP) exprOP).accept(this);
        }
        else if(exprOP instanceof BinaryOP) {
            return ((BinaryOP) exprOP).accept(this);
        }
        else if(exprOP instanceof Identifier) {
            return ((Identifier) exprOP).accept(this);
        }
        else if(exprOP instanceof UnaryOP) {
            return ((UnaryOP) exprOP).accept(this);
        }
        else if(exprOP instanceof IOArgsExpr) {
            return ((IOArgsExpr) exprOP).accept(this);
        }
        else if(exprOP instanceof FunCall) {
            try {
                return ((FunCall) exprOP).accept(this);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public Object visit(CallableParam callableParam) {
        return null;
    }
}
