package absyn;

public class ReturnExp extends Exp {
  public Exp exp;

  public ReturnExp( int row, int col, Exp returnExpression ) {
    this.row = row;
    this.col = col;
    this.exp = returnExpression;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}

