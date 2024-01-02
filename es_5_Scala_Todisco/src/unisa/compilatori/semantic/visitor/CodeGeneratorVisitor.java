package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.CallableFieldType;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.symboltable.SymbolTableRecord;
import unisa.compilatori.semantic.symboltable.VarFieldType;


import java.io.File;
import java.io.FileWriter;

import java.nio.file.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class CodeGeneratorVisitor implements Visitor {
    private SymbolTable currentScope;
    private static File outFile;
    private FileWriter writer;
    public static String FILE_NAME = "output.c";

    private int countFunCall = 0;


    public void enterScope(SymbolTable scope) {
        this.currentScope = scope;
    }

    public void exitScope() {
        this.currentScope = (SymbolTable) this.currentScope.getFather();
    }

    @Override
    public Object visit(ProgramOp program) {
        currentScope = program.getTable();

        // controllo che esista la directory dei file e inizializzo il file
        try {
            if (!(new File("test_files" + File.separator + "c_out" + File.separator)).exists()) {
                Files.createDirectory(Paths.get("test_files" + File.separator + "c_out" + File.separator));
            }
            outFile = new File("test_files" + File.separator + "c_out" + File.separator + FILE_NAME);
            outFile.createNewFile();
            writer = new FileWriter(outFile);

            //aggiunge librerie di base come stdio.h
            CodeGeneratorUtils.addBaseLibraries(this.writer);
            //aggiunge funzioni di utilità
            CodeGeneratorUtils.addHelperFunctions(this.writer);

            //scriviamo i nomi di tutte le funzioni
            //TODO Se togliamo iterWithoutProcedure bisogna cambiare anche questo metodo
            CodeGeneratorUtils.addFunctionSignatures(this.writer, program.getIterOp(), program.getIterWithoutProcedure(), program.getProc());

            program.getIterWithoutProcedure().accept(this);
            program.getIterOp().accept(this);
            program.getProc().accept(this);

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object visit(IterWithoutProcedure iterWithoutProcedure) throws RuntimeException {
        //aggiungere firme delle funzioni e delle procedure ad inizio file
        iterWithoutProcedure.getDeclarations().forEach(varDecl -> varDecl.accept(this));
        iterWithoutProcedure.getFunctions().forEach(funzione->funzione.accept(this));
        return null;
    }

    @Override
    public Object visit(IterOp iterOP) throws RuntimeException {
        iterOP.getDeclarations().forEach(varDecl -> varDecl.accept(this));
        iterOP.getProcedures().forEach(procedure -> procedure.accept(this));

        iterOP.getFunctions().forEach(funzione->funzione.accept(this));
        return null;
    }


    @Override
    public Object visit(Procedure procedure) throws RuntimeException {
        String idProcedura = procedure.getId().getLessema();
        ArrayList<CallableParam> parametri = procedure.getProcParamDeclList();

        enterScope(procedure.getTable());
        try{
            writer.append("\n\n");
            CodeGeneratorUtils.scriviSingolaProceduraSignature(writer, procedure);
            writer.append("{\n");
            procedure.getBody().accept(this);
            writer.append("}\n");

        }catch(Exception e){
            e.printStackTrace();
        }
        exitScope();


        return null;
    }

    @Override
    public Object visit(BinaryOP operazioneBinaria) throws RuntimeException {
        String expr1 = (String) operazioneBinaria.getExpr1().accept(this);
        String expr2 = (String) operazioneBinaria.getExpr2().accept(this);

        String lessemaOperazione = CodeGeneratorUtils.convertOperations(operazioneBinaria.getName()); //ottieni il lessema giusto per l'operazione
        if(lessemaOperazione.equalsIgnoreCase("strcat")) {
            //todo fare controlli per vedere di che tipo sono le due espressioni
            //a quel punto puoi richiamare la funzione helper che converte quel tipo specifico


            return "str_concat(" + expr1 + ", "+ expr2+")";
        }

        return expr1 + " " + lessemaOperazione + " " + expr2;
    }

    @Override
    public Object visit(UnaryOP operazioneUnaria) throws RuntimeException {
        String expr = (String) operazioneUnaria.getExpr().accept(this);

        String lessemaOperazione = CodeGeneratorUtils.convertOperations(operazioneUnaria.getSimbolo());

        return lessemaOperazione + " " + expr;
    }

    @Override
    public Object visit(VarDecl dichiarazione) throws RuntimeException {
        dichiarazione.getDecls().forEach(decl -> decl.accept(this));

        return null;
    }

    @Override
    public Object visit(Function funzione) throws RuntimeException {
        String idFunzione = funzione.getId().getLessema();
        var parametri = funzione.getParametersList();
        boolean ritornaStruct = funzione.getReturnTypes().size()>1;
        StringBuilder signature = new StringBuilder();

        try{
            if(ritornaStruct){
                signature.append("result_" + idFunzione);
            }
            else{
                signature.append(CodeGeneratorUtils.convertType(funzione.getReturnTypes().get(0).getTipo()));
            }
            signature.append(" ");

            signature.append(idFunzione);
            signature.append("(");

            //aggiungo parametri della funzione
            for(CallableParam param : parametri) {
                //aggiungo il tipo
                signature.append(CodeGeneratorUtils.convertType(param.getTipo().getTipo()));
                signature.append(" ");
                //aggiungo l'id del parametro
                signature.append(param.getId().getLessema());
                signature.append(",");
            }
            //elimino l'ultimo "," aggiunto alla fine dei parametri
            signature.deleteCharAt(signature.length()-1);
            writer.write(signature.toString());


            enterScope(funzione.getTable());
            writer.append(") {\n");
            funzione.getBody().accept(this);
            writer.append("}\n");

        }catch(Exception e){
            e.printStackTrace();
        }
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
        if (statement.getTipo().equals(Stat.Mode.ASSIGN)) {
            //lo statement ha lista di id e corrispondente lista di exprs

            var listaId = statement.getIdsList();
            var listaEspressioni = statement.getEspressioniList();
            Iterator<Identifier> itListaId = listaId.iterator();
            Iterator<ExprOP> itListaEspressioni = listaEspressioni.iterator();

            while(itListaId.hasNext() && itListaEspressioni.hasNext()) {
                Identifier id = itListaId.next();
                ExprOP espressione = itListaEspressioni.next();

                String lessemaId = (String) id.accept(this);
                var lessemaOperazione = espressione.accept(this);
                //a,b,c ^= foo(), 12;
                //
                //result_foo r1 = foo();
                //
                //a = r1.result0;
                //b = r1.result1;
                //c = 12;
                if(espressione instanceof FunCall){
                    try {
                        writer.write((String)lessemaOperazione);
                    }catch (Exception e) {

                    }


                    int countResults = ((FunCall) espressione).getExprs().size();
                    for(int i = 0; i < countResults; i++) {

                        System.out.println(countResults);
                        System.out.println(id);
                        try {
                            writer.write(lessemaId + " = r" + countFunCall+ ".result" + i +";\n");

                            if(itListaId.hasNext()) {
                                id = itListaId.next();
                                lessemaId = id.getLessema();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                    
                    countFunCall++;
                }
                else {
                    //Quando l' expr non è una chimaata a funzione ho solo una stringa
                    try{
                        writer.write(lessemaId + " = " + (String) lessemaOperazione+";\n");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
            for(int i = 0; i < listaId.size(); i++) {

            }
        }
        //TODO implementare statement return, write, write-return
        if (statement.getTipo().equals(Stat.Mode.RETURN)) {
        }
        if (statement.getTipo().equals(Stat.Mode.READ)) {
            var listaEspresioni = statement.getEspressioniList();

            List<ExprOP> espressioniDaStampare = listaEspresioni
                                    .stream()
                                    .filter(exprOP -> !exprOP.getMode().equals(ExprOP.Mode.IOARGSDOLLAR))
                                    .collect(Collectors.toList());

            List<Identifier> listaPerScanf = listaEspresioni
                    .stream()
                    .filter(exprOP -> exprOP.getMode().equals(ExprOP.Mode.IOARGSDOLLAR))
                    .filter(exprOP -> exprOP instanceof Identifier)
                    .map(exprOP -> (Identifier) exprOP)
                    .toList();


            //controlla se la lista per scanf va reversata
            try {
                //PRINTF
                for(ExprOP espressione : espressioniDaStampare) {
                    if(espressione instanceof ConstOP) {
                        writer.write("printf("+(String) espressione.accept(this)+");\n");
                    }
                    if(espressione instanceof Identifier) {
                        Identifier id = (Identifier) espressione;
                        SymbolTableRecord record = this.currentScope.lookup(id.getLessema()).get();
                        VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                        if(varFieldType.getType().equalsIgnoreCase("real")) {
                            writer.write("printf( \"%lf\", "+id.getLessema()+");\n" );
                        }
                        if(varFieldType.getType().equalsIgnoreCase("integer")) {
                            writer.write("printf( \"%d\", "+id.getLessema()+");\n" );
                        }
                        if(varFieldType.getType().equalsIgnoreCase("string")) {
                            writer.write("printf( \"%s\", "+id.getLessema()+");\n" );
                        }
                    }
                    if(espressione instanceof BinaryOP) {//se è concatenazione di stringhe
                        BinaryOP operazione = (BinaryOP) espressione;

                        if(operazione.getName().equalsIgnoreCase("stringConcat")) {
                            writer.write("printf( \"%s\", "+(String)operazione.accept(this)+");\n" );
                        }
                    }
                }

                //SCANF
                for(Identifier id : listaPerScanf) {
                    SymbolTableRecord record = this.currentScope.lookup(id.getLessema()).get();
                    VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                    if(varFieldType.getType().equalsIgnoreCase("real")) {
                        writer.write("scanf( \"%lf\", &"+id.getLessema()+");\n" );
                    }
                    if(varFieldType.getType().equalsIgnoreCase("integer")) {
                        writer.write("scanf( \"%d\", &"+id.getLessema()+");\n" );
                    }
                    if(varFieldType.getType().equalsIgnoreCase("string")) {
                        writer.write("scanf( \"%s\", &"+id.getLessema()+");\n" );
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        if (statement.getTipo().equals(Stat.Mode.WRITE)) {

        }
        if (statement.getTipo().equals(Stat.Mode.WRITE_RETURN)) {
        }

        return null;
    }


    @Override
    public Object visit(ProcCall procCall) throws RuntimeException {
        var lessemaId = procCall.getIdentifier().getLessema();
        var parametri = procCall.getExprs();

        try{
            writer.write(lessemaId + "(");

            if(parametri == null || parametri.isEmpty()){
                System.out.println("sono in parametri null o vuoto");
                writer.write(");\n");
                return null;
            }

            for(int i = 0; i < parametri.size(); i++) {
                ExprOP parametroAttuale = parametri.get(i);
                if(i == parametri.size()-1){
                    String result = (String) parametroAttuale.accept(this);
                    if (result != null) {
                        writer.write(result);
                    }

                    continue;
                }
                writer.write((String) parametroAttuale.accept(this) + ",");
            }
            writer.write(");\n");
        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object visit(WhileStat whileStat) throws RuntimeException {
        enterScope(whileStat.getTable()); //entro nello scope del while
        String condizione = (String) whileStat.getExpr().accept(this); //ottieni la condizione

        try {
            writer.write("while (" + condizione + ") { \n"); // while (true) {
            whileStat.getBody().accept(this); //traduci il body
            writer.write("\n}\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        exitScope();

        return null;
    }

    @Override
    public Object visit(Body body) throws RuntimeException {
        body.getVarDeclList().forEach(varDecl -> varDecl.accept(this));

        var copiedStatList = new ArrayList<>(body.getStatList());
        Collections.reverse(copiedStatList);

        copiedStatList.forEach(stat -> stat.accept(this));

        return null;
    }

    @Override
    public Object visit(Type type) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(ProcParamDecl procParam) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(FunctionParam functionParam) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(Decl decl) throws RuntimeException {
        if (decl.getTipoDecl().equals(Decl.TipoDecl.TYPE)) {
            //tipo listaID ;
            try {
                writer.write(CodeGeneratorUtils.convertType(decl.getTipo().getTipo()));

                for (int i = 0; i < decl.getIds().size(); i++) {
                    Identifier idParametro = decl.getIds().get(i);
                    String id = (String) idParametro.accept(this);

                    writer.append(" ");
                    writer.append(id);

                    //se non è l'ultimo elemento metti la virgola
                    if (i != decl.getIds().size() - 1)
                        writer.append(",");
                    else
                        writer.append(";\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (decl.getTipoDecl().equals(Decl.TipoDecl.ASSIGN)) {
            for (int i = 0; i < decl.getIds().size(); i++) {
                Identifier id = decl.getIds().get(i);
                ConstOP costante = decl.getConsts().get(i);

                String tipo = CodeGeneratorUtils.convertType(costante.getType().toString());
                String lessemaId = (String) id.accept(this);
                String lessemaCostante = (String) costante.accept(this);

                try {
                    writer.write(tipo + " " + lessemaId + " = " + lessemaCostante + ";\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    public Object visit(Identifier id) throws RuntimeException {
        var lessema = id.getLessema();
        SymbolTableRecord result = currentScope.lookup(lessema).get();

        if (id.getMode().equals(ExprOP.Mode.VARIABLENAME) || id.getMode().equals(ExprOP.Mode.PARAMS) || id.getMode().equals(ExprOP.Mode.NONE)) {
            return lessema;
        } else if (id.getMode().equals(ExprOP.Mode.PARAMSOUT)) {
            return "*" + lessema;
        } else if (id.getMode().equals(ExprOP.Mode.PARAMSREF)) {
            return "&" + lessema;
        }

        return lessema;
    }


    @Override
    public Object visit(ConstOP constOP) {
        var tipoCostante = constOP.getType();

        if (tipoCostante.equals(ConstOP.Kind.STRING)) {
            return "\"" + constOP.getLessema() + "\"";
        }

        return constOP.getLessema();
    }

    private void ricorsioneFunCall(FunCall funCall, StringBuilder stringBuilder) {
        if(funCall.getExprs()==null ||funCall.getExprs().isEmpty()) {
            stringBuilder.append("result_" + funCall.getIdentifier().getLessema()+ " r" +countFunCall+ " " +funCall.getIdentifier().getLessema() + "();\n");
            return;
        }

        ArrayList<String> param = new ArrayList<>();

        ArrayList<String> valoriAssegnati = new ArrayList<>();

        for(ExprOP exprOP: funCall.getExprs()) {
            ArrayList<String> nomiId = null;
            String nonFunCall;

            if (exprOP instanceof FunCall) {
                ricorsioneFunCall((FunCall) exprOP, stringBuilder);

            } else {
                stringBuilder.append("tipo valore" + countFunCall + " = " + (String)exprOP.accept(this) + ";\n");
            }


        }

        SymbolTableRecord funzioneChiamata = this.currentScope.lookup(funCall.getIdentifier().getLessema()).orElseThrow();
        ArrayList<String> tipiRitornatiFunzione = new ArrayList<>(Arrays.asList(funzioneChiamata.getProperties().split(";")));


        int count_param = 0;
        Iterator<String> itTipiRitornatiFunzione = tipiRitornatiFunzione.iterator();
        while (itTipiRitornatiFunzione.hasNext()) {break;}
        for(String tipo : tipiRitornatiFunzione) {


            stringBuilder.append(tipo + " r" + countFunCall + "_" + count_param +" = \n" );
            count_param++;
        }

        stringBuilder.append("result_" + funCall.getIdentifier().getLessema()+ " r" +countFunCall+ " = "  + funCall.getIdentifier().getLessema() + "(");
        count_param = 0;
        for(String tipo : tipiRitornatiFunzione) {
            stringBuilder.append( "r" + countFunCall + "_" + count_param +" ,");
            count_param++;
        }

        stringBuilder.append( ");\n\n");
    }

    Stack<FunCall> stackFunzioni = new Stack<>();
    @Override
    public Object visit(FunCall funCall) throws RuntimeException {
        //a,b,c ^= foo(), 12;
        //
        //result_foo r1 = foo();
        //
        //a = r1.result0;
        //b = r1.result1;
        //c = 12;
        /*
        String idFunCall = funCall.getIdentifier().getLessema();
        StringBuilder resultStringFunCall = new StringBuilder();

        StringBuilder exprOPStringBuilder = new StringBuilder();

        try {
            if(funCall.getExprs()!= null && !funCall.getExprs().isEmpty()) {
                for(ExprOP exprOp: funCall.getExprs()) {
                    String exprOpResult = (String) exprOp.accept(this);

                    if(exprOp instanceof FunCall) {
                        exprOPStringBuilder.append(exprOpResult);
                    } else {
                        exprOPStringBuilder.append("r = " + exprOpResult + "\n");
                    }

                    exprOPStringBuilder.append(" ");
                }
                exprOPStringBuilder.append(" ");
                exprOPStringBuilder.deleteCharAt(exprOPStringBuilder.length()-1);
            }

            resultStringFunCall.append(exprOPStringBuilder.toString());

            resultStringFunCall.append("result_" + idFunCall + " r" + countFunCall + " =");
            resultStringFunCall.append(idFunCall + "(");

            resultStringFunCall.append(");\n\n");
        } catch (Exception e ) {
            e.printStackTrace();
        }
        */

        //Lookup Della Funzione
        Function function = (Function) this.currentScope.lookup(funCall.getIdentifier().getLessema()).orElseThrow().getNodo();
        //Mi prendo i tipi di ritorno della funzione
        ArrayList<Type> tipiDiRitorno = function.getReturnTypes();




        return null;
    }

    @Override
    public Object visit(ExprOP exprOP) throws RuntimeException {
        if(exprOP instanceof ConstOP)
            return (String) ((ConstOP) exprOP).accept(this);
        if(exprOP instanceof BinaryOP)
            return (String) ((BinaryOP) exprOP).accept(this);
        if(exprOP instanceof UnaryOP)
            return (String) ((UnaryOP) exprOP).accept(this);
        if(exprOP instanceof Identifier)
            return (String) ((Identifier) exprOP).accept(this);
        if(exprOP instanceof FunCall)
            return (String) ((FunCall) exprOP).accept(this);

        return null;
    }

    @Override
    public Object visit(CallableParam callableParam) throws RuntimeException {
        return null;
    }
}
