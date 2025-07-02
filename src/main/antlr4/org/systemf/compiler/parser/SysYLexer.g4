lexer grammar SysYLexer;

CONST : 'const';

INT : 'int';

FLOAT : 'float';

VOID : 'void';

IF : 'if';

ELSE : 'else';

WHILE : 'while';

BREAK : 'break';

CONTINUE : 'continue';

RETURN : 'return';

PLUS : '+';

MINUS : '-';

MUL : '*';

DIV : '/';

MOD : '%';

ASSIGN : '=';

EQ : '==';

NEQ : '!=';

LT : '<';

GT : '>';

LE : '<=';

GE : '>=';

NOT : '!';

AND : '&&';

OR : '||';

L_PAREN : '(';

R_PAREN : ')';

L_BRACE : '{';

R_BRACE : '}';

L_BRACKT : '[';

R_BRACKT : ']';

COMMA : ',';

SEMICOLON : ';';

fragment NON_DIGIT : [a-zA-Z_];

fragment NON_ZERO_DECIMAL_DIGIT : [1-9];
fragment DECIMAL_DIGIT : '0' | NON_ZERO_DECIMAL_DIGIT;
fragment OCTAL_DIGIT : [0-7];
fragment HEX_DIGIT : [0-9a-fA-F];

IDENT : NON_DIGIT (NON_DIGIT | DECIMAL_DIGIT)*;

fragment DECIMAL_INTEGER : NON_ZERO_DECIMAL_DIGIT DECIMAL_DIGIT*;
fragment OCTAL_INTEGER : '0' OCTAL_DIGIT*;
fragment HEX_INTEGER : '0' [xX] HEX_DIGIT+;

INTEGER_CONST : DECIMAL_INTEGER | OCTAL_INTEGER | HEX_INTEGER;

WS : [ \r\n\t]+ -> skip;

LINE_COMMENT : '//' .*? ('\n' | EOF) -> skip;

MULTILINE_COMMENT : '/*' .*? '*/' -> skip;