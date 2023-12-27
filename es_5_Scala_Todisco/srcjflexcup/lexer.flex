/* JFlex example: part of Java language lexer specification */
package unisa.compilatori;
import java_cup.runtime.*;
import java.util.HashMap;
%%
%class Lexer
%unicode
%line
%column
%cup
%type Symbol
%function next_token

%{
private StringBuilder stringBuilder = new StringBuilder();
private Boolean firstCharacter = false;
private int lineUnclosed = 0;
private static HashMap<String, Symbol> stringTable = new HashMap<>();
StringBuffer string = new StringBuffer();
   private Symbol symbol(int type) {
          return new Symbol(type, yyline, yycolumn);
   }

   private Symbol symbol(int type, Object value) {
          return new Symbol(type, yyline, yycolumn, value);
   }
    private Symbol installID(String lessema){
        Symbol token;

        //utilizzo come chiave della hashmap il lessema
        if(stringTable.containsKey(lessema)){
            return stringTable.get(lessema);
        }
        else {
            token =  symbol(sym.ID, "<ID,"+lessema+">");
            stringTable.put(lessema, token);
            return token;
        }
    }
    public void stampaHashMap(){
       this.stringTable.keySet().stream().forEach(key->System.out.println(stringTable.get(key)));
    }
%}

//gestico l'eof
%eofval{
return symbol(sym.EOF, yytext());
%eofval}


LineTerminator = \r|\n|\r\n

