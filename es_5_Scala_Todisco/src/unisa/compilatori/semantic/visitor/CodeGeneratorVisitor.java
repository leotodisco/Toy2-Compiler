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

import java.io.Writer;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (lessemaOperazione.equalsIgnoreCase("strcat")) {
            //todo fare controlli per vedere di che tipo sono le due espressioni
            //a quel punto puoi richiamare la funzione helper che converte quel tipo specifico

            if (operazioneBinaria.getExpr1() instanceof Identifier) {
                var id = ((Identifier) operazioneBinaria.getExpr1()).getLessema();
                var record = this.currentScope.lookup(id).get();
                if (record.getFieldType() instanceof VarFieldType) {
                    VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                    if (varFieldType.getType().equalsIgnoreCase("integer")) {
                        return "str_concat(" + "integer_to_str(" + expr1 + ")" + ", " + expr2 + ")";
                    }
                    if (varFieldType.getType().equalsIgnoreCase("real")) {
                        return "str_concat(" + "real_to_str(" + expr1 + ")" + ", " + expr2 + ")";
                    }
                    if (varFieldType.getType().equalsIgnoreCase("boolean")) {
                        return "str_concat(" + "bool_to_str(" + expr1 + ")" + ", " + expr2 + ")";
                    }
                }
            }

            if (operazioneBinaria.getExpr1() instanceof ConstOP) {
                var tipo = ((ConstOP) operazioneBinaria.getExpr1()).getType();
                if (tipo.toString().equalsIgnoreCase("integer")) {
                    return "str_concat(" + "integer_to_str(" + expr1 + ")" + ", " + expr2 + ")";
                }
                if (tipo.toString().equalsIgnoreCase("real")) {
                    return "str_concat(" + "real_to_str(" + expr1 + ")" + ", " + expr2 + ")";
                }
                if (tipo.toString().equalsIgnoreCase("boolean")) {
                    return "str_concat(" + "bool_to_str(" + expr1 + ")" + ", " + expr2 + ")";
                }
            }


            if (operazioneBinaria.getExpr2() instanceof ConstOP) {
                var tipo = ((ConstOP) operazioneBinaria.getExpr2()).getType();
                if (tipo.toString().equalsIgnoreCase("integer")) {
                    return "str_concat(" + expr1 + ", " + "integer_to_str(" + expr2 + ")) ";
                }
                if (tipo.toString().equalsIgnoreCase("real")) {
                    return "str_concat(" + expr1 + ", " + "real_to_str(" + expr2 + ")) ";
                }
                if (tipo.toString().equalsIgnoreCase("boolean")) {
                    return "str_concat(" + expr1 + ", " + "bool_to_str(" + expr2 + ")) ";
                }
            }


            if (operazioneBinaria.getExpr2() instanceof Identifier) {
                var id = ((Identifier) operazioneBinaria.getExpr2()).getLessema();
                var record = this.currentScope.lookup(id).get();
                if (record.getFieldType() instanceof VarFieldType) {
                    VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                    if (varFieldType.getType().equalsIgnoreCase("integer")) {
                        return "str_concat(" + expr1 + ", " + "integer_to_str(" + expr2 + "))";
                    }
                    if (varFieldType.getType().equalsIgnoreCase("real")) {
                        return "str_concat(" + expr1 + ", " + "real_to_str(" + expr2 + "))";
                    }
                    if (varFieldType.getType().equalsIgnoreCase("boolean")) {
                        return "str_concat(" + expr1 + ", " + "bool_to_str(" + expr2 + "))";
                    }
                }
            }

            if (operazioneBinaria.getExpr1() instanceof FunCall) {
                FunCall chiamataAFunzione = (FunCall) operazioneBinaria.getExpr1();
                var record = currentScope.lookup(chiamataAFunzione.getIdentifier().getLessema());
                var tipoRitorno = (Function) (record.get().getNodo());
                if (tipoRitorno.getReturnTypes().size() > 1) {
                    //gestisci più return types appena decidi che fare
                    var tipo = tipoRitorno.getReturnTypes().get(0);
                }
                var tipo = tipoRitorno.getReturnTypes().get(0);

                if (tipo.toString().equalsIgnoreCase("integer")) {
                    return "str_concat(" + "integer_to_str(" + expr1 + ")" + ", " + expr2 + ")";
                }
                if (tipo.toString().equalsIgnoreCase("real")) {
                    return "str_concat(" + "real_to_str(" + expr1 + ")" + ", " + expr2 + ")";
                }
                if (tipo.toString().equalsIgnoreCase("boolean")) {
                    return "str_concat(" + "bool_to_str(" + expr1 + ")" + ", " + expr2 + ")";
                }
            }

            if (operazioneBinaria.getExpr2() instanceof FunCall) {
                FunCall chiamataAFunzione = (FunCall) operazioneBinaria.getExpr2();
                var record = currentScope.lookup(chiamataAFunzione.getIdentifier().getLessema());
                var tipoRitorno = (Function) (record.get().getNodo());
                if (tipoRitorno.getReturnTypes().size() > 1) {
                    //gestisci più return types appena decidi che fare
                    var tipo = tipoRitorno.getReturnTypes().get(0);
                }
                var tipo = tipoRitorno.getReturnTypes().get(0);

                if (tipo.toString().equalsIgnoreCase("integer")) {
                    return "str_concat("  + expr1 + ", " + "integer_to_str(" + expr2 + "))";
                }
                if (tipo.toString().equalsIgnoreCase("real")) {
                    return "str_concat("  + expr1 + ", " + "real_to_str(" + expr2 + "))";
                }
                if (tipo.toString().equalsIgnoreCase("boolean")) {
                    return "str_concat("  + expr1 + ", " + "bool_to_str(" + expr2 + "))";
                }
            }

            if(operazioneBinaria.getExpr1() instanceof BinaryOP) {
                String tipoE1 = (String) ((BinaryOP) operazioneBinaria.getExpr1()).getExpr1().accept(this);
                String tipoE2 = (String) ((BinaryOP) operazioneBinaria.getExpr1()).getExpr2().accept(this);
            }


            return "str_concat(" + expr1 + ", " + expr2 + ")";
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
            signature.append("result_" + idFunzione);

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
            if( !parametri.isEmpty()) {
                signature.deleteCharAt(signature.length()-1);
            }

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
                int temp = this.funCallCount + 1;
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
                        e.printStackTrace();
                    }

                    SymbolTableRecord recordFunzione = this.currentScope.lookup(((FunCall) espressione).getIdentifier().getLessema()).orElseThrow();
                    ArrayList<String> tipiDiRitornoFunzione = new ArrayList<>(Arrays.asList(recordFunzione.getProperties().split(";")));

                    int countResults = tipiDiRitornoFunzione.size();
                    for(int i = 0; i < countResults; i++) {

                        try {
                            writer.write(lessemaId + " = r_" + temp+ ".result" + i +";\n");

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
            List<ExprOP> listaFunCall = statement.getEspressioniList().stream().filter(exprOP -> exprOP instanceof FunCall).toList();
            StringBuffer daRestituire = new StringBuffer();

            ArrayList<Integer> idStructsFunzioni = new ArrayList<>();
            for (ExprOP funCall : listaFunCall) {
                try {
                    int currentFunCallCount = this.funCallCount + 1;
                    writer.write((String)funCall.accept(this));
                    idStructsFunzioni.add(currentFunCallCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Iterator<Integer> itIdStructsFunzioni = idStructsFunzioni.iterator();

            //lookup per scoprire il nome della funzione del return per costruire il tipo di ritorno
            Function funzioneDiAssign = (Function) statement.getParent().getParent();
            try {
                writer.write("result_" + funzioneDiAssign.getId().getLessema() + " daRestituire;\n");
            } catch (Exception e) {
                e.printStackTrace();
            }


            for(int i = 0; i < statement.getEspressioniList().size(); i++) {
                ExprOP exprOP = statement.getEspressioniList().get(i);

                if(exprOP instanceof FunCall) {

                    //lookup di funcal per scoprire quanti tipi restituisce
                    SymbolTableRecord funzioneChiamataCorrente = this.currentScope.lookup(((FunCall) exprOP).getIdentifier().getLessema()).orElseThrow();
                    ArrayList<String> tipiDiRitornoFunCallCorrente = new ArrayList<>(Arrays.asList(funzioneChiamataCorrente.getProperties().split(";")));

                    int idStruct = itIdStructsFunzioni.next();
                    for(int j = 0; j < tipiDiRitornoFunCallCorrente.size(); j++) {
                        daRestituire.append("daRestituire." + "result" + i + "=" + "r_" + idStruct + ".result"+ j + ";\n");
                        i++;
                    }

                } else {
                    daRestituire.append("daRestituire." + "result" + i + "=" + exprOP.accept(this)+ ";\n");
                }
            }

            try {
                writer.write(daRestituire.toString());
                writer.write("return " + "daRestituire" + ";\n");
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        if (statement.getTipo().equals(Stat.Mode.READ)) {
            var listaEspresioni = statement.getEspressioniList();
            Collections.reverse(listaEspresioni);

            try{
                for (ExprOP espressione : listaEspresioni) {
                    if(espressione.getMode().equals(ExprOP.Mode.IOARGSDOLLAR)) {
                        Identifier id = (Identifier) espressione;
                        SymbolTableRecord record = this.currentScope.lookup(id.getLessema()).get();
                        VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                        if(varFieldType.getType().equalsIgnoreCase("real")) {
                            writer.write("scanf( \"%f\", &"+id.getLessema()+");\n" );
                        }
                        if(varFieldType.getType().equalsIgnoreCase("integer")) {
                            writer.write("scanf( \"%d\", &"+id.getLessema()+");\n" );
                        }
                        if(varFieldType.getType().equalsIgnoreCase("string")) {
                            writer.write("scanf( \"%s\", &"+id.getLessema()+");\n" );
                        }
                    }
                    else{
                        if(espressione instanceof ConstOP) {
                            writer.write("printf("+(String) espressione.accept(this)+");\n");
                        }
                        if(espressione instanceof Identifier) {
                            Identifier id = (Identifier) espressione;
                            SymbolTableRecord record = this.currentScope.lookup(id.getLessema()).get();
                            VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                            if(varFieldType.getType().equalsIgnoreCase("real")) {
                                writer.write("printf( \"%f\", "+id.getLessema()+");\n" );
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

                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        if (statement.getTipo().equals(Stat.Mode.WRITE)) {

            List<ExprOP> listaFunCall = statement.getEspressioniList().stream().filter(exprOP -> exprOP instanceof FunCall).toList();

            ArrayList<Integer> idStructsFunzioni = new ArrayList<>();
            for (ExprOP funCall : listaFunCall) {
                try {
                    int currentFunCallCount = this.funCallCount + 1;
                    writer.write((String)funCall.accept(this));
                    idStructsFunzioni.add(currentFunCallCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            Iterator<Integer> itIdStructsFunzioni = idStructsFunzioni.iterator();

            //lookup per scoprire il nome della funzione del return per costruire il tipo di ritorno

            StringBuilder daRestituire = new StringBuilder();

            for(int i = 0; i < statement.getEspressioniList().size(); i++) {
                ExprOP exprOP = statement.getEspressioniList().get(i);

                if(exprOP instanceof FunCall) {

                    //lookup di funcal per scoprire quanti tipi restituisce
                    SymbolTableRecord funzioneChiamataCorrente = this.currentScope.lookup(((FunCall) exprOP).getIdentifier().getLessema()).orElseThrow();
                    ArrayList<String> tipiDiRitornoFunCallCorrente = new ArrayList<>(Arrays.asList(funzioneChiamataCorrente.getProperties().split(";")));

                    int idStruct = itIdStructsFunzioni.next();
                    int k = i;
                    for(int j = 0; j < tipiDiRitornoFunCallCorrente.size(); j++) {
                        daRestituire.append("daRestituire." + "result" + k + "=" + "r_" + idStruct + ".result"+ j + ";\n");
                        k++;
                    }

                    for(int j = 0; j < tipiDiRitornoFunCallCorrente.size(); j++) {
                        if(tipiDiRitornoFunCallCorrente.get(j).equalsIgnoreCase("integer")) {
                            daRestituire.append("printf(\"%d \", daRestituire." + "result" + j +");");
                        }
                        System.out.println(tipiDiRitornoFunCallCorrente.get(j));
                        if(tipiDiRitornoFunCallCorrente.get(j).equalsIgnoreCase("string")) {
                            daRestituire.append("printf(\"%s \", daRestituire." + "result" + j +");");
                            System.out.println("SONO IN UNA STRINGA");
                        }

                        //i++;
                    }
                } else {
                    daRestituire.append("daRestituire." + "result" + i + "=" + exprOP.accept(this)+ ";\n");
                }
            }

            try {
                System.out.println(daRestituire.toString());
                writer.write(daRestituire.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (statement.getTipo().equals(Stat.Mode.WRITE_RETURN)) {
        }

        return null;
    }
    @Override
    public Object visit(IfStat ifStat) throws RuntimeException {
        try{
            writer.write("if( ");
            writer.append((String)ifStat.getExpr().accept(this));
            writer.append(") {\n");
            enterScope(ifStat.getSymbolTableThen());
            //scrive il then
            ifStat.getBody().accept(this);
            exitScope();
            writer.append("}\n"); //finisce il then

            //se ho degli else if li scrivo
            if(!ifStat.getElseIfOPList().isEmpty()) {
                ifStat.getElseIfOPList().forEach(elseIfOP -> elseIfOP.accept(this));
            }

            //scrivo l'else se è presente
            if(ifStat.getElseOP()!=null) {
                ifStat.getElseOP().accept(this);
            }
        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object visit(ElseOP elseOP) throws RuntimeException {
        try{
            writer.write("else {\n");

            enterScope(elseOP.getSymbolTableElseOp());
            elseOP.getBody().accept(this);
            exitScope();

            writer.append("}\n");


        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object visit(ElseIfOP elseIfOP) throws RuntimeException {
        String condizione = (String) elseIfOP.getExpr().accept(this);
        try{
            writer.write("else if (" + condizione + ") {\n");
            enterScope(elseIfOP.getSymbolTableElseIF());
            elseIfOP.getBody().accept(this);
            exitScope();
            writer.write("}\n");
        } catch(Exception e){
            e.printStackTrace();
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
        var varFieldType = (VarFieldType) result.getFieldType();

        var tipo = varFieldType.getType();

        if (id.getMode().equals(ExprOP.Mode.VARIABLENAME) || id.getMode().equals(ExprOP.Mode.PARAMS) || id.getMode().equals(ExprOP.Mode.NONE)) {
            return lessema;
        } else if (id.getMode().equals(ExprOP.Mode.PARAMSOUT) && !tipo.equalsIgnoreCase("string")) { //se è una stringa non devi fare sta roba
            return "*" + lessema;
        } else if (id.getMode().equals(ExprOP.Mode.PARAMSREF)&& !tipo.equalsIgnoreCase("string") ) { //se è una stringa non devi fare sta roba
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


    int funCallCount = 0;
    @Override
    public Object visit(FunCall funCall) throws RuntimeException {
        funCallCount++;
        int funCallCorrente = funCallCount;
        StringBuilder stringBuilder = new StringBuilder();
        //Lookup Della Funzione
        Function function = (Function) this.currentScope.lookup(funCall.getIdentifier().getLessema()).orElseThrow().getNodo();
        //Mi prendo i tipi di ritorno della funzione
        ArrayList<Type> tipiDiRitorno = function.getReturnTypes();

        ArrayList<ExprOP> listaExprOp = funCall.getExprs();
        Iterator<ExprOP> itListaExprOp = listaExprOp.iterator();

        //caso senza nessun parametro con funzione all'interno tipo foo(a , 12, "pippo");
        if(!funCall.getExprs().stream().anyMatch(exprOP -> exprOP instanceof FunCall)) {
            //stringBuilder.append("result_" + funCall.getIdentifier().getLessema() + " r_" + funCallCount + " = ");
            if(function.getReturnTypes().size() > 1 ){
                stringBuilder.append("result_" + funCall.getIdentifier().getLessema() + " r_" + funCallCount + " = "+ funCall.getIdentifier().getLessema() + "(");
            } else {
                stringBuilder.append(CodeGeneratorUtils.convertType(function.getReturnTypes().get(0).getTipo()) + " r_" + funCallCount + " = "+ funCall.getIdentifier().getLessema() + "(");
            }

            stringBuilder.append(funCall.getIdentifier().getLessema() + "(");
            int i = 0;
            for(ExprOP exprOP : funCall.getExprs()) {

                stringBuilder.append(exprOP.accept(this));

                i++;
                if(funCall.getExprs().size() != i) {
                    stringBuilder.append(",");
                }
            }
            stringBuilder.append(");\n");
        } else {
            StringBuilder chiamataAFunzione = new StringBuilder();
            if(function.getReturnTypes().size() > 1 ){
                chiamataAFunzione.append("result_" + funCall.getIdentifier().getLessema() + " r_" + funCallCount + " = "+ funCall.getIdentifier().getLessema() + "(");
            } else {
                chiamataAFunzione.append(CodeGeneratorUtils.convertType(function.getReturnTypes().get(0).getTipo()) + " r_" + funCallCount + " = "+ funCall.getIdentifier().getLessema() + "(");
            }

            //bisogna prepararsi i parametri da passare alla funzione nel caso di chiamata di funzione
            int j = 0;
            for(ExprOP exprOP : listaExprOp) {
                //caso di espressione non funCall
                if(!(exprOP instanceof FunCall)) {
                    chiamataAFunzione.append(exprOP.accept(this) + ",");

                    j++;
                    if((listaExprOp.size()-1) == j) {
                       // chiamataAFunzione.deleteCharAt(chiamataAFunzione.length()-1);
                    }


                } else {
                    stringBuilder.append(exprOP.accept(this));
                    FunCall f = (FunCall) exprOP;
                    SymbolTableRecord funzioneChiamataCorrente = this.currentScope.lookup(f.getIdentifier().getLessema()).orElseThrow();
                    ArrayList<String> tipiDiRitornoFunCallCorrente = new ArrayList<>(Arrays.asList(funzioneChiamataCorrente.getProperties().split(";")));

                    int i = 0;
                    while( i < tipiDiRitornoFunCallCorrente.size()) {
                        chiamataAFunzione.append("r_" + (funCallCorrente+1) + ".result" + i+ ",");
                        i++;


                    }
                    funCallCorrente++;

                }
            }
            chiamataAFunzione.append(");\n");
            stringBuilder.append(chiamataAFunzione);
        }


        return stringBuilder.toString();

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
