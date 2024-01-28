package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.symboltable.SymbolTableRecord;
import unisa.compilatori.semantic.symboltable.VarFieldType;


import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.FileWriter;

import java.nio.file.*;
import java.nio.file.Files;
import java.util.*;

public class CodeGeneratorVisitor implements Visitor {
    private SymbolTable currentScope;
    private static File outFile;
    private FileWriter writer;
    public static String FILE_NAME = "output.c";

    ArrayList <String> idParamsOut = new ArrayList<>();
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
            CodeGeneratorUtils.addFunctionSignatures(this.writer, program.getIterOp());

            program.getIterOp().accept(this);

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        idParamsOut = new ArrayList<>();
        parametri.stream()
                .filter(param -> param.getId().getMode().equals(ExprOP.Mode.PARAMSOUT))
                .map(param -> param.getId().getLessema())
                .forEach(param -> idParamsOut.add(param));
        
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

        idParamsOut = new ArrayList<>();
        return null;
    }

    @Override
    public Object visit(BinaryOP operazioneBinaria) throws RuntimeException {
        String expr1 = (String) operazioneBinaria.getExpr1().accept(this);
        if(operazioneBinaria.getExpr1() instanceof Identifier && idParamsOut.contains(((Identifier) operazioneBinaria.getExpr1()).getLessema())) {
            SymbolTableRecord record = this.currentScope.lookup(((Identifier) operazioneBinaria.getExpr1()).getLessema()).orElseThrow();
            VarFieldType varFieldType = (VarFieldType) record.getFieldType();
            if(varFieldType.getType().equalsIgnoreCase("string")) {
                expr1 = expr1.replace("*", "");
            }

        }

        String expr2 = (String) operazioneBinaria.getExpr2().accept(this);
        if(operazioneBinaria.getExpr2() instanceof Identifier && idParamsOut.contains(((Identifier) operazioneBinaria.getExpr2()).getLessema())) {
            SymbolTableRecord record = this.currentScope.lookup(((Identifier) operazioneBinaria.getExpr2()).getLessema()).orElseThrow();
            VarFieldType varFieldType = (VarFieldType) record.getFieldType();
            if(varFieldType.getType().equalsIgnoreCase("string")) {
                expr2 = expr2.replace("*", "");
            }
        }

        //serve a capure se l'operazione è tra due stringhe
        boolean isString = operazioneBinaria.getExpr1().getTipo().equalsIgnoreCase("string") || operazioneBinaria.getExpr2().getTipo().equalsIgnoreCase("string");
        String lessemaOperazione = CodeGeneratorUtils.convertOperations(operazioneBinaria.getName(), isString ); //ottieni il lessema giusto per l'operazione

        if(lessemaOperazione.equalsIgnoreCase("==") || lessemaOperazione.equalsIgnoreCase("!=")) {
            if(operazioneBinaria.getExpr1().getTipo().equalsIgnoreCase("string")
                    && operazioneBinaria.getExpr2().getTipo().equalsIgnoreCase("string")) {
                return "strcmp(" + expr1 + ", "  + expr2 + ")" + lessemaOperazione +"0";
            }
        }



        if (lessemaOperazione.equalsIgnoreCase("strcat")) {
            //todo fare controlli per vedere di che tipo sono le due espressioni
            //a quel punto puoi richiamare la funzione helper che converte quel tipo specifico

            if (operazioneBinaria.getExpr1() instanceof Identifier) {
                var id = ((Identifier) operazioneBinaria.getExpr1()).getLessema();
                var record = this.currentScope.lookup(id).get();
                if (record.getFieldType() instanceof VarFieldType) {
                    VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                    if (varFieldType.getType().equalsIgnoreCase("integer")) {
                        return "str_concat(" + "integer_to_str(" + expr1 + ")" + ", " + expr2.replace("*","") + ")";
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

        //isString qui è falso perchè non esistono operazioni unarie con stringhe
        String lessemaOperazione = CodeGeneratorUtils.convertOperations(operazioneUnaria.getSimbolo(), false);

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
            if( funzione.getReturnTypes().size() == 1) {
                signature.append(CodeGeneratorUtils.convertType(funzione.getReturnTypes().get(0).getTipo()));
                signature.append(" ");
            } else {
                signature.append("result_" + idFunzione);
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
                            if(tipiDiRitornoFunzione.size() > 1) {
                                writer.write(lessemaId + " = r_" + temp+ ".result" + i +";\n");
                            } else {
                                writer.write(lessemaId + " = r_" + temp+ ";\n");
                            }

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
                    try{
                        if(espressione instanceof Identifier) {
                            SymbolTableRecord record = this.currentScope.lookup(((Identifier) espressione).getLessema()).orElseThrow();
                            VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                            if(varFieldType.getType().equalsIgnoreCase("String")) {
                                writer.write(lessemaId + " = " + " malloc(sizeof(char) * MAXCHAR);\n");
                                writer.write("strncpy(" + lessemaId + "," +  (String) lessemaOperazione+", MAXCHAR);\n");
                                continue;
                            }
                        }else if(espressione.getTipo().equalsIgnoreCase("String")) {
                            writer.write(lessemaId + " = " + " malloc(sizeof(char) * MAXCHAR)\n;");
                            writer.write("strncpy(" + lessemaId + "," +  (String) lessemaOperazione+", MAXCHAR);\n");
                            continue;
                        }
                        writer.write(lessemaId + " = " + (String) lessemaOperazione+";\n");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }
        //TODO implementare statement return, write, write-return
        if (statement.getTipo().equals(Stat.Mode.RETURN)) {
            ArrayList<ExprOP> listaEspressioni = statement.getEspressioniList();

            //CASO IN CUI NEL RETURN CI SIA SOLTANTO UN ESPRESSIONE, QUINDI SOLO UNA FUNZIONE, UNA COSTANTE O UNA VARIABILE
            if(listaEspressioni.size() == 1) {
                if(listaEspressioni.get(0) instanceof FunCall) {
                    //lookup per scoprire i tipi di ritorno
                    SymbolTableRecord function = this.currentScope.lookup(((FunCall) listaEspressioni.get(0)).getIdentifier().getLessema()).orElseThrow();
                    ArrayList<String> tipiDiRitorno = new ArrayList<>(Arrays.asList(function.getProperties().split(";")));

                    if(tipiDiRitorno.size() == 1) {
                        try {
                            int temp = funCallCount;
                            writer.write((String) listaEspressioni.get(0).accept(this));
                            writer.write("return r_" + (temp+1) +";");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return "";
                    }
                } else {
                    try {
                        writer.write("return ");
                        writer.write(listaEspressioni.get(0).accept(this) + ";");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "";
                }

            }

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
            TreeNode nodo = statement.getParent();
            while(!(nodo instanceof Function)){
                nodo = nodo.getParent();
            }

            Function funzioneDiAssign = (Function) nodo;
            try {
                writer.write("result_" + funzioneDiAssign.getId().getLessema() + " daRestituire;\n");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //k è il counter che viene usato per incrementare la variabili result
            //in daRestituire tipo daResituire.resultK
            //viene incrementato ogni volta che si mette un valore nel .result di daRestituire
            int k = 0;

            for(int i = 0; i < statement.getEspressioniList().size(); i++) {
                ExprOP exprOP = statement.getEspressioniList().get(i);

                if(exprOP instanceof FunCall) {

                    //lookup di funcal per scoprire quanti tipi restituisce
                    SymbolTableRecord funzioneChiamataCorrente = this.currentScope.lookup(((FunCall) exprOP).getIdentifier().getLessema()).orElseThrow();
                    ArrayList<String> tipiDiRitornoFunCallCorrente = new ArrayList<>(Arrays.asList(funzioneChiamataCorrente.getProperties().split(";")));

                    int idStruct = itIdStructsFunzioni.next();

                    if(tipiDiRitornoFunCallCorrente.size() == 1) {
                        daRestituire.append("daRestituire." + "result" + k + "=" + "r_" + idStruct +";\n");
                        k++;
                        continue;
                    }

                    for(int j = 0; j < tipiDiRitornoFunCallCorrente.size(); j++) {
                        daRestituire.append("daRestituire." + "result" + k + "=" + "r_" + idStruct + ".result"+ j + ";\n");
                        k++;
                    }

                } else {
                    daRestituire.append("daRestituire." + "result" + k + "=" + exprOP.accept(this)+ ";\n");
                    k++;
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
                        if(this.idParamsOut.contains(id.getLessema())){
                            SymbolTableRecord record = this.currentScope.lookup(id.getLessema()).get();
                            VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                            if (varFieldType.getType().equalsIgnoreCase("real")) {
                                writer.write("scanf( \"%f\", " + id.getLessema() + ");\n");
                            }
                            if (varFieldType.getType().equalsIgnoreCase("integer")) {
                                writer.write("scanf( \"%d\", " + id.getLessema() + ");\n");
                            }
                            if (varFieldType.getType().equalsIgnoreCase("string")) {
                                writer.write("scanf( \"%s\", " + id.getLessema() + ");\n");
                            }
                        }
                        else {
                            SymbolTableRecord record = this.currentScope.lookup(id.getLessema()).get();
                            VarFieldType varFieldType = (VarFieldType) record.getFieldType();
                            if (varFieldType.getType().equalsIgnoreCase("real")) {
                                writer.write("scanf( \"%f\", &" + id.getLessema() + ");\n");
                            }
                            if (varFieldType.getType().equalsIgnoreCase("integer")) {
                                writer.write("scanf( \"%d\", &" + id.getLessema() + ");\n");
                            }
                            if (varFieldType.getType().equalsIgnoreCase("string")) {
                                writer.write("scanf( \"%s\", " + id.getLessema() + ");\n");
                            }
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
            ArrayList<ExprOP> listaEspressioni = statement.getEspressioniList();
            Collections.reverse(listaEspressioni);

            StringBuilder parteDopoLeVirgolette = new StringBuilder();
            try{
                StringBuilder stringBuilder = new StringBuilder("printf(\" ");
                for(int i = 0; i < listaEspressioni.size(); i++) {
                    ExprOP espressione = listaEspressioni.get(i);
                    String tipoEspressione = "";
                    if(espressione instanceof FunCall) {
                        FunCall espressioneCastata = (FunCall) espressione;
                        var record = this.currentScope.lookup(espressioneCastata.getIdentifier().getLessema()).get();
                        String tipo = record.getProperties();

                        String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipo.substring(0, tipo.length()-1));

                        stringBuilder.append(formatSpecifier + " ");
                        parteDopoLeVirgolette.append(espressione.accept(this) + ", ");

                        continue;
                    }
                    else if(espressione instanceof ConstOP) {
                        ConstOP constOP = (ConstOP) espressione;
                        if (constOP.getType().equals(ConstOP.Kind.STRING)){
                            String costante = (String) constOP.accept(this);
                            stringBuilder.append(costante.replace("\"", ""));
                            stringBuilder.append(" ");


                        } else {
                            stringBuilder.append((String) constOP.accept(this));
                        }

                        continue;
                    }
                    else if(espressione instanceof Identifier) {
                        Identifier id = (Identifier) espressione;
                        //String tipo = (String) id.accept(new TypeCheckingVisitor());
                        var record = this.currentScope.lookup(id.getLessema()).get();
                        var fieldType = (VarFieldType) record.getFieldType();
                        String tipo = fieldType.getType();

                        String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipo);
                        stringBuilder.append(formatSpecifier + " ");
                        parteDopoLeVirgolette.append(id.accept(this) + ", ");


                        continue;
                    }
                    else if(espressione instanceof BinaryOP) {
                        BinaryOP operazioneBinaria = (BinaryOP) espressione;


                        String tipo = operazioneBinaria.getTipo();


                        String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipo);
                        stringBuilder.append(formatSpecifier + " ");
                        parteDopoLeVirgolette.append(espressione.accept(this) + ", ");


                        continue;
                    }
                    else if(espressione instanceof UnaryOP) {
                        UnaryOP operazioneUnaria = (UnaryOP) espressione;


                        String tipo = operazioneUnaria.getReturnType();


                        String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipo);
                        stringBuilder.append(formatSpecifier + " ");
                        parteDopoLeVirgolette.append(espressione.accept(this) + ", ");


                        continue;
                    }
                    else {
                        tipoEspressione = (String) espressione.accept(new TypeCheckingVisitor());
                    }

                    String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipoEspressione);
                    stringBuilder.append(formatSpecifier + " ");

                }
                stringBuilder.append("\", " + parteDopoLeVirgolette);

                stringBuilder.deleteCharAt(stringBuilder.length()-2); //rimuovi l'ultima virgola
                stringBuilder.append(");\n");
                writer.append(stringBuilder.toString());


            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if (statement.getTipo().equals(Stat.Mode.WRITE_RETURN)) {
            ArrayList<ExprOP> listaEspressioni = statement.getEspressioniList();
            Collections.reverse(listaEspressioni);

            StringBuilder parteDopoLeVirgolette = new StringBuilder();
            try{
                StringBuilder stringBuilder = new StringBuilder("printf(\" ");
                for(int i = 0; i < listaEspressioni.size(); i++) {
                    ExprOP espressione = listaEspressioni.get(i);
                    String tipoEspressione = "";
                    if(espressione instanceof FunCall) {
                        FunCall espressioneCastata = (FunCall) espressione;
                        var record = this.currentScope.lookup(espressioneCastata.getIdentifier().getLessema()).get();
                        String tipo = record.getProperties();

                        String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipo.substring(0, tipo.length()-1));

                        stringBuilder.append(formatSpecifier + " ");
                        parteDopoLeVirgolette.append(espressione.accept(this) + ", ");

                        continue;
                    }
                    else if(espressione instanceof ConstOP) {
                        ConstOP constOP = (ConstOP) espressione;

                        if (constOP.getType().equals(ConstOP.Kind.STRING)){
                            String costante = (String) constOP.accept(this);
                            stringBuilder.append(costante.replace("\"", ""));
                            stringBuilder.append(" ");

                        } else {
                            stringBuilder.append((String) constOP.accept(this));
                        }

                        continue;
                    }
                    else if(espressione instanceof Identifier) {
                        Identifier id = (Identifier) espressione;
                        //String tipo = (String) id.accept(new TypeCheckingVisitor());
                        var record = this.currentScope.lookup(id.getLessema()).get();
                        var fieldType = (VarFieldType) record.getFieldType();
                        String tipo = fieldType.getType();

                        String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipo);
                        stringBuilder.append(formatSpecifier + " ");
                        parteDopoLeVirgolette.append(id.accept(this) + ", ");


                        continue;
                    }
                    else if(espressione instanceof BinaryOP) {
                        BinaryOP operazioneBinaria = (BinaryOP) espressione;


                        String tipo = operazioneBinaria.getTipo();


                        String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipo);
                        stringBuilder.append(formatSpecifier + " ");
                        parteDopoLeVirgolette.append(espressione.accept(this) + ", ");


                        continue;
                    }
                    else if(espressione instanceof UnaryOP) {
                        UnaryOP operazioneUnaria = (UnaryOP) espressione;


                        String tipo = operazioneUnaria.getReturnType();


                        String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipo);
                        stringBuilder.append(formatSpecifier + " ");
                        parteDopoLeVirgolette.append(espressione.accept(this) + ", ");


                        continue;
                    }



                    String formatSpecifier = CodeGeneratorUtils.getFormatSpecifier(tipoEspressione);
                    stringBuilder.append(formatSpecifier + " ");

                }
                stringBuilder.append("\\n\", " + parteDopoLeVirgolette);

                stringBuilder.deleteCharAt(stringBuilder.length()-2); //rimuovi l'ultima virgola
                stringBuilder.append(");\n");
                writer.append(stringBuilder.toString());


            } catch(Exception e){
                e.printStackTrace();
            }
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

                //quando chiami una procedura dentro una procedura
                //si utilizza la struttura dati idParamsOut che memorizza i parametri out della procedura chiamante
                String result_paramsref = (String) parametroAttuale.accept(this);

                if (parametroAttuale.getMode().equals(ExprOP.Mode.PARAMSREF) && idParamsOut.contains(((Identifier) parametroAttuale).getLessema())) {
                    result_paramsref = result_paramsref.replace("*", "");
                }

                if( i == parametri.size()-1) {
                    writer.write(result_paramsref );
                } else {
                    writer.write(result_paramsref + " ,");
                }
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
                String tipo = CodeGeneratorUtils.convertType(decl.getTipo().getTipo());

                for (int i = 0; i < decl.getIds().size(); i++) {
                    Identifier idParametro = decl.getIds().get(i);
                    String id = (String) idParametro.accept(this);

                    writer.write(tipo);
                    writer.append(" ");
                    writer.append(id);

                    if(tipo.equalsIgnoreCase("char*")){
                        writer.write(" = ");
                        writer.write("malloc(sizeof(char) * MAXCHAR)");
                    }

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
                    if(costante.getType().toString().equalsIgnoreCase("String")) {
                        writer.write(tipo + " " + lessemaId + " = " + " malloc(sizeof(char) * MAXCHAR);\n");
                        writer.write("strncpy(" + lessemaId + "," + lessemaCostante+", MAXCHAR);\n");
                    } else {
                        writer.write(tipo + " " + lessemaId + " = " + lessemaCostante + ";\n");
                    }

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

        if(result.getNodo() instanceof CallableParam && ((CallableParam) result.getNodo()).getId().getMode().equals(ExprOP.Mode.PARAMSOUT)) {
            return "*" + lessema;
        }

        var tipo = varFieldType.getType();
        /*
        if(idParamsOut.contains(id.getLessema()) && !tipo.equalsIgnoreCase("string")) {
            return "*" + lessema;
        }
        */
        if(id.getMode().equals(ExprOP.Mode.PARAMSOUT)) {
            return "*" + lessema;
        }else if (id.getMode().equals(ExprOP.Mode.VARIABLENAME) || id.getMode().equals(ExprOP.Mode.PARAMS) || id.getMode().equals(ExprOP.Mode.NONE)) {
            return lessema;
        }  else if (id.getMode().equals(ExprOP.Mode.PARAMSREF)/*&& !tipo.equalsIgnoreCase("string") */) { //se è una stringa non devi fare sta roba
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

        StringBuilder stringBuilder = new StringBuilder();
        TreeNode ParentFunCall = funCall.getParent();
        if(ParentFunCall instanceof Stat && (((Stat)ParentFunCall).getTipo().equals(Stat.Mode.ASSIGN) || ((Stat)ParentFunCall).getTipo().equals(Stat.Mode.RETURN))) {
            funCallCount++;
            int funCallCorrente = funCallCount;

            //Lookup Della Funzione
            Function function = (Function) this.currentScope.lookup(funCall.getIdentifier().getLessema()).orElseThrow().getNodo();
            //Mi prendo i tipi di ritorno della funzione
            ArrayList<Type> tipiDiRitorno = function.getReturnTypes();

            ArrayList<ExprOP> listaExprOp = funCall.getExprs();
            Iterator<ExprOP> itListaExprOp = listaExprOp.iterator();

            if (function.getReturnTypes().size() > 1) {
                stringBuilder.append("result_" + funCall.getIdentifier().getLessema() + " r_" + funCallCount + " = " + funCall.getIdentifier().getLessema() + "(");
            } else {
                stringBuilder.append(CodeGeneratorUtils.convertType(function.getReturnTypes().get(0).getTipo()) + " r_" + funCallCount + " = " + funCall.getIdentifier().getLessema() + "(");
            }

            //caso senza nessun parametro con funzione all'interno tipo foo(a , 12, "pippo");
            if (!funCall.getExprs().stream().anyMatch(exprOP -> exprOP instanceof FunCall)) {

                //stringBuilder.append(funCall.getIdentifier().getLessema() + "(");
                int i = 0;
                for (ExprOP exprOP : funCall.getExprs()) {

                    stringBuilder.append(exprOP.accept(this));

                    i++;
                    if (funCall.getExprs().size() != i) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append(");\n");
            } else {
                StringBuilder chiamataAFunzione = new StringBuilder();

                //bisogna prepararsi i parametri da passare alla funzione nel caso di chiamata di funzione
                int j = 0;
                for (ExprOP exprOP : listaExprOp) {
                    //caso di espressione non funCall

                    chiamataAFunzione.append(exprOP.accept(this) + ",");
                    if (exprOP instanceof FunCall) {
                        funCallCorrente++;
                    }

                    j++;
                    if( j > listaExprOp.size()-1) {
                        chiamataAFunzione.deleteCharAt(chiamataAFunzione.length()-1);
                    }
                }
                chiamataAFunzione.append(");\n");
                stringBuilder.append(chiamataAFunzione);
            }
            //System.out.println("Chiamata funzione dentro assign");
        } else { //CASO IN CUI NON è UN ASSIGN, QUINDI QUANDO LA FUNZIONE NON PUò TORNARE TIPI DI RITORNO MULTIPLI
            stringBuilder.append(funCall.getIdentifier().getLessema() + "(");

            int i = 0;
            for( ExprOP exprOP : funCall.getExprs()) {
                stringBuilder.append(exprOP.accept(this) + ",");
                i++;

                if(i > funCall.getExprs().size() -1 ) {
                    stringBuilder.deleteCharAt(stringBuilder.length()-1);
                }
            }

            stringBuilder.append(")");

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
