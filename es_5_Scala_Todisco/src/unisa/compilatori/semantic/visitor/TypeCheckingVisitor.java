package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.CallableFieldType;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.symboltable.SymbolTableRecord;
import unisa.compilatori.semantic.symboltable.VarFieldType;
import unisa.compilatori.utils.Exceptions;


import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import java.util.*;

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
    public String evaluateType(String type1, String type2, String op){
        switch (op){
            case "plus_op", "times_op", "minus_op":
                if (type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("INTEGER"))
                    return "integer";
                else if (type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("REAL"))
                    return new String("REAL");
                if (type1.equalsIgnoreCase("REAL") && type2.equalsIgnoreCase("INTEGER"))
                    return new String("REAL");
                if (type1.equalsIgnoreCase("REAL") && type2.equalsIgnoreCase("REAL"))
                    return new String("REAL");
                if(type1.equalsIgnoreCase("STRING") && type2.equalsIgnoreCase("STRING")) {
                    return new String("STRING");
                }
                else if (type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("STRING"))
                    return new String("STRING");
                else if (type1.equalsIgnoreCase("STRING") && type2.equalsIgnoreCase("INTEGER")) {
                    System.out.println("sono nella evaluate type corretta quella di string + int");
                    return new String("STRING");
                }
                else if (type1.equalsIgnoreCase("STRING") && type2.equalsIgnoreCase("REAL"))
                    return new String("STRING");
                else if (type1.equalsIgnoreCase("REAL") && type2.equalsIgnoreCase("STRING"))
                    return new String("STRING");
                else if (type1.equalsIgnoreCase("STRING") && type2.equalsIgnoreCase("BOOLEAN"))
                    return new String("STRING");
                else if (type1.equalsIgnoreCase("BOOLEAN") && type2.equalsIgnoreCase("STRING"))
                    return new String("STRING");
                else {
                    throw new RuntimeException("errore di tipo nella evaluate type, type1 = " + type1 + " type2 = " + type2);
                }

            case "or_op", "and_op":
                if(type1.equalsIgnoreCase("BOOLEAN") && type2.equalsIgnoreCase("BOOLEAN"))
                    return new String("boolean");
                else
                    throw new RuntimeException("Tipi incompatibili per operazione binaria");

            case "div_op":
                if (type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("INTEGER"))
                    return "real";
                else if (type1.equalsIgnoreCase("INTEGER") && type2.equalsIgnoreCase("REAL"))
                    return new String("REAL");
                else if (type1.equalsIgnoreCase("REAL") && type2.equalsIgnoreCase("INTEGER"))
                    return new String("REAL");
                else if (type1.equalsIgnoreCase("REAL") && type2.equalsIgnoreCase("REAL"))
                    return new String("REAL");
                else {
                    throw new RuntimeException("errore di tipo nella evaluate type, type1 = " + type1 + " type2 = " + type2);
                }


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
                    throw new RuntimeException("tipo1 = " + type1 + "tipo2 = " + type2 + " op name = " + op);

            case "eq_op", "ne_op":
                if(type1.equalsIgnoreCase(type2))
                    return new String("BOOLEAN");
                else
                    throw new RuntimeException("errore");
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
    private String evaluateType(String type1, String op) {
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
                throw new RuntimeException("ERRORE COI TIPI");
        }
    }



    @Override
    public Object visit(ProgramOp program) {
        currentScope = program.getTable();

        //controllo che ci sia uno e un solo main
        SymbolTableRecord main = currentScope.lookup("main").orElseThrow(Exceptions.LackOfMain::new);
        // controllo che sia una procedura e non una funzione
        if ( main.getNodo() instanceof Function) {
            throw new RuntimeException("Il main è una funzione");
        }
        //TODO Dobbiamo controllare se il main ha dei parametri?

        program.getIterOp().accept(this);

        return null;
    }

    @Override
    public Object visit(BinaryOP operazioneBinaria) {
        String typeExpr1 = null;
        String typeExpr2 = null;
        //faccio il lookup di expr1
        if(operazioneBinaria.getExpr1() instanceof FunCall) {
            ArrayList<String> tipiDiRitornoExpr1 = (ArrayList<String>) operazioneBinaria.getExpr1().accept(this);
            if(tipiDiRitornoExpr1.size() > 1) {
                throw new RuntimeException("Hai chiamato una funzione con più parametri di ritorno in corrispondenza di un' operazione binaria");
            }
            typeExpr1 = tipiDiRitornoExpr1.get(0);
        } else {
            typeExpr1 = (String) operazioneBinaria.getExpr1().accept(this);
        }

        if(operazioneBinaria.getExpr2() instanceof FunCall) {
            ArrayList<String> tipiDiRitornoExpr2 = (ArrayList<String>) operazioneBinaria.getExpr2().accept(this);
            if(tipiDiRitornoExpr2.size() > 1) {
                throw new RuntimeException("Hai chiamato una funzione con più parametri di ritorno in corrispondenza di un' operazione binaria");
            }
            typeExpr2 = tipiDiRitornoExpr2.get(0);
        } else {
           typeExpr2 = (String) operazioneBinaria.getExpr2().accept(this);
        }

        //faccio il lookup dell'operazione
        String typeOp = operazioneBinaria.getName();
        //controllo di che tipo l'operazione binaria
        String risultato =  evaluateType(typeExpr1, typeExpr2, typeOp);
        operazioneBinaria.setTipo(risultato);

        operazioneBinaria.getExpr1().setTipo(typeExpr1);
        operazioneBinaria.getExpr2().setTipo(typeExpr2);

        return risultato;
    }


    @Override
    public Object visit(UnaryOP operazioneUnaria) {
        String tipoExpr1;
        //Qui si controlla che l'operazione non sia una funzione con più tipi di ritorno
        if(operazioneUnaria.getExpr() instanceof FunCall) {
            FunCall espressioneFuncall = ((FunCall) operazioneUnaria.getExpr());
            ArrayList<String> tipiRitornoFunzione = (ArrayList<String>) espressioneFuncall.accept(this);
            if(tipiRitornoFunzione.size() > 1) {
                throw new RuntimeException("Non puoi usare una funzione con più tipi di ritorno in una UnaryOP");
            }
            tipoExpr1 = tipiRitornoFunzione.get(0);
        } else {
            tipoExpr1 = (String) operazioneUnaria.getExpr().accept(this);
        }

        String expName = operazioneUnaria.getSimbolo();
        String risultato = "";
        risultato = evaluateType(tipoExpr1, expName);
        operazioneUnaria.setTipo(tipoExpr1);

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
    private void getAllFunctionReturns(Body body, HashMap<Stat, SymbolTable> listaReturn) {
        if(body==null || body.getStatList().isEmpty()) {
            return;
        }

        //TODO trovato bug che se hai solo un if nella funzione
        // con un return e non metti un return al termine della funzione non ti dà errore
        /*
        PER LUI QUESTO VA BENE:

            var risultato:integer;\
            risultato ^= a+b;

            if (risultato > 20) && (risultato < 40) then

                return risultato;
            endif;

        endfunc
         */
        else{
            ArrayList<Stat> listaStatements = body.getStatList();
            for(Stat statement : listaStatements) {
                if(statement instanceof WhileStat) {
                    WhileStat whileStat = (WhileStat) statement;

                    enterScope(whileStat.getTable());

                    getAllFunctionReturns(whileStat.getBody(), listaReturn);

                    exitScope();
                }
                else if(statement instanceof IfStat) {
                    IfStat ifStat = (IfStat) statement;

                    enterScope(ifStat.getSymbolTableThen());

                    getAllFunctionReturns((ifStat).getBody(), listaReturn);

                    exitScope();

                    if(!ifStat.getElseIfOPList().isEmpty()) {
                        ifStat.getElseIfOPList().forEach(elseIfOP -> {
                            enterScope(elseIfOP.getSymbolTableElseIF());

                            getAllFunctionReturns(elseIfOP.getBody(), listaReturn);

                            exitScope();
                        });
                    }

                    if(ifStat.getElseOP()!=null) {
                        enterScope(ifStat.getElseOP().getSymbolTableElseOp());

                        getAllFunctionReturns(ifStat.getElseOP().getBody(), listaReturn);

                        exitScope();
                    }



                }
                else if (statement.getTipo().equals(Stat.Mode.RETURN)){
                    listaReturn.put(statement, this.currentScope);
                }

            }
        }
    }

    public void getAllAssignStatement(ArrayList<Stat> statements, ArrayList<Stat> assignStatements) {
        if(statements == null ||statements.isEmpty()) {
            return;
        }

        for (Stat stat : statements) {
            if( stat instanceof WhileStat) {
                WhileStat whileStat = (WhileStat) stat;
                getAllAssignStatement(whileStat.getBody().getStatList(), assignStatements);
            }
            if( stat instanceof IfStat) {
                IfStat IfStat = (IfStat) stat;
                getAllAssignStatement(IfStat.getBody().getStatList(), assignStatements);

                if(IfStat.getElseOP() != null) {
                    getAllAssignStatement(IfStat.getElseOP().getBody().getStatList(), assignStatements);
                }

                for( ElseIfOP elseif : IfStat.getElseIfOPList()) {
                    getAllAssignStatement(elseif.getBody().getStatList(), assignStatements);
                }
            }
            if(stat.getTipo().equals(Stat.Mode.ASSIGN)) {
                assignStatements.add(stat);
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


        ArrayList<Stat> statsAssign = new ArrayList<>();

        //mi ricavo gli statement di tipo assign
        getAllAssignStatement(bodyStatements, statsAssign);

        //mi ricavo gli ids utilizzati nefIi parametri della funzione
        List<Identifier> callableParams = paramsFunzione.stream().map(callableParam -> callableParam.getId()).toList();

        //controllo che ogni statement assign non utilizzi un parametro immutable
        for (Stat stat : statsAssign) {
            for(Identifier id : stat.getIdsList()) {

                modificatoMutable = callableParams
                        .stream()
                        .anyMatch(param -> param.getLessema()
                                .equals(id.getLessema()));

                if(modificatoMutable) {
                    return modificatoMutable;
                }
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
     * @param funz
     * @return true se il body non ha return
     * @return false se il body ha almeno un return
     */
    private Boolean checkNoReturn(Function funz) {
        var listaReturns = new HashMap<Stat, SymbolTable>(); // è la lista che ottengo da getALLFunctionsReturns

        enterScope(funz.getTable());

        getAllFunctionReturns(funz.getBody(), listaReturns);

        exitScope();

        return listaReturns.isEmpty();

        //return result;
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
            throw new Exceptions.EmptyBodyError();
        }

        //CONTROLLA CHE CI SIA ALMENO UN RETURN
        if(checkNoReturn(funzione)) //TODO CONTROLLARE I RETURN NEL CASO DEGLI IFSTAT
            throw new Exceptions.NoReturnError(funzione.getId().getLessema());

        var returns = new HashMap<Stat, SymbolTable>();

        enterScope(funzione.getTable());

        getAllFunctionReturns(funzione.getBody(), returns);

        exitScope();

        if(controlloSugliAssign(funzione.getBody().getStatList(), funzione.getParametersList())) {
            throw new RuntimeException("I parametri di una funzione sono immutabili");
        }
        //Controlla che i parametri sono usati in modo legale nella funzione


        //controllo che ogni return abbia i tipi uguali a quelli della dichiarazione
        for (Stat returnStat: returns.keySet()) {

            enterScope(returns.get(returnStat));

            ArrayList<String> tipiReturn = (ArrayList<String>) returnStat.accept(this);

            exitScope();

            Iterator<String> itTipiReturn = tipiReturn.iterator();
            Iterator<String> itTipiDichiarati = tipiDichiarati.iterator();

            while(itTipiDichiarati.hasNext() && itTipiReturn.hasNext()) {
                if(!itTipiReturn.next().equalsIgnoreCase(itTipiDichiarati.next())) {
                    throw new RuntimeException("I tipi dei parametri usati nel return non matchano con quelli usati nella funzione,\n" +
                            " tipi nel return" + tipiReturn +
                            " tipi nella dichiarazione" + tipiDichiarati);
                }
            }

            if(itTipiDichiarati.hasNext() || itTipiReturn.hasNext()) {
                throw new RuntimeException("I tipi dei parametri usati nel return non matchano con quelli usati nella funzione,\n" +
                        " tipi nel return " + tipiReturn +
                        " tipi nella dichiarazione" + tipiDichiarati);
            }
        }

        enterScope(funzione.getTable());
        //controlli sul body della funzione
        funzione.getBody().accept(this);

        exitScope();

        return null;
    }



    @Override
    public Object visit(Stat statement) throws RuntimeException {
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
            System.out.println("stateent = " + statement );
            //1. prendiamo i tipi di ritorno di una possibile funzione
            //2. prendiamo i tipi dei
            ArrayList<String> leftSide = statement.getIdsList()
                    .stream()
                    .map(id -> (String) id.accept(this))
                    .collect(Collectors.toCollection(ArrayList<String>::new));


            ArrayList<String> rightSide = getStringArrayList(statement);

            Iterator<String> itLeftSide = leftSide.iterator();
            Iterator<String> itRightSide = rightSide.iterator();

            while(itLeftSide.hasNext() && itRightSide.hasNext()) {
                String tipoLeftSide = itLeftSide.next();
                String tipoRightSide = itRightSide.next();

                if(tipoLeftSide.equalsIgnoreCase("real") && tipoRightSide.equalsIgnoreCase("integer")){
                    continue;
                }

                if(!tipoRightSide.equalsIgnoreCase(tipoLeftSide)) {
                    throw new Exceptions.TypesMismatch(tipoLeftSide,tipoRightSide);
                }
            }

            if(itLeftSide.hasNext() || itRightSide.hasNext()) {
                throw new RuntimeException("Numero di elementi a destra e sinistra di assign non è uguale");
            }

        }

        /**
         * Si controlla che gli id nella read siano stati tutti dichiarati precedentemente
         */
        if(statement.getTipo().equals(Stat.Mode.READ)) {
            for(ExprOP espressione : statement.getEspressioniList()){
                if(espressione instanceof Identifier) {
                    espressione.accept(this);
                }
                else if(espressione instanceof ConstOP || espressione instanceof BinaryOP) {
                    String tipoCostante = (String) espressione.accept(this);
                    if(!tipoCostante.equalsIgnoreCase("String")){
                        throw new RuntimeException("puoi usare solo un id o Stringhe in una read");
                    }
                }
                else if(espressione instanceof FunCall){
                    ArrayList<String> tipiRitorno = (ArrayList<String>) ((FunCall) espressione).accept(this);
                    if(tipiRitorno.size()>1){
                        throw new RuntimeException("Non puoi usare una funzione con più tipi di ritorno in una read");
                    }
                }
                else {
                    throw new RuntimeException("Una Read richiede che si usino ID");
                }

            }

        }

        //devo controllare che stia all'interno di una funzione e non di una procedura
        //devo controllare che l'espressione return abbia un tipo compatibile col tipo di ritorno di una funzione
        //devo controllare che gli ipotetici id siano dichiarati
        if(statement.getTipo().equals(Stat.Mode.RETURN)) {
            //devo controllare che il tipi di ritorno della funzione matchano con i tipi effettivamente restituiti
            ArrayList<String> tipiDiRitorno = new ArrayList<>();

            statement.getEspressioniList().forEach(exprOP -> {
                    Object resultAccept = exprOP.accept(this);
                    if( resultAccept instanceof ArrayList<?>){
                        ((ArrayList<String>) resultAccept).forEach(tipo -> tipiDiRitorno.add(tipo));
                    } else {
                        tipiDiRitorno.add((String)resultAccept);
                    }
            });
            return tipiDiRitorno;
        }

        if(statement.getTipo().equals(Stat.Mode.WRITE_RETURN)) {
            statement.getEspressioniList().forEach(exprOP -> {
                if(exprOP instanceof FunCall) {
                    ArrayList<String> resultFunCall = (ArrayList<String>) exprOP.accept(this);
                    if (resultFunCall.size() > 1 )  {
                        throw new RuntimeException("Stai usando una funzione con più parametri di ritorno in una WRITE operation");
                    }
                } else {
                    exprOP.accept(this);
                }
            });
        }

        if(statement.getTipo().equals(Stat.Mode.WRITE)) {
            statement.getEspressioniList().forEach(exprOP -> {
                if(exprOP instanceof FunCall) {
                    ArrayList<String> resultFunCall = (ArrayList<String>) exprOP.accept(this);
                    if (resultFunCall.size() > 1 )  {
                        throw new RuntimeException("Stai usando una funzione con più parametri di ritorno in una WRITE operation");
                    }
                } else {
                    exprOP.accept(this);
                }
            });
        }

        return null;
    }

    private ArrayList<String> getStringArrayList(Stat statement) {
        ArrayList<String> rightSide = new ArrayList<>();
        statement.getEspressioniList().forEach(exprOP -> {
            Object resultAccept = exprOP.accept(this);

            if(resultAccept instanceof ArrayList<?>){
                ((ArrayList<String>) resultAccept).forEach(tipo -> rightSide.add(tipo));
            } else {
                rightSide.add((String)resultAccept);
            }
        });
        return rightSide;
    }

    @Override
    public Object visit(IfStat ifStat) throws RuntimeException{
        String tipoExpr = "";

        Object resultExpr = ifStat.getExpr().accept(this);
        if(resultExpr instanceof ArrayList<?>) {
            ArrayList<String> funCall = ((ArrayList<String>) resultExpr);

            if(funCall.size() > 1) {
                throw  new RuntimeException("Hai chiamato una funzione con più tipi di ritorno in un IF");
            }

            tipoExpr = funCall.get(0);
        } else {
            tipoExpr = (String) ifStat.getExpr().accept(this);
        }



        if (!tipoExpr.equalsIgnoreCase("boolean")) {
            throw new Exceptions.InvalidCondition(tipoExpr);
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
    public Object visit(ElseOP elseOP) throws RuntimeException {
        //entro nello scope dell'else op
        enterScope(elseOP.getSymbolTableElseOp());
        //controllo il body dell'else
        elseOP.getBody().accept(this);
        //esco dallo scope dell'else
        exitScope();
        return null;
    }

    @Override
    public Object visit(ElseIfOP elseIfOP) throws RuntimeException{
        String tipoExpr;

        Object resultExpr = elseIfOP.getExpr().accept(this);
        if(resultExpr instanceof ArrayList<?>) {
            ArrayList<String> funCall = ((ArrayList<String>) resultExpr);

            if(funCall.size() > 1) {
                throw  new RuntimeException("Hai chiamato una funzione con più tipi di ritorno in un IF");
            }

            tipoExpr = funCall.get(0);
        } else {
            tipoExpr = (String) elseIfOP.getExpr().accept(this);
        }


        if (!tipoExpr.equalsIgnoreCase("boolean"))
            throw new Exceptions.InvalidCondition(tipoExpr);

        enterScope(elseIfOP.getSymbolTableElseIF());
        elseIfOP.getBody().accept(this);
        exitScope();

        return null;
    }

    @Override
    public Object visit(ProcCall procCall) throws RuntimeException {
        //Lookup nella tabella
        SymbolTableRecord record = new SymbolTableRecord();
            currentScope.lookup(procCall.getIdentifier().getLessema());

        record = currentScope
                .lookup(procCall.getIdentifier().getLessema())
                .orElseThrow(() -> new Exceptions.NoDeclarationError(procCall.getIdentifier().getLessema()));

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

        if(parametriDichiarati.size() != parametriUtilizzati.size()) {
            throw new RuntimeException("il numero di parametri in procedura non matcha");
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

                if(tipiDiRitornoFunzione.size() > 1) {
                    throw new RuntimeException("Hai chiamato una funzione con più valori di ritorno in una procedura");
                }

                if(exprOPcorrente.getMode().equals(ExprOP.Mode.PARAMSREF)) {
                    throw new RuntimeException("KEYWORD @ utilizzata in corrispondenza di una funzione");
                }

                CallableParam paramDichiarato = paramDichiaratiIterator.next();


                String tipoParametroDichiarato = null;
                try {
                    tipoParametroDichiarato = paramDichiarato.getTipo().getTipo();
                } catch (NoSuchElementException e) {
                    throw new RuntimeException("il numero dei parametri utilizzati sono diversi da quelli dichiarati");
                }

                String tipoParametroUtilizzato = tipiDiRitornoFunzione.get(0);


                if(!tipoParametroUtilizzato.equals(tipoParametroDichiarato)) {
                    throw new Exceptions.TypesMismatch(procCall.getIdentifier().getLessema(), tipoParametroDichiarato, tipoParametroUtilizzato);
                }

                //CONTROLLO SULLA KEYWORD OUT
                if(paramDichiarato.getId().getMode().equals(ExprOP.Mode.PARAMSOUT)){
                    throw new RuntimeException("Non puoi utilizzare una funzione in corrispondenza di parametri out");
                }

            }
            //ogni altro caso, per exprOP
            else if(parametroUtilizzatoCorrente instanceof String) {
                //poi facciamo il confronto
                CallableParam parametroInTable = paramDichiaratiIterator.next();
                String parametroUtilizzatoCorrente_string = (String) parametroUtilizzatoCorrente;

                if(!exprOPcorrente.getMode().equals(ExprOP.Mode.PARAMSREF) && parametroInTable.getId().getMode().equals(ExprOP.Mode.PARAMSOUT)) {
                    throw new RuntimeException("type mismatch nella procedura: NON CI SIAMO CON LE OUT E I REF "); //TODO CUSTOM EXCEPTION
                }

                if(exprOPcorrente.getMode().equals(ExprOP.Mode.PARAMSREF) && !parametroInTable.getId().getMode().equals(ExprOP.Mode.PARAMSOUT)){
                    throw new RuntimeException("non si trovano out e ref"); //TODO CUSTOM EXCEPTION
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
        String condition = "";

        if(whileStat.getExpr() instanceof FunCall){
            var espressioneFuncall = (FunCall) whileStat.getExpr();
            var tipi = (ArrayList<String>) espressioneFuncall.accept(this);
            if(tipi.size() > 1)
                throw new RuntimeException("Non puoi usare una funzione con più tipi di ritorno in un while");
            else{
                condition = tipi.get(0);
            }
        }
        else {
            condition = (String) whileStat.getExpr().accept(this);
        }

        if( !condition.equalsIgnoreCase("boolean")){
            throw new Exceptions.InvalidCondition(condition);
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
                if (!tipoInTable.equalsIgnoreCase(decl.getTipo().getTipo())) {
                    throw new RuntimeException("TYPE MISMATCH KING");
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
        record = currentScope
                .lookup(id.getLessema())
                .orElseThrow(() -> new Exceptions.NoDeclarationError(id.getLessema()));

        VarFieldType varFieldType;
        try {
            varFieldType = (VarFieldType) record.getFieldType();
        }
        catch (Exception e) {
            throw new RuntimeException("Stai usando una procedura come una variabile");
        }
        return varFieldType.getType();
    }

    @Override
    public Object visit(IterOp iterOP) {
        iterOP.getProcedures().forEach(procedure -> procedure.accept(this));
        iterOP.getDeclarations().forEach(s->s.accept(this));
        iterOP.getFunctions().forEach(s -> s.accept(this));

        return null;
    }




    @Override
    public Object visit(Procedure procedure) throws RuntimeException{
        var listaReturnStatementProcedura =  new HashMap<Stat, SymbolTable>();
        getAllFunctionReturns(procedure.getBody(), listaReturnStatementProcedura);

        //controlliamo che la procedura non abbia dei returns
        if(!listaReturnStatementProcedura.isEmpty()) {
            throw new Exceptions.SemanticError();
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
    public Object visit(ConstOP constOP) throws RuntimeException{
        var typeAsString = constOP.getType().toString();
        constOP.setTipo(typeAsString);
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

        //TODO CODICE COPIATO SPUDORATAMENTE DA PROCCALL SI POTREBBE FARE UN PO' DI EXTRACTION
        //Lookup nella tabella
        SymbolTableRecord record = new SymbolTableRecord();
        currentScope.lookup(funCall.getIdentifier().getLessema());

        record = currentScope
                .lookup(funCall.getIdentifier().getLessema())
                .orElseThrow(() -> new Exceptions.NoDeclarationError(funCall.getIdentifier().getLessema()));

        //bisogna scorrere i parametri dichiarati nella funzione e quelli effettivamente utilizzati
        //per controllare il tipo

        //prendiamoci i parametri dichiarati nel record che abbiamo preso dalla tabella dei simboli
        ArrayList<CallableParam> parametriDichiarati = ((CallableFieldType) record.getFieldType()).getParams();

        //prendiamoci i parametri utilizzati nella chiamata di procedura
        //qui dentro ci possono essere tutti i tipi di Expr anche la chiamata a funzione che restituisce una lista di stringhe
        //che indica i tipi di ritorno della funzione
        ArrayList<Object> parametriUtilizzati = new ArrayList<>();

        for (ExprOP exprOP: funCall.getExprs()) {
            parametriUtilizzati.add(exprOP.accept(this));
        }

        if(parametriDichiarati.size() != parametriUtilizzati.size()) {
            throw new RuntimeException("Parametri dichiarati nella chiamata di funzione non matchano quelli utilizzati");
        }


        //Adesso scorriamo sia la lista dei parametri utilizzati che quelli dichiarati e confrontiamo tipo per tipo
        Iterator<CallableParam> paramDichiaratiIterator = parametriDichiarati.iterator();
        Iterator<Object> paramUtilizzatiIterator = parametriUtilizzati.iterator();
        Iterator<ExprOP> itExprOPs = funCall.getExprs().iterator();

        while(paramUtilizzatiIterator.hasNext() && paramDichiaratiIterator.hasNext() && itExprOPs.hasNext())
        {
            //vediamo il vero tipo dell'object
            Object parametroUtilizzatoCorrente = paramUtilizzatiIterator.next();
            ExprOP exprOPcorrente = itExprOPs.next();
            //caso di chiamata a funzione, RICORDA la funzione restituisce un array di tipi di ritorno
            if (parametroUtilizzatoCorrente instanceof ArrayList<?>) {
                //iteratore sulla lista dei tipi di ritorno della funzione
                ArrayList<String> tipiDiRitornoFunzione = (ArrayList<String>) parametroUtilizzatoCorrente;
                if(tipiDiRitornoFunzione.size() > 1) {
                    throw new RuntimeException("Hai chiamato una funzione con più valori di ritorno in un'altra funzione");
                }

                if(exprOPcorrente.getMode().equals(ExprOP.Mode.PARAMSREF)) {
                    throw new RuntimeException("KEYWORD @ utilizzata in corrispondenza di una funzione");
                }
                CallableParam paramDichiarato = paramDichiaratiIterator.next();


                String tipoParametroDichiarato = null;
                try {
                    tipoParametroDichiarato = paramDichiarato.getTipo().getTipo();
                } catch (NoSuchElementException e) {
                    throw new RuntimeException("il numero dei parametri utilizzati sono diversi da quelli dichiarati");
                }

                String tipoParametroUtilizzato = tipiDiRitornoFunzione.get(0);


                if(!tipoParametroUtilizzato.equals(tipoParametroDichiarato)) {
                    throw new Exceptions.TypesMismatch(funCall.getIdentifier().getLessema(), tipoParametroDichiarato, tipoParametroUtilizzato);
                }

                    //CONTROLLO SULLA KEYWORD OUT
                if(paramDichiarato.getId().getMode().equals(ExprOP.Mode.PARAMSOUT)){
                    throw new RuntimeException("Non puoi utilizzare una funzione in corrispondenza di parametri out");
                }

            }

            //ogni altro caso, per exprOP
            else if(parametroUtilizzatoCorrente instanceof String) {
                //poi facciamo il confronto
                CallableParam parametroInTable = paramDichiaratiIterator.next();
                String parametroUtilizzatoCorrente_string = (String) parametroUtilizzatoCorrente;

                if(!exprOPcorrente.getMode().equals(ExprOP.Mode.PARAMSREF) && parametroInTable.getId().getMode().equals(ExprOP.Mode.PARAMSOUT)) {
                    throw new RuntimeException("type mismatch nella procedura: NON CI SIAMO CON LE OUT E I REF "); //TODO CUSTOM EXCEPTION
                }

                if(exprOPcorrente.getMode().equals(ExprOP.Mode.PARAMSREF) && !parametroInTable.getId().getMode().equals(ExprOP.Mode.PARAMSOUT)){
                    throw new RuntimeException("non si trovano out e ref"); //TODO CUSTOM EXCEPTION
                }

                // ora si vede se i tipi matchano
                String tipoParametroInTable = parametroInTable.getTipo().getTipo();
                if (!tipoParametroInTable.equalsIgnoreCase(parametroUtilizzatoCorrente_string)) {
                    throw new Exceptions.TypesMismatch(funCall.getIdentifier().getLessema(), parametroInTable.getTipo().toString(), parametroUtilizzatoCorrente_string);

                }
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
    public Object visit(CallableParam callableParam) throws RuntimeException{
        return null;
    }
}
