package absyn;

public class OpExp extends Exp {
  //add onto original OpExp.java to make it hand additional opperators
  public final static int PLUS   = 0;
  public final static int MINUS  = 1;
  public final static int TIMES  = 2;
  public final static int OVER   = 3;
  public final static int EQ     = 4;
  public final static int LT     = 5;
  public final static int LTE     = 6;
  public final static int GT     = 7;
  public final static int GTE     = 8;
  public final static int NEQ     = 9;
  public final static int UMINUS = 10;
  public final static int AND = 11;
  public final static int OR = 12;
  public final static int NOT = 13;

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

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
