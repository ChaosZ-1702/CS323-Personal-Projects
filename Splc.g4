//lexer grammar Splc;
grammar Splc;

// Changes in Project 2: Splc.g4 contains both parser rules and lexer rules.
//  so there should be "grammar Splc;' instead of 'lexer grammer Splc;'

// IDEA Plugin Settings
// - Output Directory: src/main/java/
// - package name: generated.Splc

// =========================
// Parser Rules
// =========================

program: globalDef* EOF;

globalDef: specifier Identifier LPAREN funcArgs RPAREN LBRACE statement* RBRACE |
    specifier varDec SEMI |
    specifier SEMI;

specifier: INT | CHAR |
    STRUCT Identifier | STRUCT Identifier LBRACE (specifier varDec SEMI)* RBRACE;

varDec: Identifier | varDec LBRACK Number RBRACK | STAR varDec | LPAREN varDec RPAREN;

funcArgs: (specifier varDec(COMMA specifier varDec)*)?;

statement:
    LBRACE statement* RBRACE  # blockStmt  |
    specifier varDec (ASSIGN expression)? SEMI  # varDecStmt |
    IF LPAREN expression RPAREN statement (ELSE statement)?  # ifStmt |
    WHILE LPAREN expression RPAREN statement  # whileStmt |
    RETURN expression? SEMI  # returnStmt |
    expression SEMI  # exprStmt;

expression: Identifier | Number | Char | LPAREN expression RPAREN |
    expression (INC | DEC) |
    Identifier LPAREN (expression (COMMA expression)*)? RPAREN |
    expression LBRACK expression RBRACK |
    expression (DOT | ARROW) Identifier |
    (INC | DEC | PLUS | MINUS | NOT | STAR | AMP)+ expression |
    expression (STAR | DIV | MOD) expression |
    expression (PLUS | MINUS) expression |
    expression (LT | LE | GT | GE) expression |
    expression (EQ | NEQ) expression |
    expression (AND) expression |
    expression (OR) expression |
    expression ASSIGN expression;

// =========================
// Lexer Rules
// =========================

// ---------- Keywords ----------
INT    : 'int';
CHAR   : 'char' ;
STRUCT : 'struct' ;
RETURN : 'return' ;
IF     : 'if' ;
ELSE   : 'else' ;
WHILE  : 'while' ;

// ---------- Operators ----------
ASSIGN : '=' ;
PLUS   : '+' ;
MINUS  : '-' ;
STAR   : '*' ;
DIV    : '/' ;
MOD    : '%' ;
LT     : '<' ;
LE     : '<=' ;
GT     : '>' ;
GE     : '>=' ;
EQ     : '==' ;
NEQ    : '!=' ;
AND    : '&&' ;
OR     : '||' ;
NOT    : '!' ;
INC    : '++' ;
DEC    : '--' ;
DOT    : '.' ;
ARROW  : '->' ;
AMP    : '&' ;

// ---------- Separators ----------
SEMI   : ';' ;
COMMA  : ',' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
LBRACK : '[' ;
RBRACK : ']' ;

// ---------- Identifiers & Literals ----------
Identifier : [a-zA-Z_][a-zA-Z0-9_]* ;
Number     : [0-9] | [1-9][0-9]* ;
Char       : '\'' (~['\\] | '\\'.) '\'' ;

// ---------- Whitespace & Comments ----------
WS            : [ \f\n\r\t\u000B]+ -> skip ;  // why no \v...
LINE_COMMENT  : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;