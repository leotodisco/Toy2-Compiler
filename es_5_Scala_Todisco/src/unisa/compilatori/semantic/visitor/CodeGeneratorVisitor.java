package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import unisa.compilatori.semantic.symboltable.SymbolTableRecord;
import unisa.compilatori.semantic.symboltable.VarFieldType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class CodeGeneratorVisitor implements Visitor {
    private SymbolTable currentScope;
    private static File outFile;
    private FileWriter writer;
    public static String FILE_NAME = "output.c";

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
            program.getProc().accept(this);
            program.getIterOp().accept(this);

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
        return null;
    }

    @Override
    public Object visit(IterOp iterOP) throws RuntimeException {
        iterOP.getDeclarations().forEach(varDecl -> varDecl.accept(this));
        return null;
    }


    @Override
    public Object visit(Procedure procedure) throws RuntimeException {
        String idProcedura = procedure.getId().getLessema();
        ArrayList<CallableParam> parametri = procedure.getProcParamDeclList();




        return null;
    }

    @Override
    public Object visit(BinaryOP operazioneBinaria) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(UnaryOP operazioneUnaria) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(VarDecl dichiarazione) throws RuntimeException {
        dichiarazione.getDecls().forEach(decl -> decl.accept(this));

        return null;
    }

    @Override
    public Object visit(Function funzione) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(Stat statement) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(IfStat ifStat) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(ElseOP elseOP) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(ElseIfOP elseIfOP) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(ProcCall procCall) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(WhileStat whileStat) throws RuntimeException {
        enterScope(whileStat.getTable()); //entro nello scope del while

        String condizione = (String) whileStat.getExpr().accept(this); //ottieni la condizione

        try {
            writer.write("while (" + condizione + ") { \n"); // while (true) {
            whileStat.getBody().accept(this); //traduci il body
            writer.write("}");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            exitScope();
        }

        return null;
    }

    @Override
    public Object visit(Body body) throws RuntimeException {
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
                //posso assumere che il numero di costanti sia uguale al numero di id in questa fase
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

    @Override
    public Object visit(FunCall funCall) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(ExprOP exprOP) throws RuntimeException {
        return null;
    }

    @Override
    public Object visit(CallableParam callableParam) throws RuntimeException {
        return null;
    }
}
