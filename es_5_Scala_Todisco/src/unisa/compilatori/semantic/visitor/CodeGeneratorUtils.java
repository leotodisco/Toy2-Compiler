package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.ExprOP;

import java.io.IOException;
import java.io.Writer;
import java.io.File;
import java.util.ArrayList;

public class CodeGeneratorUtils {
    /**
     * Usiamo questo metodo per includere le librerie base di C.
     * @param writer
     */
    public static void addBaseLibraries(Writer writer) {
        try {
            writer.write("#include <stdio.h>\n");
            writer.write("#include <stdlib.h>\n");
            writer.write("#include <string.h>\n");
            writer.write("#include <math.h>\n");
            writer.write("#include <unistd.h>\n");
            writer.write("#include <stdbool.h>\n");
            writer.write("#define MAXCHAR 512\n");
            writer.write("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo che aggiunge al nostro file C le funzioni di utilità
     * @param writer
     */
    public static void addHelperFunctions(Writer writer) {
        try {
            writer.write("char* integer_to_str(int i){\n");
            writer.write("\tint length= snprintf(NULL,0,\"%d\",i);\n");
            writer.write("\tchar* result=malloc(length+1);\n");
            writer.write("\tsnprintf(result,length+1,\"%d\",i);\n");
            writer.write("\treturn result;\n");
            writer.write("}\n");

            writer.write("char* real_to_str(float i){\n");
            writer.write("\tint length= snprintf(NULL,0,\"%f\",i);\n");
            writer.write("\tchar* result=malloc(length+1);\n");
            writer.write("\tsnprintf(result,length+1,\"%f\",i);\n");
            writer.write("\treturn result;\n");
            writer.write("}\n");

            writer.write("char* char_to_str(char i){\n");
            writer.write("\tint length= snprintf(NULL,0,\"%c\",i);\n");
            writer.write("\tchar* result=malloc(length+1);\n");
            writer.write("\tsnprintf(result,length+1,\"%c\",i);\n");
            writer.write("\treturn result;\n");
            writer.write("}\n");

            writer.write("char* bool_to_str(bool i){\n");
            writer.write("\tint length= snprintf(NULL,0,\"%d\",i);\n");
            writer.write("\tchar* result=malloc(length+1);\n");
            writer.write("\tsnprintf(result,length+1,\"%d\",i);\n");
            writer.write("\treturn result;\n");
            writer.write("}\n");

            writer.write("char* str_concat(char* str1, char* str2){\n");
            writer.write("\tchar* result=malloc(sizeof(char)*MAXCHAR);\n");
            writer.write("\tresult=strcat(result,str1);\n");
            writer.write("\tresult=strcat(result,str2);\n");
            writer.write("\treturn result;\n}\n");

            writer.write("\n");
            writer.write("char* read_str(){\n");
            writer.write("\tchar* str=malloc(sizeof(char)*MAXCHAR);\n");
            writer.write("\tscanf(\"%s\",str);\n");
            writer.write("\treturn str;\n}\n");

            writer.write("\n");
            writer.write("int str_to_bool(char* expr){\n");
            writer.write("\tint i=0;\n");
            writer.write("\tif ( (strcmp(expr, \"true\")==0) || (strcmp(expr, \"1\"))==0 )\n");
            writer.write("\t\ti=1;\n");
            writer.write("\tif ( (strcmp(expr, \"false\")==0) || (strcmp(expr, \"0\"))==0 )\n");
            writer.write("\t\ti=0;\n");
            writer.write("\treturn i;\n}\n");

            writer.write("\n");
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Converte i nomi dei tipi in nomi compatibili con C.
     * @param type
     * @return
     */
    public static String convertType(String type){
        if(type.equalsIgnoreCase("real")) type="double";
        if(type.equalsIgnoreCase("integer")) type="int";
        if(type.equalsIgnoreCase("boolean")) type="bool";
        if(type.equalsIgnoreCase("string")) type="char*";

        return type;
    }

    /**
     * Converte i nomi delle operazioni binarie nel corrispondente token compatibile in C.
     * @param nomeOperazione
     * @return
     */
    public static String convertOperations(String nomeOperazione){
//"plus_op", "times_op", "minus_op"
        String tipoOprazione="";
        if(nomeOperazione.equalsIgnoreCase("plus_op")) //fare che se le due exprs sono stringhe allora è strcat
            tipoOprazione="+";
        if(nomeOperazione.equalsIgnoreCase("minus_op"))
            tipoOprazione="-";
        if(nomeOperazione.equalsIgnoreCase("times_op"))
            tipoOprazione="*";
        if(nomeOperazione.equalsIgnoreCase("div_op"))
            tipoOprazione="/";
        if(nomeOperazione.equalsIgnoreCase("gt_op"))
            tipoOprazione=">";
        if(nomeOperazione.equalsIgnoreCase("ge_op"))
            tipoOprazione=">=";
        if(nomeOperazione.equalsIgnoreCase("lt_op"))
            tipoOprazione="<";
        if(nomeOperazione.equalsIgnoreCase("le_op"))
            tipoOprazione="<=";
        if(nomeOperazione.equalsIgnoreCase("ne_op"))
            tipoOprazione="!=";
        if(nomeOperazione.equalsIgnoreCase("eq_op"))
            tipoOprazione="==";
        if(nomeOperazione.equalsIgnoreCase("and_op"))
            tipoOprazione="&&";
        if(nomeOperazione.equalsIgnoreCase("or_op"))
            tipoOprazione="||";
        if(nomeOperazione.equalsIgnoreCase("uminus"))
            tipoOprazione="-1*";
        if(nomeOperazione.equalsIgnoreCase("not"))
            tipoOprazione="!";
        if(nomeOperazione.equalsIgnoreCase("stringConcat"))
            tipoOprazione="strcat";

        return tipoOprazione;
    }

    /**
     * Aggiunge al file le firme di tutte le procedure/funzioni di Toy2.
     * @param iter
     * @param iterWithoutProcedure
     * @param procedure
     * @param writer
     */
    public static void addFunctionSignatures(Writer writer, IterOp iter, IterWithoutProcedure iterWithoutProcedure, Procedure procedure) {

        addFunctionsSignaturesHelper(writer, iter.getFunctions());
        addFunctionsSignaturesHelper(writer, iterWithoutProcedure.getFunctions());

        ArrayList<Procedure> procedures = new ArrayList<>(iter.getProcedures());
        procedures.add(procedure);
        addProcedureSignatureHelper(writer, procedures);


    }

    private static void addFunctionsSignaturesHelper(Writer writer, ArrayList<Function> functions) {

        //(struct) nomeFunzione(tipo id, tipo id, tipo* id)
        for(Function function: functions) {
            StringBuilder signature = new StringBuilder();

            signature.append("typedef struct { \n");
            int i = 0;
            for(Type returnType : function.getReturnTypes()) {
                signature.append("\t" + convertType(returnType.getTipo()));
                signature.append(" result" + i +";\n");
                i++;
            }
            signature.append("}" +" result_" + function.getId().getLessema() + ";\n\n");

            signature.append("result_" + function.getId().getLessema() + " ");

            signature.append(function.getId().getLessema());
            signature.append("(");


            //aggiungo parametri della funzione
            for(CallableParam param : function.getParametersList()) {
                //aggiungo il tipo
                signature.append(convertType(param.getTipo().getTipo()));
                signature.append(" ");
                //aggiungo l'id del parametro
                signature.append(param.getId().getLessema());
                signature.append(",");
            }
            //elimino l'ultimo "," aggiunto alla fine dei parametri
            signature.deleteCharAt(signature.length()-1);

            signature.append(");\n\n");
            try {
                writer.append(signature.toString());
            } catch (Exception e) {
                throw new RuntimeException("Errore nella scrittura delle signature delle funzioni");
            }

        }
    }

    //void nomeProcedura(tipo id, tipo id, tipo* id)
    public static void addProcedureSignatureHelper(Writer writer, ArrayList<Procedure> procedureList) {
        for(Procedure proc : procedureList) {
            if (proc.getId().getLessema().equals("main")){
                continue;
            }
            scriviSingolaProceduraSignature(writer, proc);
            try {
                writer.append(";\n\n"); //lo faccio qui per riutilizzare questo metodo
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    /**
     * Scrive una singola signature di procedura
     * @param writer
     * @param proc
     */
    public static void scriviSingolaProceduraSignature(Writer writer, Procedure proc) {
        var nomeProcedura = proc.getId().getLessema();

        var listaParametriProcedura = proc.getProcParamDeclList();

        try{
        //scrivi void(
        //scrivi ogni singolo parametro
            // scrivi )
            writer.write("void ");
            writer.append(nomeProcedura);
            writer.append("(");

            //Scrivi la lista di parametri
            for(int i = 0; i < listaParametriProcedura.size(); i++){
                var parametro = listaParametriProcedura.get(i);
                var id = parametro.getId().getLessema();
                var tipo = parametro.getTipo().getTipo();
                var isPuntatore = parametro.getId().getMode().equals(ExprOP.Mode.PARAMSOUT);
                var tipoConvertito = convertType(tipo);

                writer.append(tipoConvertito);
                if(isPuntatore) {
                    writer.append("*");
                }

                writer.append(" ");
                writer.append(id);

                //se non è l'ultimo elemento metti la virgola
                if(i != listaParametriProcedura.size()-1)
                    writer.append(",");
                else
                    break;
            }
            writer.append(")");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
