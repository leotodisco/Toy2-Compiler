# Compilatore Toy2
Progetto di Benedetto Scala e Leopoldo Todisco per il corso di Compilatori. <br>
Per eseguire il progetto occorre usare Java 17 e maven 3.8
***
## Specifiche Lessicali
In questa sezione vi sono tutti i token e i corrispettivi lessemi. <BR>
Le regole di inferenza si possono trovare nel file pdf inviato con il link della repository.

| Token       |         Lessema          |
|-------------|:------------------------:|
| VAR         |           var            |
| ID          | \[A-Za-z_]\[A-Za-z0-9_]* |
| INTEGER     |         integer          |
| REAL        |           real           |
| STRING      |          string          |
| LPAR        |            (             |
| RPAR        |            )             |
| FUNC        |           func           |
| ENDFUNC     |         endfunc          |
| TYPERETURN  |            ->            |
| READ        |           <--            |
| WRITERETURN |           -->!           |
| WRITE       |           -->            |
| DOLLARSIGN  |            $             |
| OUT         |           out            |
| PROC        |           proc           |
| ENDPROC     |         endproc          |
| IF          |            if            |
| THEN        |           then           |
| ELSE        |           else           |
| ELSEIF      |          elseif          |
| DO          |            do            |
| ENDWHILE    |         endwhile         |
| ENDIF       |          endif           |
| PLUS        |            +             |
| MINUS       |            -             |
| DIV         |            /             |
| TIMES       |            *             |
| NEQ         |            <>            |
| EQ          |            =             |
| ASSIGN      |            ^=            |
| LT          |            <             |
| GT          |            >             |
| LE          |            <=            |
| GE          |            >=            |
| REF         |            @             |
| NOT         |            !             |
| OR          |           \|\|           |
| AND         |            &&            |
| SEMI        |            ;             |
| COLON       |            :             |
| COMMA       |            ,             |
| ENDVAR      |            \\            |
| TRUE        |           true           |
| FALSE       |          false           |

## Grammatica

***
precedence left OR; <br>
precedence left AND;<br>
precedence right NOT;<br>
precedence nonassoc EQ, NE, LT, LE, GT, GE;<br>
precedence left PLUS, MINUS;<br>
precedence left TIMES, DIV;<br>


Program ::= IterWithoutProcedure Procedure Iter;

IterWithoutProcedure ::= VarDecl IterWithoutProcedure <br>
| Function IterWithoutProcedure <br>
| /* empty */ ; 

Iter ::= VarDecl Iter <br>
| Function Iter <br>
| Procedure Iter <br>
| /* empty */ ;

VarDecl ::= VAR Decls;

Decls ::= Ids COLON Type SEMI Decls  <br>
| Ids:ids ASSIGN Consts:consts SEMI Decls:decls  <br>
	| Ids:ids COLON Type:tipo SEMI ENDVAR  <br>
	| Ids:ids ASSIGN Consts:consts SEMI ENDVAR  <br>

Ids ::= ID:id COMMA Ids:ids  <br> 
| ID:id;

Consts ::= Const:costante COMMA Consts:listaCostanti  <br>
| Const:costante;

Const ::= REAL_CONST:lessema  <br>
| INTEGER_CONST <br>
| STRING_CONST <br>
| TRUE <br>
| FALSE ;

Type ::= REAL  <br>
| INTEGER  <br>
| STRING  <br>
| BOOLEAN ;

Function  ::= FUNCTION ID LPAR FuncParams RPAR TYPERETURN Types

FuncParams ::= ID COLON Type OtherFuncParams <br>
| /* empty */;

OtherFuncParams ::= COMMA ID COLON Type OtherFuncParams:paramsList  <br>
| /* empty */;

Types ::= Type COMMA Types  <br>
| Type;  <br>

Procedure ::= PROCEDURE ID LPAR ProcParams RPAR COLON Body ENDPROCEDURE

