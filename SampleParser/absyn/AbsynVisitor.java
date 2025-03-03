package absyn;

public interface AbsynVisitor {

  public void visit( ExpList exp, int level );

  public void visit( AssignExp exp, int level );

  public void visit( IfExp exp, int level );

  public void visit( IntExp exp, int level );

  public void visit( OpExp exp, int level );

  public void visit( WhileExp exp, int level );

  public void visit( VarExp exp, int level );

  public void visit( DecList decList, int level );

  public void visit( ArrayDec arrDec, int level );

  public void visit( BoolExp exp , int level );

  public void visit( CallExp exp, int level );

  public void visit( CompoundExp exp, int level );

  public void visit( FunctionDec FunDec, int level );

  public void visit( IndexVar var, int level );

  public void visit( NameTy type, int level );

  public void visit( NilExp exp, int level );

  public void visit( ReturnExp exp, int level );

  public void visit( SimpleDec dec, int level );

  public void visit( SimpleVar var, int level );

  public void visit( VarDec dec, int level );

  public void visit( VarDecList varDecList, int level );


}
