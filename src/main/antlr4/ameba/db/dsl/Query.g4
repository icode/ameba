grammar Query;

query : sourceElements? EOF;

sourceElements : sourceElement+;

sourceElement : expression ('.' expression)?;

arguments
 : '(' argumentList? ')'
 ;

argumentList
 : sourceElement+
 | ('!'? (literal | identifierVal)+ '!'?)+
 | literal (COMMA? literal)*
 ;

expression
  : identifierName arguments                        # CallExpression
  | Identifier (('.' Identifier)+)?                 # FieldExpression
  ;

identifierName : Identifier;
identifierVal: Identifier;

literal
 : NullLiteral
 | BooleanLiteral
 | StringLiteral
 | DecimalLiteral
 ;

NullLiteral
 : 'null'
 | 'nil'
 ;

BooleanLiteral
 : 'true'
 | 'false'
 ;

StringLiteral
 : QUOTE (~'\'' | COMMA QUOTE)* QUOTE
 {
    setText(getText().substring(1, getText().length()-1).replace("!'","'"));
 }
 ;

DecimalLiteral
 : [+-]? (DecimalIntegerLiteral '.' DecimalDigit*
 | '.' DecimalDigit+
 | DecimalIntegerLiteral)
 ;

fragment DecimalDigit
 : [0-9]
 ;

fragment DecimalIntegerLiteral
 : '0'
 | [1-9] DecimalDigit*
 ;

Identifier : Letter LetterOrDigit*;

fragment Letter : [a-zA-Z$_];

fragment LetterOrDigit : [a-zA-Z0-9$_];


COMMA : '!';
QUOTE : '\'';

WS :  (' '|'\r'|'\t'|'\u000C'|'\n')+ -> skip;
