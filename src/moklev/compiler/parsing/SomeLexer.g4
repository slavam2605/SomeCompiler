lexer grammar SomeLexer;

PLUS:       '+';
STAR:       '*';
EQEQ:       '==';
LESS:       '<';
LPAREN:     '(';
RPAREN:     ')';
COMMA:      ',';
COLON:      ':';
LBRACE:     '{';
RBRACE:     '}';
EQUALS:     '=';
SEMICOLON:  ';';

IF:         'if';
WHILE:      'while';
FUN:        'fun';
ELSE:       'else';
RETURN:     'return';
VAR:        'var';

IDENT:      [a-zA-Z_]+ [a-zA-Z0-9_]*;
NUMBER:     [0-9]+ '.' [0-9]+ | [0-9]+;

WS:         [ \t\n\r]+ -> skip;