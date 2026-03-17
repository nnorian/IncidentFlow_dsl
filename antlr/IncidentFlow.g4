grammar IncidentFlow;

// ============================================================
// Parser Rules (P1-P45)
// ============================================================

program
    : config_block* import_decl* playbook_decl+ EOF
    ;

import_decl
    : IMPORT STRING (AS IDENTIFIER)?
    ;

config_block
    : CONFIG config_name COLON config_entry+
    ;

config_name
    : IDENTIFIER
    ;

config_entry
    : IDENTIFIER COLON config_value
    ;

config_value
    : STRING
    | INT
    | severity_lit
    | bool_lit
    | list_lit
    | chain_lit
    ;

list_lit
    : LBRACKET config_value (COMMA config_value)* RBRACKET
    ;

chain_lit
    : IDENTIFIER (ARROW IDENTIFIER)+
    ;

playbook_decl
    : PLAYBOOK IDENTIFIER trigger_decl phase_decl+ report_decl?
    ;

trigger_decl
    : TRIGGER COLON expr
    ;

phase_decl
    : PHASE IDENTIFIER COLON statement+
    ;

report_decl
    : REPORT COLON statement+
    ;

statement
    : do_stmt
    | if_stmt
    | for_stmt
    | wait_stmt
    | verify_stmt
    | log_stmt
    | goto_stmt
    | assign_stmt
    | parallel_block
    | severity_stmt
    ;

do_stmt
    : DO function_call
    ;

if_stmt
    : IF expr THEN COLON statement+ (ELSE COLON statement+)?
    ;

for_stmt
    : FOR EACH IDENTIFIER IN expr COLON statement+
    ;

wait_stmt
    : WAIT IDENTIFIER FROM expr TIMEOUT DURATION timeout_handler?
    ;

timeout_handler
    : ON_TIMEOUT COLON statement+
    ;

verify_stmt
    : VERIFY expr fail_handler?
    ;

fail_handler
    : ON_FAIL COLON statement+
    ;

log_stmt
    : LOG expr
    ;

goto_stmt
    : GOTO IDENTIFIER
    ;

assign_stmt
    : IDENTIFIER ASSIGN expr
    ;

parallel_block
    : PARALLEL COLON statement+
    ;

severity_stmt
    : SEVERITY severity_lit
    ;

expr
    : or_expr
    ;

or_expr
    : and_expr (OR and_expr)*
    ;

and_expr
    : not_expr (AND not_expr)*
    ;

not_expr
    : NOT not_expr
    | comparison
    ;

comparison
    : primary (comp_op primary)?
    ;

comp_op
    : EQ
    | NEQ
    | GTE
    | LTE
    | GT
    | LT
    ;

primary
    : function_call
    | member_access
    | IDENTIFIER
    | literal
    | LPAREN expr RPAREN
    ;

member_access
    : IDENTIFIER (DOT IDENTIFIER)+
    ;

function_call
    : IDENTIFIER LPAREN argument_list? RPAREN
    ;

argument_list
    : argument (COMMA argument)*
    ;

argument
    : IDENTIFIER COLON expr
    | expr
    ;

literal
    : STRING
    | INT
    | DURATION
    | bool_lit
    | severity_lit
    | list_lit
    ;

bool_lit
    : TRUE
    | FALSE
    ;

severity_lit
    : LOW
    | MEDIUM
    | HIGH
    | CRITICAL
    ;

// ============================================================
// Lexer Rules (Token Spec)
// ============================================================

// Keywords
PLAYBOOK    : 'PLAYBOOK' ;
CONFIG      : 'CONFIG' ;
TRIGGER     : 'TRIGGER' ;
PHASE       : 'PHASE' ;
DO          : 'DO' ;
IF          : 'IF' ;
THEN        : 'THEN' ;
ELSE        : 'ELSE' ;
FOR         : 'FOR' ;
EACH        : 'EACH' ;
IN          : 'IN' ;
WAIT        : 'WAIT' ;
FROM        : 'FROM' ;
TIMEOUT     : 'TIMEOUT' ;
ON_TIMEOUT  : 'ON_TIMEOUT' ;
ON_FAIL     : 'ON_FAIL' ;
VERIFY      : 'VERIFY' ;
LOG         : 'LOG' ;
GOTO        : 'GOTO' ;
REPORT      : 'REPORT' ;
AND         : 'AND' ;
OR          : 'OR' ;
NOT         : 'NOT' ;
PARALLEL    : 'PARALLEL' ;
SEVERITY    : 'SEVERITY' ;
IMPORT      : 'IMPORT' ;
AS          : 'AS' ;
TRUE        : 'TRUE' ;
FALSE       : 'FALSE' ;
LOW         : 'LOW' ;
MEDIUM      : 'MEDIUM' ;
HIGH        : 'HIGH' ;
CRITICAL    : 'CRITICAL' ;

// Identifier / Literals
IDENTIFIER  : [a-zA-Z_][a-zA-Z0-9_]* ;
STRING      : '"' (~["\\\r\n] | '\\' .)* '"' ;
DURATION    : [0-9]+ ('sec' | 'min' | 'hr') ;
INT         : [0-9]+ ;

// Comparison operators (longest first)
EQ          : '==' ;
NEQ         : '!=' ;
GTE         : '>=' ;
LTE         : '<=' ;
GT          : '>' ;
LT          : '<' ;

// Punctuation / assignment / arrow
COLON       : ':' ;
COMMA       : ',' ;
DOT         : '.' ;
LPAREN      : '(' ;
RPAREN      : ')' ;
LBRACKET    : '[' ;
RBRACKET    : ']' ;
ASSIGN      : '=' ;
ARROW       : '->' ;

// Comments / whitespace / BOM
COMMENT       : '#' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
WHITESPACE    : [ \t\r\n]+ -> skip ;
BOM           : '\uFEFF' -> skip ;
