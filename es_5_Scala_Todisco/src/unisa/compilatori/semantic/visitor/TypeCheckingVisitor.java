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
                else if (type1.equals("INTEGER") && type2.equals("REAL"))
                    return new String("REAL_CONST");
                else if (type1.equals("REAL_CONST") && type2.equals("INTEGER_CONST"))
                    return new String("REAL_CONST");
                else if (type1.equals("REAL_CONST") && type2.equals("REAL_CONST"))
                    return new String("REAL_CONST");
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

            case "GT", "GE", "LT", "LE":
                if(type1.equals("INTEGER_CONST") && type2.equals("INTEGER_CONST"))
                    return new String("BOOLEAN_CONST");
                else if(type1.equals("REAL_CONST") && type2.equals("INTEGER_CONST"))
                    return new String("BOOLEAN_CONST");
                else if(type1.equals("INTEGER_CONST") && type2.equals("REAL_CONST"))
                    return new String("BOOLEAN_CONST");
                else if(type1.equals("REAL_CONST") && type2.equals("REAL_CONST"))
                    return new String("BOOLEAN_CONST");
                else
                    throw new Exception("errore");

            case "eq", "ne":
                if(type1.equals(type2))
                    return new String("BOOLEAN_CONST");
                else
                    throw new Exception("errore");
        }
        return null;
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
        return null;
    }

    @Override
    public Object visit(VarDecl dichiarazione) {
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

        if (!tipoExpr.equals("boolean")) {
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

        if( !condition.equals("boolean")){
            //TODO ECCEZIONE
            throw new Exception("Errore, tipo della condizione in while stat non corretto");
        }

        enterScope(whileStat.getTable());
        whileStat.getBody().accept(this);
        exitScope();
        return null;
    }

    @Override
    public Object visit(IOArgsOp ioArgsOp) {
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
