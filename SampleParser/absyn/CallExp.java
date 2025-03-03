package absyn;

public class CallExp extends Exp {
  public String fun;
  public ExpList args;

  public CallExp( int row, int col, String funcName, ExpList arguments ) {
    this.row = row;
    this.col = col;
    this.test = test;
    this.fun = funcName;
    this.args = arguments;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}

