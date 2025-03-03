import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( ExpList expList, int level ) {
    while( expList != null ) {
      expList.head.accept( this, level );
      expList = expList.tail;
    } 
  }

  public void visit( AssignExp exp, int level ) {
    indent( level );
    System.out.println( "AssignExp:" );
    level++;
    exp.lhs.accept( this, level );
    exp.rhs.accept( this, level );
  }

  public void visit( IfExp exp, int level ) {
    indent( level );
    System.out.println( "IfExp:" );
    level++;
    exp.test.accept( this, level );
    exp.thenpart.accept( this, level );
    if (exp.elsepart != null )
       exp.elsepart.accept( this, level );
  }

  public void visit( IntExp exp, int level ) {
    indent( level );
    System.out.println( "IntExp: " + exp.value ); 
  }

  public void visit( OpExp exp, int level ) {
    indent( level );
    System.out.print( "OpExp:" ); 
    switch( exp.op ) {
      case OpExp.PLUS:
        System.out.println( " + " );
        break;
      case OpExp.MINUS:
        System.out.println( " - " );
        break;
      case OpExp.TIMES:
        System.out.println( " * " );
        break;
      case OpExp.OVER:
        System.out.println( " / " );
        break;
      case OpExp.EQ:
        System.out.println( " = " );
        break;
      case OpExp.LT:
        System.out.println( " < " );
        break;
      case OpExp.GT:
        System.out.println( " > " );
        break;
      case OpExp.UMINUS:
        System.out.println( " - " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    if (exp.left != null)
       exp.left.accept( this, level );
    if (exp.right != null)
       exp.right.accept( this, level );
  }


  public void visit( VarExp expVar, int level ) {
    indent( level );

    System.out.println( "VarExp:");

    level++;
    
    expVar.variable.accept(this, level);
  }

  public void visit(ArrayDec arrayDec, int level )
  {
    indent( level );

    level++;

    System.out.println("ArrayDec:");

    indent( level );

    if(arrayDec.typ.typeVal == 1)
    {
      System.out.println("NameTy: int");

      indent( level );

      System.out.println("Name: " + arrayDec.name);

      if( arrayDec.size != 0)
      {
        indent( level );
        System.out.println("Size: " + arrayDec.size );
      }
    }
  }

  public void visit(BoolExp expBool, int level)
  {
    indent( level );
    level++;
    System.out.println("BoolExp: " + expBool.value);
  }

  public void visit(CallExp expCall, int level)
  {
    indent( level );

    System.out.println("CallExp: ");

    level++;

    indent( level );

    System.out.println("Func: " + expCall.fun);

    ExpList listOfArguments = expCall.args;

    while(listOfArguments != null)
    {
      listOfArguments.head.accept(this, level);

      listOfArguments = listOfArguments.tail;
    }
  }

  public void visit( NameTy type, int level ){
    indent( level );
    if(type.typeVal == 0){
      System.out.println( "NameTy: INT" );
    }
    else if(type.typeVal == 1){
      System.out.println( "NameTy: VOID" );
    }
    else if(type.typeVal == 2){
      System.out.println( "NameTy: NULL" );
    }
    else if(type.typeVal == 3){
      System.out.println( "NameTy: BOOL" );
    }
  }

  public void visit( NilExp exp, int level ){
    indent( level );
    System.out.println( "NilExp: NULL" );

  }

  public void visit( ReturnExp exp, int level ){
    indent( level );
    System.out.println( "ReturnExp:" );

    if(exp.exp!=null){//if we have an expression to go with the return, envoke that expression visit function
      level++; //go down a level further
      exp.exp.accept(this,level);
    }

  }

  public void visit( SimpleDec dec, int level ){
    indent( level );
    System.out.println( "SimpleDec:" );
    //go down a level and print the type along with the name
    level++;
    
    indent( level );
    if(dec.typ.typeVal == 0){
      System.out.println( "NameTy: INT" );
    }
    else if(dec.typ.typeVal == 1){
      System.out.println( "NameTy: VOID" );
    }
    else if(dec.typ.typeVal == 2){
      System.out.println( "NameTy: NULL" );
    }
    else if(dec.typ.typeVal == 3){
      System.out.println( "NameTy: BOOL" );
    }

    indent( level );
    System.out.println( "Name: " + dec.name );

  }

  public void visit( SimpleVar var, int level ){
    indent( level );
    System.out.println( "SimpleVar: " + var.name );

  }

  public void visit( VarDecList varDecList, int level ){
    VarDecList tempList = varDecList;

    //loop through and print each node in the list, similar to ExpList
    while(varDecList!=null){
      varDecList.head.accept(this,level);
      varDecList = varDecList.tail;
    }

  }

  public void visit( IndexVar var, int level ){
    indent( level );
    System.out.println( "IndexVar: " + var.name );
    level++;
    var.index.accept(this,level);
  }

  public void visit( FunctionDec FunDec, int level ){
    indent( level );
    System.out.println( "FunctionDec:");

    //Print the return type
    level++;
    indent( level );
    if(FunDec.result.typeVal == 0){
      System.out.println( "NameTy: INT" );
    }
    else if(FunDec.result.typeVal == 1){
      System.out.println( "NameTy: VOID" );
    }
    else if(FunDec.result.typeVal == 2){
      System.out.println( "NameTy: NULL" );
    }
    else if(FunDec.result.typeVal == 3){
      System.out.println( "NameTy: BOOL" );
    }

    //print function name next
    indent( level );
    System.out.println( "func: " + FunDec.func );

    VarDecList tempList = FunDec.parameters;
    while(tempList!=null && tempList.tail!=null){
      tempList.head.accept(this,level);
      tempList = tempList.tail;
    }

    //if it has a body print that out too
    if( FunDec.body != null ) {
      FunDec.body.accept( this, level );
    }


  }

}

  