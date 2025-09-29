grammar Calc1;

root: lower_expr <EOF>;

lower_expr    : upper_expr ADD upper_expr
              | upper_expr SUB upper_expr
              | upper_expr
              ;

upper_expr    : upper_expr TIMES upper_expr
              | upper_expr DIV upper_expr
              | factor
              ;

factor        : INT
              | '(' lower_expr ')'
              ;

INT : [0-9]+ ;
ADD : '+' ;
SUB : '-' ;
TIMES : '*' ;
DIV : '/' ;

WS  : [ \t\r\n]+ -> skip ;