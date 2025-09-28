lexer grammar Splc;

// IDEA Plugin Settings
// - Output Directory: src/main/java/
// - package name: generated.Splc

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