package unisa.compilatori.nodes.expr;

import unisa.compilatori.semantic.visitor.Visitable;
import unisa.compilatori.semantic.visitor.Visitor;

public class IOArgsExpr extends ExprOP implements Visitable {
    private ExprOP e1, e2;
    private String str;

    public IOArgsExpr(ExprOP e1, ExprOP e2) {
        super(Mode.IOARGS);
        this.e1 = e1;
        this.e2 = e2;

        super.add(e1);
        super.add(e2);
    }

    public ExprOP getE1() {
        return e1;
    }

    public void setE1(ExprOP e1) {
        this.e1 = e1;
    }

    public ExprOP getE2() {
        return e2;
    }

    public void setE2(ExprOP e2) {
        this.e2 = e2;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public IOArgsExpr(ConstOP strConst) {
        super(Mode.IOARGS);
        if(!strConst.getType().toString().equals("STRING_CONST")) {
            throw new IllegalArgumentException("This is not a String Constant");
        }

        this.str = strConst.getLessema();
        super.add(strConst);
        this.e1 = null;
        this.e2 = null;
    }


    @Override
    public String toString() {
        return "IOArgsExpr{" +
                "e1=" + e1 +
                ", e2=" + e2 +
                ", str='" + str + '\'' +
                '}';
    }
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}