ProcParams::= ProcParamId COLON Type OtherProcParams <br>
| /* empty */ ;

OtherProcParams ::= COMMA ProcParamId COLON Type OtherProcParams  <br>
| /* empty */;

ProcParamId ::= ID  <br>
| OUT ID;

Body ::= VarDecl Body  <br>
| Stat Body  <br>
| /* empty */;

Stat ::= Ids ASSIGN Exprs SEMI  <br>
| ProcCall SEMI  <br>
| RETURN Exprs SEMI  <br>
| WRITE IOArgs SEMI <br>
| WRITERETURN IOArgs SEMI  <br>
| READ IOArgs SEMI <br>
| IfStat SEMI  <br>
| WhileStat SEMI;

FunCall ::= ID LPAR Exprs RPAR <br>
| ID LPAR RPAR; 

ProcCall ::= ID LPAR ProcExprs RPAR  <br>
| ID LPAR RPAR;

IfStat ::= IF Expr THEN Body Elifs Else ENDIF;

Elifs ::= Elif Elifs <br>
| /* empty */; 

Elif ::= ELIF Expr THEN Body <br>

Else ::= ELSE Body <br>
| /* empty */;

WhileStat ::= WHILE Expr DO Body ENDWHILE;

IOArgs ::= IOArgsExpr IOArgs  <br>
|  DOLLARSIGN LPAR Expr RPAR IOArgs
| /* empty */;

IOArgsExpr ::= ID <br>
| STRING_CONST <br>
| IOArgsExpr PLUS IOArgsExpr; 

ProcExprs::= Expr COMMA ProcExprs <br>
| REF ID COMMA ProcExprs <br>
| Expr <br>
| REF ID;

Exprs ::= Expr COMMA Exprs <br>
| Expr;

Expr ::= FunCall <br>
| REAL_CONST <br>
| INTEGER_CONST <br>
| STRING_CONST <br>
| ID <br>
| TRUE <br>
| FALSE <br>
| Expr PLUS Expr <br>
| Expr MINUS Expr <br>
| Expr TIMES Expr <br>
| Expr DIV Expr <br>
| Expr AND Expr <br>
| Expr OR Expr <br>
| Expr GT Expr <br>
| Expr GE Expr <br>
| Expr LT Expr <br>
| Expr LE Expr <br>
| Expr EQ Expr <br>
| Expr NE Expr <br>
| LPAR Expr RPAR <br>
| MINUS Expr<br>
| NOT Expr;

***

## Modifiche Aggiuntive
Nel Lexer sono state aggiunte delle parole riservate che servono nella traduzione in C:
- daRestituire
- r [0-9


Inoltre nel nostro compilatore è stato anche eseguito un controllo relativo ai return quando si ha un if-else.
Come si può vedere nell'esempio di seguito, sebbene non ci sia un return statement alla fine, il codice viene compilato poichè sia il body dell'if che il body dell'else hanno un return statement.


	`func funzione(a:integer)->integer :
		if a>8 then
			return 8;
		else
			return 10;
		endif;
	endfunc`

Inoltre, nella repository è presente una branch chiamata "OrderOfVariable" nella quale è presente una prima versione del compilatore (incompleta) in cui si poteva usare una variabile se e solo se questa era stata dichiarata nelle linee precedenti di codice.
Ciò è stato fatto grazie alla classe DefaultMutableTreeNode che è stata anche usata per produrre l'albero sintattico.
Nel metodo visit dello ScopeCheckingVisitor si iterava su tutti i figli di un body e si visitava l'albero al contrario, in questo modo se, in uno statement, si usava una variabile che non era presente nella tabella dei simboli si lanciava un'eccezione.
Nella repository è anche presente una branch chiamata "CodeGeneratorVisitor" nella quale è presente una versione del compilatore (anche questa incompleta) che consentiva di usare funzioni con multipli tipi di ritorno anche nei parametri di altre funzioni.


