package unisa.compilatori;


import unisa.compilatori.nodes.ProgramOp;
import unisa.compilatori.semantic.visitor.CodeGeneratorVisitor;
import unisa.compilatori.semantic.visitor.ScopeCheckingVisitor;
import unisa.compilatori.semantic.visitor.TypeCheckingVisitor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.FileReader;


public class Main {

    public static void main(String[] args){
        JTree tree;
        String filePath = args[0];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            Lexer prova = new Lexer(br);
            parser p = new parser(prova);

            //get filename from file path
            String[] slashSplit = args[0].split("/");
            String fullName = slashSplit[slashSplit.length-1];
            String fileName = fullName.split(".txt")[0];
            CodeGeneratorVisitor.FILE_NAME = fileName + ".c";

            DefaultMutableTreeNode root = (DefaultMutableTreeNode) p.parse().value;
            tree=new JTree(root);

            ((ProgramOp) root).accept(new ScopeCheckingVisitor());
            ((ProgramOp) root).accept(new TypeCheckingVisitor());


            ((ProgramOp) root).accept(new CodeGeneratorVisitor());
            int a = 0;
            //JFrame framePannello=new JFrame();
            //framePannello.setSize(400, 400);
            //JScrollPane treeView = new JScrollPane(tree);
            //framePannello.add(treeView);
            //framePannello.setVisible(true);

            /*while (!prova.yyatEOF()){
                p.debug_parse();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            //System.err.println("\n\n--------------ERRORE--------------\nErrore durante la compilazione: " + CodeGeneratorVisitor.FILE_NAME +"\n" + e.getMessage() + "\n----------------------------------\n\n");
        }


    }
}
