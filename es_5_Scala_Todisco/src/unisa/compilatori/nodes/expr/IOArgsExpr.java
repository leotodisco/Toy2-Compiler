package unisa.compilatori.nodes.expr;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

public class IOArgsExpr extends ExprOP implements Visitable {
    private IOArgsExpr e1, e2;
    private String str;

    public IOArgsExpr(IOArgsExpr e1, IOArgsExpr e2) {
        this.e1 = e1;
        this.e2 = e2;

        super.add(e1);
        super.add(e2);
    }

    public IOArgsExpr(ConstOP strConst) {
        if(!strConst.getType().toString().equals("STRING_CONST")) {
            throw new IllegalArgumentException("This is not a String Constant");
        }

        this.str = strConst.getLessema();
        super.add(strConst);
        this.e1 = null;
        this.e2 = null;
    }

    public IOArgsExpr getE1() {
        return e1;
    }

    public void setE1(IOArgsExpr e1) {
        this.e1 = e1;
    }

    public IOArgsExpr getE2() {
        return e2;
    }

    public void setE2(IOArgsExpr e2) {
        this.e2 = e2;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return "IOArgsExpr{" +
                "e1=" + e1 +
                ", e2=" + e2 +
                ", str='" + str + '\'' +
                '}';
    }
}
