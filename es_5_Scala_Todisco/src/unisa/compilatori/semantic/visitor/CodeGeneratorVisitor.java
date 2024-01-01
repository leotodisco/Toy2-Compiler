package unisa.compilatori.semantic.visitor;

import unisa.compilatori.nodes.*;
import unisa.compilatori.nodes.expr.*;
import unisa.compilatori.nodes.stat.*;
import unisa.compilatori.semantic.symboltable.SymbolTable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Files;

public class CodeGeneratorVisitor implements Visitor{
    private SymbolTable currentScope;
    private static File outFile;
    private FileWriter writer;
    public static String FILE_NAME = "output.c";

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
        } catch (Exception e){
            throw new RuntimeException("Errore nella scrittura del file");
        }

        return null;
    }

    @Override
    public Object visit(IterWithoutProcedure iterWithoutProcedure) throws RuntimeException {
        //aggiungere firme delle funzioni e delle procedure ad inizio file
        return null;
    }

    @Override
    public Object visit(IterOp iterOP) throws RuntimeException {
        return null;
    }


    @Override
    public Object visit(Procedure procedure) throws RuntimeException {
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
        return null;
    }

    @Override
    public Object visit(Identifier id) throws RuntimeException {
        return null;
    }



    @Override
    public Object visit(ConstOP constOP) throws RuntimeException {
        return null;
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
