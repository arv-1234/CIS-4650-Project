package absyn;

public class OpExp extends Exp {
  //add onto original OpExp.java to make it hand additional opperators
  public final static int PLUS   = 0;
  public final static int MINUS  = 1;
  public final static int TIMES  = 2;
  public final static int OVER   = 3;
  public final static int EQ     = 4;
  public final static int LT     = 5;
  public final static int LTEQ     = 6;
  public final static int GT     = 7;
  public final static int GTEQ     = 8;
  public final static int NEQ     = 9;
  public final static int APPROX = 10;
  public final static int AND = 11;
  public final static int OR = 12;
  public final static int NOT = 13;
  public final static int UMINUS = 14;

  //left is the LHS of the exp and right is the RHS of the exp
  public Exp left;
  public int op;
  public Exp right;

  public OpExp( int row, int col, Exp left, int op, Exp right ) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.op = op;
    this.right = right;
  }

  @Override
  public int getType() {
      // Determine type based on operation semantics
      if (op == PLUS || op == MINUS || op == TIMES || op == OVER) {
          return 0; // Arithmetic operations return int
      } else {
          return 3; // Comparisons return bool
      }
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }

  //** from here are helps to help us with C3 **/
  public String getOpName(){

    String tmpStr = "";
  
    switch(op){

      case 0:
        tmpStr = "ADD";
        break;
      
      case 1:
        tmpStr = "SUB";
        break;
    
      case 2:
        tmpStr = "MUL";
        break;
    
      case 3:
        tmpStr = "DIV";
        break;
    
      case 4:
        tmpStr = "EQ";
        break;
    
      case 5:
        tmpStr = "LT";
        break;

      case 6:
        tmpStr = "LTEQ";
        break;
    
      case 7:
        tmpStr = "GT";
        break;
    
      case 8:
        tmpStr = "GTEQ";
        break;
    
      case 9:
        tmpStr = "NEQ";
        break;
    
      case 10:
        tmpStr = "APPROX";
        break;
    
      case 11:
        tmpStr = "AND";
        break;
    
      case 12:
        tmpStr = "OR";
        break;
    
      case 13:
        tmpStr = "NOT";
        break;
    
      case 14:
        tmpStr = "UMINUS";
        break;
    
    }
    return tmpStr;
  }

  public String getOpSymbol(){
    String tmpStr = "";
  
    switch(op){

      case 0:
        tmpStr = "+";
        break;
      
      case 1:
        tmpStr = "-";
        break;
    
      case 2:
        tmpStr = "*";
        break;
    
      case 3:
        tmpStr = "/";
        break;
    
      case 4:
        tmpStr = "==";
        break;
    
      case 5:
        tmpStr = "<";
        break;

      case 6:
        tmpStr = "<=";
        break;
    
      case 7:
        tmpStr = ">";
        break;
    
      case 8:
        tmpStr = ">=";
        break;
    
      case 9:
        tmpStr = "!=";
        break;
    
      case 10:
        tmpStr = "~";
        break;
    
      case 11:
        tmpStr = "&&";
        break;
    
      case 12:
        tmpStr = "||";
        break;
    
      case 13:
        tmpStr = "!";
        break;
    
      case 14:
        tmpStr = "--"; //idk what this is supposed to be
        break;
    
    }
    return tmpStr;
  }


}
