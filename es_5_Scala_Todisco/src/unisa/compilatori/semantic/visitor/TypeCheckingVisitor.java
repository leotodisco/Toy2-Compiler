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

import java.util.stream.Collectors;
public class TypeCheckingVisitor implements Visitor {

    private SymbolTable currentScope;
    //TODO fare che nelle funzioni i parametri sono immutable e non puoi assegnargli un nuovo valore

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
                else if (type1.equalsIgnoreCase("STRING") && type2.equalsIgnoreCase("STRING"))
                    return new String("STRING");
                else if (type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("STRING"))
                    return new String("STRING");
                else if (type1.equalsIgnoreCase("STRING") && type2.equalsIgnoreCase("INTEGER"))
                    return new String("STRING");
                else if (type1.equalsIgnoreCase("STRING") && type2.equalsIgnoreCase("REAL"))
                    return new String("STRING");
                else if (type1.equalsIgnoreCase("REAL") && type2.equalsIgnoreCase("STRING"))
                    return new String("STRING");
                else {
                    throw new Exception("errore di tipo nella evaluate type, type1 = " + type1 + " type2 = " + type2);
                }

            case "OR", "AND":
                if(type1.equalsIgnoreCase("BOOLEAN") && type2.equalsIgnoreCase("BOOLEAN"))
                    return new String("bool");
                else
                    throw new Exception("errore");

            case "stringConcat":
                if(type1.equalsIgnoreCase("STRING") && type2.equalsIgnoreCase("STRING")) {
                    return new String("STRING");
                }
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

