package unisa.compilatori;


import unisa.compilatori.nodes.ProgramOp;
import unisa.compilatori.semantic.visitor.ScopeCheckingVisitor;
import unisa.compilatori.semantic.visitor.TypeCheckingVisitor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.FileReader;


public class Prova {

    public static void main(String[] args){
        JTree tree;
        String filePath = args[0];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            Lexer prova = new Lexer(br);
            parser p = new parser(prova);

            DefaultMutableTreeNode root = (DefaultMutableTreeNode) p.parse().value;
            //tree=new JTree(root);
            ((ProgramOp) root).accept(new ScopeCheckingVisitor());
            ((ProgramOp) root).accept(new TypeCheckingVisitor());
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
        }


    }
}
