package absyn;

public class CompoundStmt extends absyn {
  public String value;
  public DecList declarations;
  public DecList statementList;

  public IntExp( int row, int col, String value ) {
    this.row = row;
    this.col = col;
    this.value = value;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
