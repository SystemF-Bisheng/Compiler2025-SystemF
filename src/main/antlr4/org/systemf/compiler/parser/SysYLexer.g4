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
fragment DECIMAL_SEQ : DECIMAL_DIGIT+;
fragment HEX_SEQ : HEX_DIGIT+;

IDENT : NON_DIGIT (NON_DIGIT | DECIMAL_DIGIT)*;

fragment SIGN : [+-];
fragment HEX_PREFIX : '0' [xX];

fragment DECIMAL_INTEGER : NON_ZERO_DECIMAL_DIGIT DECIMAL_DIGIT*;
fragment OCTAL_INTEGER : '0' OCTAL_DIGIT*;
fragment HEX_INTEGER : HEX_PREFIX HEX_SEQ;

INTEGER_CONST : DECIMAL_INTEGER | OCTAL_INTEGER | HEX_INTEGER;

fragment DEC_FRACTIONAL : DECIMAL_SEQ? '.' DECIMAL_SEQ | DECIMAL_SEQ '.';
fragment EXPONENT : [eE] SIGN? DECIMAL_SEQ;

fragment HEX_FRACTIONAL : HEX_SEQ? '.' HEX_SEQ | HEX_SEQ '.';
fragment BIN_EXPONENT : [pP] SIGN? DECIMAL_SEQ;

fragment DEC_FLOAT : DEC_FRACTIONAL EXPONENT? | DECIMAL_SEQ EXPONENT;
fragment HEX_FLOAT : HEX_PREFIX (HEX_FRACTIONAL | HEX_SEQ) BIN_EXPONENT;

FLOAT_CONST : DEC_FLOAT | HEX_FLOAT;

WS : [ \r\n\t]+ -> skip;

LINE_COMMENT : '//' .*? ('\n' | EOF) -> skip;

MULTILINE_COMMENT : '/*' .*? '*/' -> skip;