            case "eq_op", "ne_op":
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
                if(type1.equals("INTEGER")) {
                    return "INTEGER";
                }
                else if(type1.equals("REAL")) {
                    return "REAL";
                }
                else
                    throw new Exceptions.InvalidOperation(op, type1);
            case "NOT":
                if(type1.equals("BOOLEAN")) {
                    return "BOOLEAN";
                }
                else
                    throw new Exceptions.InvalidOperation(op, type1);
            default:
                throw new Exception("ERRORE COI TIPI");
        }
    }



    @Override
    public Object visit(ProgramOp program) {
        currentScope = program.getTable();

        //controllo che ci sia uno e un solo main
        try {
            SymbolTableRecord main = currentScope.lookup("main").orElseThrow(Exceptions.LackOfMain::new);
            // controllo che sia una procedura e non una funzione
            if ( main.getNodo() instanceof Function) {
                throw new Exception("Il main è una funzione");
            }
            //TODO Dobbiamo controllare se il main ha dei parametri?
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        program.getIterWithoutProcedure().accept(this);

        program.getProc().accept(this);

        program.getIterOp().accept(this);

        return null;
    }

    @Override
    public Object visit(BinaryOP operazioneBinaria) {
        //faccio il lookup di expr1
        String typeExpr1 = (String) operazioneBinaria.getExpr1().accept(this);
        //faccio il lookup di expr2
        String typeExpr2 = "";
        try {
            typeExpr2 = (String) operazioneBinaria.getExpr2().accept(this);
        } catch(Exception e){
            e.printStackTrace();
        }

        //faccio il lookup dell'operazione
        String typeOp = operazioneBinaria.getName();
        //controllo di che tipo l'operazione binaria
        String risultato = risultato = evaluateType(typeExpr1, typeExpr2, typeOp);

        return risultato;
    }


    @Override
    public Object visit(UnaryOP operazioneUnaria) {
        String tipoExpr1 = (String) operazioneUnaria.getExpr().accept(this);
        String expName = operazioneUnaria.getSimbolo();//TODO accertarsi che UMINUS funziona altrimenti metti MINUS
        String risultato = "";
        risultato = evaluateType(tipoExpr1, expName);

        return risultato;
    }


    @Override
    public Object visit(VarDecl dichiarazione) {
        for(Decl vars : dichiarazione.getDecls()) {
            vars.accept(this);
        }
        return null;
    }

    /**
     * In questo metodo ricorsivo si ottengono tutti i return di un body.
     * @param body indica il body da esaminare
     * @param listaReturn tail-recursion, il metodo viene invocato con questa lista vuota
     *                   e al termine avrà tutti gli statement di tipo return
     * @return void perchè usiamo la tail recursion
     */
    private void getAllFunctionReturns(Body body, ArrayList<Stat> listaReturn) {
        if(body==null || body.getStatList().isEmpty()) {
            return;
        }
        else{
            ArrayList<Stat> listaStatements = body.getStatList();
            for(Stat statement : listaStatements) {
                if(statement instanceof WhileStat) {
                    getAllFunctionReturns(((WhileStat) statement).getBody(), listaReturn);
                }
                else if(statement instanceof IfStat) {
                    IfStat ifStat = (IfStat) statement;
                    getAllFunctionReturns((ifStat).getBody(), listaReturn);
                    getAllFunctionReturns(ifStat.getElseOP().getBody(), listaReturn);
                    ifStat.getElseIfOPList().forEach(elseIfOP -> getAllFunctionReturns(elseIfOP.getBody(), listaReturn));
                }
                else if (statement.getTipo().equals(Stat.Mode.RETURN)){
                    listaReturn.add(statement);
                }

            }
        }
    }

    /**
     * Questo metodo ci dice se i parametri di una funzione sono stati usati in modo illegale, ossia se vengono cambiati.
     * @param bodyStatements
     * @param paramsFunzione
     * @return True se i parametri sono usati in modo illegale
     * @return False se i parametri sono usati in modo corretto
     */
    private Boolean controlloSugliAssign(ArrayList<Stat> bodyStatements, ArrayList<CallableParam> paramsFunzione) {
        //1. itero su tutti gli statement
        //2. trovo uno statement di tipo assign
        //3. controllo che al lato sinistro di una assign non ci sia un parametro della funzione
        Boolean modificatoMutable = false;

        //mi ricavo gli statement di tipo assign
        List<Stat> statsAssign = bodyStatements
                .stream()
                .filter(stat -> stat.getTipo().equals(Stat.Mode.ASSIGN))
                .toList();

        //mi ricavo gli ids utilizzati nei parametri della funzione
        List<Identifier> statAssign = paramsFunzione.stream().map(callableParam -> callableParam.getId()).toList();


        //controllo che ogni statement assign non utilizzi un parametro immutable
        for (Stat stat : statsAssign) {
            for(Identifier id : stat.getIdsList()) {
                modificatoMutable = statAssign.stream().filter( idParamFunzione -> id.equals(idParamFunzione)).findAny().isEmpty();
            }
        }

        return modificatoMutable;
    }

    /**
     * Metodo che dice se il body è vuoto
     * @param body
     * @return true se il body è vuoto
     * @return se il body non è vuoto
     */
    private Boolean checkEmptyBody(Body body) {
        return (body.getStatList()==null || body.getStatList().isEmpty());
    }

    /**
     * Metodo che ci dice se il body non ha almeno un return.
     * @param body
     * @return true se il body non ha return
     * @return false se il body ha almeno un return
     */
    private Boolean checkNoReturn(Body body) {
        return body.getStatList()
                .stream()
                .filter(stat -> stat.getTipo().equals(Stat.Mode.RETURN))
                .findFirst()
                .isEmpty();
    }

    /**
     * Data una funzione restituisce una lista di stringhe con i tipi dichiarati
     * @param funzione
     * @return
     */
    private static ArrayList<String> getTipiDichiarati(Function funzione) {
        return funzione.getReturnTypes()
                .stream()
                .map(type -> type.getTipo().toString())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Object visit(Function funzione) {
        ArrayList<String> tipiDichiarati;
        ArrayList<String> tipiRestituiti = new ArrayList<>();

        //Ottieni i tipi di ritorno
        tipiDichiarati = getTipiDichiarati(funzione);

        //controlla che il body non sia vuoto
        if(checkEmptyBody(funzione.getBody())) {
            throw new RuntimeException("HAI UN BODY SENZA STATEMENT");
        }

        //CONTROLLA CHE CI SIA ALMENO UN RETURN
        if(checkNoReturn(funzione.getBody()))
            throw new RuntimeException("DEVI AVERE ALMENO UN RETURN");

        ArrayList<Stat> returns = new ArrayList<>();
        getAllFunctionReturns(funzione.getBody(), returns);

        //Controlla che i parametri sono usati in modo legale nella funzione
        if(controlloSugliAssign(funzione.getBody().getStatList(), funzione.getParametersList())) {
            throw new RuntimeException("I parametri di una funzione sono immutabili");
        }

        //controllo che ogni return abbia i tipi uguali a quelli della dichiarazione
        enterScope(funzione.getTable());
        for (Stat returnStat: returns) {
            ArrayList<String> tipiReturn = (ArrayList<String>) returnStat.accept(this);
            Iterator<String> itTipiReturn = tipiReturn.iterator();
            Iterator<String> itTipiDichiarati = tipiDichiarati.iterator();

            while(itTipiDichiarati.hasNext() && itTipiReturn.hasNext()) {
                if(!itTipiReturn.next().equals(itTipiDichiarati.next())) {
                    throw new RuntimeException("I tipi dei parametri usati nel return non matchano con quelli usati nella funzione,\n" +
                            " tipi nel return" + tipiReturn + " lessema = " +
                            " tipi nella dichiarazione" + tipiDichiarati);
                }
            }
        }

        //controlli sul body della funzione
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
            ((IfStat) statement).accept(this);
        }
        if(statement instanceof ProcCall) {
            ((ProcCall) statement).accept(this);
        }

        if(statement.getTipo().equals(Stat.Mode.ASSIGN)) {
            //uno statement di questo tipo ha un array di id e un array di espressioni
            var leftSide = statement.getIdsList();
            var rightSide = statement.getEspressioniList();

            var iteratoreLeftSide = leftSide.iterator();
            var iteratoreRightSide = rightSide.iterator();

            while(iteratoreLeftSide.hasNext() && iteratoreRightSide.hasNext())  {
                var actualId = iteratoreLeftSide.next();
                var actualExpression = iteratoreRightSide.next();

                if(actualExpression instanceof FunCall) {
                    try {
                        var listaTipiRitorno = (ArrayList<String>) ((FunCall) actualExpression).accept(this);
                        // vediamo il numero di tipiEspressione,
                        // in questo modo sappiamo quante volte andare avanti con iteratore degli id
                        int numeroTipiRitorno = listaTipiRitorno.size();

                        for(int i = 0; i < numeroTipiRitorno; i++) {
                            //vediamo se ogni id matcha con il tipo di ritorno corrispondente
                            String tipoDiId = (String) actualId.accept(this);
                            if(!listaTipiRitorno.get(i).equals(tipoDiId)) {
                                throw new Exception("AOOOO FRATEEEEEE I TIPI NON SI TROVANO");
                            }
                            //vai avanti con gli iteratori
                            if(iteratoreLeftSide.hasNext()) {
                                actualId = iteratoreLeftSide.next();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                } else {
                    //se espressione non è una funzione allora puoi agire normalmente
                    String tipoId = (String) actualId.accept(this);
                    String tipoEspressione = "";
                    try {
                        tipoEspressione = (String) actualExpression.accept(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                    if(!tipoId.equalsIgnoreCase(tipoEspressione)){
                        try {
                            throw new Exceptions.TypesMismatch(actualId.getLessema(), tipoId, tipoEspressione);
                        } catch (Exception e){
                            System.out.println("tipo id = " + tipoId + "tipo espressione = " + tipoEspressione);
                            e.printStackTrace();
                            System.exit(-1);
                        }
                    }
                }//fine else

            }


        }

        /**
         * Si controlla che gli id nella read siano stati tutti dichiarati precedentemente
         */
        if(statement.getTipo().equals(Stat.Mode.READ)) {
            statement.getEspressioniList().forEach(exprOP -> {
                if(exprOP instanceof Identifier) {
                        exprOP.accept(this);
                } else if(exprOP instanceof ConstOP || exprOP instanceof BinaryOP) {
                        String tipoCostante = (String) exprOP.accept(this);
                        if(!tipoCostante.equalsIgnoreCase("String")){
                            throw new RuntimeException("puoi usare solo un id o Stringhe in una read");
                        }
                }
                else {
                    throw new RuntimeException("solo un id puoi usare nella read");
                }
            });
        }

        //devo controllare che stia all'interno di una funzione e non di una procedura
        //devo controllare che l'espressione return abbia un tipo compatibile col tipo di ritorno di una funzione
        //devo controllare che gli ipotetici id siano dichiarati
        if(statement.getTipo().equals(Stat.Mode.RETURN)) {
            //devo controllare che il tipi di ritorno della funzione matchano con i tipi effettivamente restituiti
            ArrayList<String> tipiDiRitorno = new ArrayList<>();
            statement.getEspressioniList().forEach(exprOP -> {
                try {
                    Object resultAccept = exprOP.accept(this);
                    if( resultAccept instanceof ArrayList<?>){
                        ((ArrayList<String>) resultAccept).forEach(tipo -> tipiDiRitorno.add(tipo));
                    } else {
                        tipiDiRitorno.add((String)resultAccept);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return tipiDiRitorno;
        }

        if(statement.getTipo().equals(Stat.Mode.WRITE_RETURN)) {
            statement.getEspressioniList().forEach(exprOP -> {
                try {
                    exprOP.accept(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        if(statement.getTipo().equals(Stat.Mode.WRITE)) {
            statement.getEspressioniList().forEach(exprOP -> {
                try {
                    exprOP.accept(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
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
            try {
                throw new Exceptions.InvalidCondition(tipoExpr);
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
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

        if (!tipoExpr.equalsIgnoreCase("boolean"))
            try {
                throw new Exceptions.InvalidCondition(tipoExpr);
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

        enterScope(elseIfOP.getSymbolTableElseIF());
        elseIfOP.getBody().accept(this);
        exitScope();

        return null;
    }


    private Boolean hannoStessoNumeroDiParametri(ArrayList<CallableParam> parametriDichiarati, ArrayList<Object> parametriUtilizzati) {
        int countParametriDichiarati = parametriDichiarati.size();
        int countParmetriUtilizzati =  0;
        for (Object param: parametriUtilizzati) {
            if(param instanceof ArrayList<?>){
                var lista = (ArrayList<String>) param;
                countParmetriUtilizzati += lista.size();
            } else {
                countParmetriUtilizzati++;
            }
        }
        return countParametriDichiarati==countParmetriUtilizzati;
    }
    
    @Override
    public Object visit(ProcCall procCall) throws Exception {
        //Lookup nella tabella
        SymbolTableRecord record = new SymbolTableRecord();
        try {
            currentScope.lookup(procCall.getIdentifier().getLessema());

            record = currentScope
                    .lookup(procCall.getIdentifier().getLessema())
                    .orElseThrow(() -> new Exceptions.NoDeclarationError(procCall.getIdentifier().getLessema()));

        } catch (Exception e){
            e.printStackTrace();
        }

        //bisogna scorrere i parametri dichiarati nella funzione e quelli effettivamente utilizzati
        //per controllare il tipo

        //prendiamoci i parametri dichiarati nel record che abbiamo preso dalla tabella dei simboli
        ArrayList<CallableParam> parametriDichiarati = ((CallableFieldType) record.getFieldType()).getParams();

        //prendiamoci i parametri utilizzati nella chiamata di procedura
        //qui dentro ci possono essere tutti i tipi di Expr anche la chiamata a funzione che restituisce una lista di stringhe
        //che indica i tipi di ritorno della funzione
        ArrayList<Object> parametriUtilizzati = new ArrayList<>();

        for (ExprOP exprOP: procCall.getExprs()) {
            parametriUtilizzati.add(exprOP.accept(this));
        }

        if (!hannoStessoNumeroDiParametri(parametriDichiarati, parametriUtilizzati)) {
            throw new Exception("il numero di parametri in procedura non matcha"); //TODO custom exception
        }

        //Adesso scorriamo sia la lista dei parametri utilizzati che quelli dichiarati e confrontiamo tipo per tipo
        Iterator<CallableParam> paramDichiaratiIterator = parametriDichiarati.iterator();
        Iterator<Object> paramUtilizzatiIterator = parametriUtilizzati.iterator();
        Iterator<ExprOP> itExprOPs = procCall.getExprs().iterator();

        while(paramUtilizzatiIterator.hasNext() && paramDichiaratiIterator.hasNext() && itExprOPs.hasNext())
        {
            //vediamo il vero tipo dell'object
            Object parametroUtilizzatoCorrente = paramUtilizzatiIterator.next();
            ExprOP exprOPcorrente = itExprOPs.next();
            //caso di chiamata a funzione, RICORDA la funzione restituisce un array di tipi di ritorno
            if (parametroUtilizzatoCorrente instanceof ArrayList<?>) {

                //iteratore sulla lista dei tipi di ritorno della funzione
                ArrayList<String> tipiDiRitornoFunzione = (ArrayList<String>) parametroUtilizzatoCorrente;
                Iterator<String> tipiDiRitornoFunzioneIt = tipiDiRitornoFunzione.iterator();

                if(exprOPcorrente.getMode().equals(ExprOP.Mode.PARAMSREF)) {
                    throw new Exception("KEYWORD @ utilizzata in corrispondenza di una funzione");
                }
                while (tipiDiRitornoFunzioneIt.hasNext()) {

                    CallableParam paramDichiarato = paramDichiaratiIterator.next();


                    String tipoParametroDichiarato = null;
                    try {
                        tipoParametroDichiarato = paramDichiarato.getTipo().getTipo();
                    } catch (NoSuchElementException e) {
                        throw new Exception("il numero dei parametri utilizzati sono diversi da quelli dichiarati");
                    }

                    String tipoParametroUtilizzato = tipiDiRitornoFunzioneIt.next();


                    if(!tipoParametroUtilizzato.equals(tipoParametroDichiarato)) {
                        throw new Exceptions.TypesMismatch(procCall.getIdentifier().getLessema(), tipoParametroDichiarato, tipoParametroUtilizzato);
                    }

                    //CONTROLLO SULLA KEYWORD OUT
                    if(paramDichiarato.getId().getMode().equals(ExprOP.Mode.PARAMSOUT)){
                        throw new Exception("Non puoi utilizzare una funzione in corrispondenza di parametri out");
                    }

                }
            }
            //ogni altro caso, per exprOP
            else if(parametroUtilizzatoCorrente instanceof String) {
                //poi facciamo il confronto
                CallableParam parametroInTable = paramDichiaratiIterator.next();
                String parametroUtilizzatoCorrente_string = (String) parametroUtilizzatoCorrente;

                if(!exprOPcorrente.getMode().equals(ExprOP.Mode.PARAMSREF) && parametroInTable.getId().getMode().equals(ExprOP.Mode.PARAMSOUT)) {
                    throw new Exception("type mismatch nella procedura: NON CI SIAMO CON LE OUT E I REF "); //TODO CUSTOM EXCEPTION
                }

                if(exprOPcorrente.getMode().equals(ExprOP.Mode.PARAMSREF) && !parametroInTable.getId().getMode().equals(ExprOP.Mode.PARAMSOUT)){
                    throw new Exception("non si trovano out e ref"); //TODO CUSTOM EXCEPTION

                }

                // ora si vede se i tipi matchano
                String tipoParametroInTable = parametroInTable.getTipo().getTipo();
                if (!tipoParametroInTable.equals(parametroUtilizzatoCorrente_string)) {
                    throw new Exceptions.TypesMismatch(procCall.getIdentifier().getLessema(), parametroInTable.getTipo().toString(), parametroUtilizzatoCorrente_string);

                }
            }
        }
            if(paramUtilizzatiIterator.hasNext() || paramDichiaratiIterator.hasNext()) {
                throw new RuntimeException("i parametri utilizzati sono diversi da quelli dichiarati");
            }

        return null;
    }

    @Override
    public Object visit(WhileStat whileStat) {
        String condition = (String) whileStat.getExpr().accept(this);

        if( !condition.equalsIgnoreCase("boolean")){
            try {
                throw new Exceptions.InvalidCondition(condition);
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        enterScope(whileStat.getTable());
        whileStat.getBody().accept(this);
        exitScope();
        return null;
    }


    @Override
    public Object visit(Body body) {
        //se il body ha delle dichiarazioni di variabili, controllale
        if(body.getVarDeclList()!=null) {
            body.getVarDeclList().forEach(var -> var.accept(this));
        }

        //se il body ha degli statement, controllalit
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
        String tipo = "";

        // se il tipo di dichiarazione è del tipo var a ^=2;\
        if(decl.getTipoDecl().toString().equals("ASSIGN")) {
            /**
             * fatti 2 liste: una per le const e una per gli id
             * fatti 2 iteratori uno per la lista di const e uno per la lista di id
             * itera su entrambe le liste e vedi se i tipi sono uguali
             */
            var listaConsts = decl.getConsts();
            var listaIds = decl.getIds();

            var iteratoreConsts = listaConsts.iterator();
            var iteratoreIds = listaIds.iterator();

            /**
             * Per ogni elemento controlla che il tipo dell'ID (quello che ottieni chiamando la accept)
             * sia effettivamente quello della Costante, se così non fosse lancia un'eccezione.
             */
            while(iteratoreConsts.hasNext() && iteratoreIds.hasNext()) {
                var costanteAttuale = iteratoreConsts.next();
                var idAttuale = iteratoreIds.next();

                var tipoId = (String) idAttuale.accept(this);
                var tipoCostante = costanteAttuale.getType().toString();

                if(!tipoId.equalsIgnoreCase(tipoCostante)){
                    throw new RuntimeException("I tipi dichiarati ed effettivamente restituiti non matchano nella funzione"); //TODO CUSTOM EXCEPTIOP
                }
            }
        }
        // caso in cui non è assign ma type occorre che io controlli nella tabella che il tipo sia uguale a quello dichiarato
        else if(decl.getTipoDecl().toString().equals("TYPE")) {
            for(Identifier id : decl.getIds()) {
                var tipoInTable = (String) id.accept(this);
                try {
                    if (!tipoInTable.equalsIgnoreCase(decl.getTipo().getTipo())) {
                        throw new Exception("TYPE MISMATCH KING");
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
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
            System.exit(-1);
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
                    e.printStackTrace();
                }
            });
        return null;
    }


    @Override
    public Object visit(IterWithoutProcedure iterOP) {
        iterOP.getDeclarations().forEach(s->s.accept(this));
        iterOP.getFunctions().forEach(s -> {
            try {
                s.accept(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return null;
    }


    @Override
    public Object visit(Procedure procedure) {
        ArrayList<Stat> listaReturnStatementProcedura =  new ArrayList<>();
        getAllFunctionReturns(procedure.getBody(), listaReturnStatementProcedura);

        //controlliamo che la procedura non abbia dei returns
        if(!listaReturnStatementProcedura.isEmpty()) {
            try {
                throw new Exceptions.SemanticError();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
        var typeAsString = constOP.getType().toString();
        //Prendo solo la parte che mi interessa di type ossia quella senza "_CONST"
        return typeAsString;
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
    public Object visit(FunCall funCall) {
        //1. controlllo il numero di parametri se coincide con quello nella table
        //se record è null vuol dire che la funzione non è mai stata dichiarata
        SymbolTableRecord record;
        record = currentScope
                .lookup(funCall.getIdentifier().getLessema())
                .orElseThrow(() -> new Exceptions.NoDeclarationError(funCall.getIdentifier().getLessema()));


        var fieldType = (CallableFieldType) record.getFieldType();

        var listaParametriNellaChiamata = funCall.getExprs();
        var listaParametriDichiarazione = fieldType.getParams();

        var nParamsChiamata = listaParametriNellaChiamata.size();
        var nParamsDichiarati = listaParametriDichiarazione.size();

        var isEqual = (nParamsChiamata == nParamsDichiarati) ? true : false ;

        if(!isEqual) {
            throw new RuntimeException("NUMERO DI PARAMETRI DIVERSO DALLA DECL"); //TODO CUSTOM EXC
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
            if(!tipoCallableParam.equalsIgnoreCase(tipoExpr)) {
                throw new RuntimeException("I TIPI NON MATCHANO NELLA FUNZIONE"); //TODO CUSTOM EXCEPTION
            }
        }
        var tipiDiRitorno = new ArrayList<>(Arrays.asList(record.getProperties().split(";")));
        return tipiDiRitorno;
    }

    /**
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
            if(exprOP.getMode().equals(ExprOP.Mode.IOARGSDOLLAR)) {
                throw new RuntimeException("IO ARGS DOLLAR VUOLE SOLO UN ID");
            }
            return ((ConstOP) exprOP).accept(this);
        }
        else if(exprOP instanceof BinaryOP) {
            if(exprOP.getMode().equals(ExprOP.Mode.IOARGSDOLLAR)) {
                throw new RuntimeException("IO ARGS DOLLAR VUOLE SOLO UN ID");
            }
            return ((BinaryOP) exprOP).accept(this);
        }
        else if(exprOP instanceof Identifier) {
            return ((Identifier) exprOP).accept(this);
        }
        else if(exprOP instanceof UnaryOP) {
            if(exprOP.getMode().equals(ExprOP.Mode.IOARGSDOLLAR)) {
                throw new RuntimeException("IO ARGS DOLLAR VUOLE SOLO UN ID");
            }
            return ((UnaryOP) exprOP).accept(this);
        }
        else if(exprOP instanceof FunCall) {
            if(exprOP.getMode().equals(ExprOP.Mode.IOARGSDOLLAR)) {
                throw new RuntimeException("IO ARGS DOLLAR VUOLE SOLO UN ID");
            }

            return ((FunCall) exprOP).accept(this);
        }

        return null;
    }

    @Override
    public Object visit(CallableParam callableParam) {
        return null;
    }
}