IntegerConst = [0-9]+
RealConst = ((\d)+\.(\d)+)
StringConst = \"([^\"\\]|\\.)*\"
UnclosedComment = \%([^\"\\]|\\.)*\%
ClosedComment = \%([^\"\\]|\\.)*\%
InputCharacter = [^\r\n]
WhiteSpace = {LineTerminator} | [ \t\f]
Identifier = [A-Za-z][A-Za-z0-9]*

%state BLK_COMMENT
%state STRING_STATE

%%
/* keywords */


<YYINITIAL> {
//blocca l'inizio dei commenti
"var" { return symbol(sym.VAR, new Token("VAR", yytext())); }
":" { return symbol(sym.COLON, new Token("COLON", yytext())); }
"^=" { return symbol(sym.ASSIGN, new Token("ASSIGN", yytext())); }
 ";" { return symbol(sym.SEMI, new Token("SEMI", yytext())); }
"," { return symbol(sym.COMMA, new Token("COMMA", yytext())); }
"\\" { return symbol(sym.ENDVAR, new Token("ENDVAR", yytext()));}
"^="  { return symbol(sym.ASSIGN, new Token("ASSIGN", yytext())); }
";"    { return symbol(sym.SEMI, new Token("SEMI", yytext())); }
","   { return symbol(sym.COMMA, new Token("COMMA", yytext())); }
"true"    { return symbol(sym.TRUE, new Token("TRUE", yytext())); }
"false"   { return symbol(sym.FALSE, new Token("FALSE", yytext())); }
"real"    { return symbol(sym.REAL, new Token("REAL", yytext())); }
"integer" { return symbol(sym.INTEGER, new Token("INTEGER", yytext())); }
"string"  { return symbol(sym.STRING, new Token("STRING", yytext())); }
"boolean" { return symbol(sym.BOOLEAN, new Token("BOOLEAN", yytext())); }
"return"  { return symbol(sym.RETURN, new Token("RETURN", yytext())); }
"func" { return symbol(sym.FUNCTION, new Token("FUNCTION", yytext())); }
"->" { return symbol(sym.TYPERETURN, new Token("TYPERETURN", yytext())); }
"endfunc" { return symbol(sym.ENDFUNCTION, new Token("ENDFUNCTION", yytext())); }
"("    { return symbol(sym.LPAR, new Token("LPAR", yytext())); }
")"    { return symbol(sym.RPAR, new Token("RPAR", yytext())); }
"proc" { return symbol(sym.PROCEDURE, new Token("PROCEDURE", yytext())); }
"endproc" { return symbol(sym.ENDPROCEDURE, new Token("ENDPROCEDURE", yytext())); }
"out"    { return symbol(sym.OUT, new Token("OUT", yytext())); }
"-->"   { return symbol(sym.WRITE, new Token("WRITE", yytext())); }
"-->!" { return symbol(sym.WRITERETURN, new Token("WRITERETURN", yytext())); }
"$" { return symbol(sym.DOLLARSIGN, new Token("DOLLARSIGN", yytext())); }
"<--"    { return symbol(sym.READ, new Token("READ", yytext())); }
"if"     { return symbol(sym.IF, new Token("IF", yytext())); }
"then"    { return symbol(sym.THEN, new Token("THEN", yytext())); }
"else"    { return symbol(sym.ELSE, new Token("ELSE", yytext())); }
"endif"   { return symbol(sym.ENDIF, new Token("ENDIF", yytext())); }
"elseif"    { return symbol(sym.ELIF, new Token("ELIF", yytext())); }
"while"   { return symbol(sym.WHILE, new Token("WHILE", yytext())); }
"do"      { return symbol(sym.DO, new Token("DO", yytext())); }
"endwhile" { return symbol(sym.ENDWHILE, new Token("ENDWHILE", yytext())); }
"+"    { return symbol(sym.PLUS, new Token("PLUS", yytext())); }
"-"   { return symbol(sym.MINUS, new Token("MINUS", yytext())); }
"*"   { return symbol(sym.TIMES, new Token("TIMES", yytext())); }
"/"     { return symbol(sym.DIV, new Token("DIV", yytext())); }
"="      { return symbol(sym.EQ, new Token("EQ", yytext())); }
"<>"      { return symbol(sym.NE, new Token("NE", yytext())); }
"<"      { return symbol(sym.LT, new Token("LT", yytext())); }
"<="      { return symbol(sym.LE, new Token("LE", yytext())); }
">"      { return symbol(sym.GT, new Token("GT", yytext())); }
">="      { return symbol(sym.GE, new Token("GE", yytext())); }
"&&"     { return symbol(sym.AND, new Token("AND", yytext())); }
"||"      { return symbol(sym.OR, new Token("OR", yytext())); }
"!"     { return symbol(sym.NOT, new Token("NOT", yytext())); }
"@"     { return symbol(sym.REF, new Token("REF", yytext())); }
{IntegerConst} { return symbol(sym.INTEGER_CONST, new Token("INTEGER_CONST", yytext())); }
{RealConst} { return symbol(sym.REAL_CONST, new Token("REAL_CONST", yytext())); }
 "%" {yybegin(BLK_COMMENT);}
"\"" {yybegin(STRING_STATE);}
}

/*stato personalizzato per i commenti*/
<BLK_COMMENT> {
    "%" {
          firstCharacter = false;
          yybegin(YYINITIAL);
    }
    [^%]* {
            if(!firstCharacter) {
                lineUnclosed = yyline+1;
                firstCharacter = true;
            }
            }
    <<EOF>> {
            throw new Error("Errore: Commento non chiuso correttamente alla linea:" + lineUnclosed);
        }
}
/*stato personalizzato per le stringhe*/
<STRING_STATE> {
    \" {
          firstCharacter = false;
          yybegin(YYINITIAL);
          String result = stringBuilder.toString();
          //pulisco lo stringBuilder
          stringBuilder.setLength(0);
          return symbol(sym.STRING_CONST, new Token("STRING_CONST", result));
    }
    [^\"]* {
            if(!firstCharacter) {
                lineUnclosed = yyline+1;
                firstCharacter = true;
            }
            stringBuilder.append(yytext());
            }
    <<EOF>> {
            throw new Error("Errore: Stringa non chiusa correttamente alla linea:" + lineUnclosed);
        }
}

{WhiteSpace} {/*ignore*/}
{Identifier} {return symbol(sym.ID, new Token("ID", yytext())); }
/* error fallback */
[^] { throw new Error("Illegal character <"+ yytext()+"> at line:"+yyline+" and column:" + yycolumn); }