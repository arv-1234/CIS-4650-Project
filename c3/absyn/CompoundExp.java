package absyn;

public class CompoundExp extends Exp {
  public VarDecList decs;
  public ExpList exps;

  public CompoundExp( int row, int col, VarDecList decs, ExpList stmts ) {
    this.row = row;
    this.col = col;
    this.decs = decs;
    this.exps = stmts;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
