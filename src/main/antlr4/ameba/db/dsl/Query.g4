grammar Query;

query : sourceElements? EOF;

sourceElements : sourceElement+;

sourceElement : expression ('.' expression)?;

arguments
 : '(' argumentList? ')'
 ;

argumentList
 : (COMMA? (literal | identifierVal)+ COMMA?)+
 | sourceElement+
 ;

method
 : identifierName arguments
 ;

expression
  : method                              # CallExpression
  | Identifier (('.' Identifier)+)?     # FieldExpression
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
 | 'N'
 ;

BooleanLiteral
 : 'true' | 'T'
 | 'false' | 'F'
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


COMMA : '!'|',';
QUOTE : '\'';