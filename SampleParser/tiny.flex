/*
   Created By: Fei Song
   Modified By: Jessica Nguyen
   File Name: tiny.flex
   
   To Build (no Parser): jflex tiny.flex
   To Build (with Parser): javac Lexer.java
*/
   
/* --------------------------Usercode Section------------------------ */
import java_cup.runtime.*;
      
%%

/* -----------------Options and Declarations Section----------------- */
/* 
   The name of the class JFlex will create will be Lexer.
   Will write the code to the file Lexer.java. 
*/
%class Lexer

%eofval{
  return null;
%eofval};

/*
  The current line number can be accessed with the variable yyline
  and the current column number with the variable yycolumn.
*/
%line
%column
    
/* 
   Will switch to a CUP compatibility mode to interface with a CUP
   generated parser.
*/
%cup
   
/*
  Declarations
   
  Code between %{ and %}, both of which must be at the beginning of a
  line, will be copied letter to letter into the lexer class source.
  Here you declare member variables and functions that are used inside
  scanner actions.  
*/
%{   
   /* To create a new java_cup.runtime.Symbol with information about
      the current token, the token will have no value in this case. */
   private Symbol symbol(int type) {
      return new Symbol(type, yyline, yycolumn);
   }
    
   /* Also creates a new java_cup.runtime.Symbol with information
      about the current token, but this object has a value. */
   private Symbol symbol(int type, Object value) {
      return new Symbol(type, yyline, yycolumn, value);
   }
%}

/*
   Macro Declarations
   These declarations are regular expressions that will be used latter
   in the Lexical Rules Section.  
*/
   
/* A line terminator is a \r (carriage return), \n (line feed), or \r\n. */
lineTerminator = \r|\n|\r\n
   
/* White space is a line terminator, blank, tab, or newline. */
whiteSpace     = {lineTerminator} | [ \t\n]
   
/* A identifier integer is a word beginning a letter between A and
   Z, a and z, or an underscore followed by zero or more letters
   between A and Z, a and z, zero and nine, or an underscore. */
identifier = [_a-zA-Z][_a-zA-Z0-9]*

/* A literal integer is a number beginning with a number between
   one and nine followed by zero or more numbers between zero and nine
   or just a zero.  */
number = [0-9]+

/* A truth value is a boolean value that can be either false or true */
truth = "false" | "true"
   
%%
/* ------------------------Lexical Rules Section---------------------- */
/*
   This section contains regular expressions and actions, i.e. Java
   code, that will be executed when the scanner matches the associated
   regular expression. 
*/   

"bool"             { return symbol(sym.BOOL); }
"else"             { return symbol(sym.ELSE); }
"if"               { return symbol(sym.IF); }
"int"              { return symbol(sym.INT); }
"return"           { return symbol(sym.RETURN); }
"void"             { return symbol(sym.VOID); }
"while"            { return symbol(sym.WHILE); }
{truth}            { return symbol(sym.TRUTH, yytext()); }

"+"                { return symbol(sym.PLUS); }
"-"                { return symbol(sym.MINUS); }
"*"                { return symbol(sym.TIMES); }
"/"                { return symbol(sym.OVER); }
"<"                { return symbol(sym.LT); }
"<="               { return symbol(sym.LTEQ); }
">"                { return symbol(sym.GT); }
">="               { return symbol(sym.GTEQ); }
"=="               { return symbol(sym.EQ); }
"!="               { return symbol(sym.NEQ); }
"~"                { return symbol(sym.APPROX); }
"||"               { return symbol(sym.OR); }
"&&"               { return symbol(sym.AND); }
"="                { return symbol(sym.ASSIGN); }
";"                { return symbol(sym.SEMI); }
","                { return symbol(sym.COMMA); }
"("                { return symbol(sym.LPAREN); }
")"                { return symbol(sym.RPAREN); }
"["                { return symbol(sym.LBRACK); }
"]"                { return symbol(sym.RBRACK); }
"{"                { return symbol(sym.LCURLBRACK); }
"}"                { return symbol(sym.RCURLBRACK); }

{identifier}       { return symbol(sym.ID, yytext()); }
{number}           { return symbol(sym.NUM, yytext()); }

{whiteSpace}+      { /* skip whitespace */ }   
"{"[^\}]*"}"       { /* skip comments */ }

.                  { return symbol(sym.ERROR); }