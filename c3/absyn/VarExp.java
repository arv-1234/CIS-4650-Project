package absyn;

public class VarExp extends Exp {
  public Var variable;

  public VarExp( int row, int col, Var v ) {
    this.row = row;
    this.col = col;
    this.variable = v;
  }

  public void accept( AbsynVisitor visitor, int level, boolean flag ) {
    visitor.visit( this, level, flag );
  }
}
