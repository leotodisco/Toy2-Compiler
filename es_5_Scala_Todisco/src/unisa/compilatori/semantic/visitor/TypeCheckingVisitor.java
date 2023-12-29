package unisa.compilatori.semantic.visitor;

import unisa.compilatori.Token;
import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.CallableFieldType;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.symboltable.SymbolTableRecord;
import unisa.compilatori.semantic.symboltable.VarFieldType;
import unisa.compilatori.utils.Exceptions;

import java.lang.reflect.Array;
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
    //TODO fare controllo se fai concat fra stringa e real
    private String evaluateType(String type1, String type2, String op) throws Exception {        switch (op){
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
                else {
                    throw new Exception("errore di tipo nella evaluate type, type1 = " + type1 + " type2 = " + type2);
                }

            case "OR", "AND":
                if(type1.equalsIgnoreCase("BOOLEAN") && type2.equalsIgnoreCase("BOOLEAN"))
                    return new String("bool");
                else
                    throw new Exception("errore");

            case "stringConcat":
                if(type1.equalsIgnoreCase("STRING_CONST") && type2.equalsIgnoreCase("STRING_CONST")) {
                    System.out.println("\nHAI TROVATO UNA STRING CONCAT\n");
                    return new String("STRING_CONST");
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
        ArrayList<String> tipiRestituiti = new ArrayList<>();

        //in tipi dichiarati ho delle stringhe con i tipi dei parametri dichiarati
        tipiDichiarati = funzione.getReturnTypes()
                .stream()
                .map(type -> type.getTipo().toString())
                .collect(Collectors.toCollection(ArrayList::new));


        //CONTROLLA CHE CI SIA UN RETURN
        if(funzione.getBody().getStatList()==null || funzione.getBody().getStatList().isEmpty()){
            try {
                throw new Exception("HAI UN BODY SENZA STATEMENT");
            } catch(Exception e){
                e.printStackTrace();
                System.exit(-1);
            }
        }

        //CONTROLLA CHE CI SIA ALMENO UN RETURN
        funzione.getBody()
                .getStatList()
                .stream().
                filter(stat -> stat.getTipo().equals(Stat.Mode.RETURN))
                        .findFirst()
                .orElseThrow(() -> new Exception("DEVI AVERE ALMENO UN RETURN"));



//TODO FARE CONTROLLO CHE IL RETURN ABBIA LO STESSO TIPO DELLA FUNZIONE

        //mi ricavo i returns
        ArrayList<Stat> returns = funzione.getBody().getStatList()
                .stream()
                .filter(stat -> stat.getTipo().equals(Stat.Mode.RETURN))
                .collect(Collectors.toCollection(ArrayList::new));

        //controllo che ogni return abbia i tipi uguali a quelli della dichiarazione
        for (Stat returnStat: returns) {
            ArrayList<String> tipiReturn = (ArrayList<String>) returnStat.accept(this);
            Iterator<String> itTipiReturn = tipiReturn.iterator();
            Iterator<String> itTipiDichiarati = tipiDichiarati.iterator();

            while(itTipiDichiarati.hasNext() && itTipiReturn.hasNext()) {
                if(!itTipiReturn.next().equals(itTipiDichiarati.next())) {
                    throw new Exception("I tipi dei parametri usati nel return non matchano con quelli usati nella funzione,\n" +
                            " tipi nel return" + tipiReturn +
                            " tipi nella dichiarazione" + tipiDichiarati);
                }
            }
        }


        //prendo i tipi effettivamente restituiti
        for(Stat stmt: funzione.getBody().getStatList()) {
            if (stmt.getTipo().equals(Stat.Mode.RETURN)){
                tipiRestituiti = stmt
                        .getEspressioniList()
                        .stream()
                        .map(expr -> {
                            try {
                                return (String) expr.accept(this); //TODO assicurarsi che questo return non causa problemi
                            } catch (Exception e) {
                                System.exit(-1);
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
            ((IfStat) statement).accept(this);
        }
        if(statement instanceof ProcCall) {
            ((ProcCall) statement).accept(this);
        }

        //TODO sistemare tenendo conto che puoi avere che funcall restituisce un'arraylist di tipi
        if(statement.getTipo().equals(Stat.Mode.ASSIGN)) {
            //uno statement di questo tipo ha un array di id e un array di espressioni
            var leftSide = statement.getIdsList();
            var rightSide = statement.getEspressioniList();

            var iteratoreLeftSide = leftSide.iterator();
            var iteratoreRightSide = rightSide.iterator();

            while(iteratoreLeftSide.hasNext() && iteratoreRightSide.hasNext())  {
                var actualId = iteratoreLeftSide.next();
                var actualExpression = iteratoreRightSide.next();

                String tipoId = (String) actualId.accept(this);
                String tipoEspressione = "";
                try {
                    tipoEspressione = (String) actualExpression.accept(this);
                } catch (Exception e) {
                   e.printStackTrace();
                }

                if(!tipoId.equalsIgnoreCase(tipoEspressione)){
                    try {
                        throw new Exception("type mismatch: "+ actualId.getLessema() + " ha tipo " + tipoId + " ma gli stai assegnando il tipo: "  + tipoEspressione); //TODO CUSTOM EXCEPTION
                    } catch (Exception e){
                        System.out.println("tipo id = " + tipoId + "tipo espressione = " + tipoEspressione);
                        e.printStackTrace();
                    }
                }

            }


        }

        /**
         * Si controlla che gli id nella read siano stati tutti dichiarati precedentemente
         */
        if(statement.getTipo().equals(Stat.Mode.READ)) {

            //TODO TESTARE
            statement.getEspressioniList().forEach(exprOP -> {
                try {
                    exprOP.accept(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        //devo controllare che stia all'interno di una funzione e non di una procedura
        //devo controllare che l'espressione return abbia un tipo compatibile col tipo di ritorno di una funzione
        //devo controllare che gli ipotetici id siano dichiarati
        if(statement.getTipo().equals(Stat.Mode.RETURN)) {
            //devo controllare che il tipi di ritorno della funzione matchano
            //con i tipi effettivamente restituiti
            ArrayList<String> tipiDiRitorno = new ArrayList<>();
            statement.getEspressioniList().forEach(exprOP -> {
                try {
                    tipiDiRitorno.add((String) exprOP.accept(this));
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

        if (!tipoExpr.equalsIgnoreCase("boolean"))
            throw new RuntimeException("condizione nell'expr dell'else if errata");//TODO ECCEZIONE CUSTOM

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
            throw new Exception("il numero di parametri in procedura non matcha");
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
                        throw new Exception("type mismatch nella procedura: " + procCall.getIdentifier().getLessema() +
                                " è stato dichiarato con tipo: " + tipoParametroDichiarato +
                                "ma lo usi con tipo: " + tipoParametroUtilizzato);
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
                    throw new Exception("type mismatch nella procedura: " + procCall.getIdentifier().getLessema()
                            + "parametro: " + parametroInTable + " è stato dichiarato con tipo: " + parametroInTable.getTipo() +
                            "ma lo usi con tipo:" + parametroUtilizzatoCorrente_string);
                }
            }


        }
            if(paramUtilizzatiIterator.hasNext() || paramDichiaratiIterator.hasNext()) {
                throw new Exception("i parametri utilizzati sono diversi da quelli dichiarati");
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
        //var tipo = decl.getTipo().getTipo();
        var tipo = "";

        // se il tipo di dichiarazione è del tipo var a ^=2;\
        if(decl.getTipoDecl().toString().equals("ASSIGN")) {
            /**
             * fatti 2 classi: una per le const e una per gli id
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

                int lunghezza1 = costanteAttuale.getType().toString().length();

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
        System.out.println("LOG "+ "id = "+ id.getLessema() + " tipo = " + varFieldType.getType());
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

    //TODO impementare
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

    //TODO fare che non ci devono essere return in una procedura
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
    public Object visit(FunCall funCall) throws Exception {
        System.out.println("FUNZIONEEEEEEEEE\n");

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
            if(!tipoCallableParam.equalsIgnoreCase(tipoExpr)) {
                //System.out.println("tipo nella Dichiarazione " + tipoCallableParam + " tipo nella chiamata: " + tipoExpr);
                throw new Exception("I TIPI NON MATCHANO NELLA FUNZIONE"); //TODO CUSTOM EXCEPTION
            }
        }
        System.out.println("proprietà" + record.getProperties());

        var tipiDiRitorno = new ArrayList<>(Arrays.asList(record.getProperties().split(";")));

        System.out.println(tipiDiRitorno);

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
