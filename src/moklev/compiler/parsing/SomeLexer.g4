lexer grammar SomeLexer;

PLUS:       '+';
STAR:       '*';
AMPERSAND:  '&';
ANDAND:     '&&';
OROR:       '||';
MINUS:      '-';
SLASH:      '/';
EQEQ:       '==';
LESS:       '<';
LPAREN:     '(';
RPAREN:     ')';
COMMA:      ',';
DOT:        '.';
COLON:      ':';
LBRACE:     '{';
RBRACE:     '}';
EQUALS:     '=';
SEMICOLON:  ';';
LSQBRACKET: '[';
RSQBRACKET: ']';

IF:         'if';
WHILE:      'while';
FUN:        'fun';
ELSE:       'else';
RETURN:     'return';
VAR:        'var';
CLASS:      'class';

IDENT:      [a-zA-Z_]+ [a-zA-Z0-9_]*;
NUMBER:     [0-9]+ '.' [0-9]+ | [0-9]+;

WS:         [ \t\n\r]+ -> skip;