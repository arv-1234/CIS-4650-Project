package absyn;

public class CompoundStmt extends absyn {
  public String value;
  public DecList LocalDeclarations;
  public ExpList statementList;

  public CompoundStmt( int row, int col, DecList decs, ExpList stmts ) {
    this.row = row;
    this.col = col;
    this.LocalDeclarations = decs;
    this.statementList = stmts;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
