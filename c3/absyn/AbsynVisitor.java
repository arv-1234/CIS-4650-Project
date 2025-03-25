package absyn;

public interface AbsynVisitor {

  public void visit( ExpList exp, int level, boolean flag );

  public void visit( AssignExp exp, int level, boolean flag );

  public void visit( IfExp exp, int level, boolean flag );

  public void visit( IntExp exp, int level, boolean flag );

  public void visit( OpExp exp, int level, boolean flag );

  public void visit( WhileExp exp, int level, boolean flag );

  public void visit( VarExp exp, int level, boolean flag );

  public void visit( DecList decList, int level, boolean flag );

  public void visit( ArrayDec arrDec, int level, boolean flag );

  public void visit( BoolExp exp , int level, boolean flag );

  public void visit( CallExp exp, int level, boolean flag );

  public void visit( CompoundExp exp, int level, boolean flag );

  public void visit( FunctionDec FunDec, int level, boolean flag );

  public void visit( IndexVar var, int level, boolean flag );

  public void visit( NameTy type, int level, boolean flag );

  public void visit( NilExp exp, int level, boolean flag );

  public void visit( ReturnExp exp, int level, boolean flag );

  public void visit( SimpleDec dec, int level, boolean flag );

  public void visit( SimpleVar var, int level, boolean flag );

  public void visit( VarDecList varDecList, int level, boolean flag );


}
