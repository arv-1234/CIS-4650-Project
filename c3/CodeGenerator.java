import absyn.AbsynVisitor;
import absyn.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator implements AbsynVisitor{

    /*Offsets */
    public int mainEntry = -1; //updated with mains location if a main function is ever defined
    public int emitLoc = 0;
    public int highEmitLoc = 0;
    public int globalOffset = 0;
    public int returnOffset = -1;
    public int initialOffset = -2;

    /*Special Registers */
    public static final int PC = 7;
    public static final int GP = 6;
    public static final int FP = 5;
    public static final int AC = 0;
    public static final int AC1 = 1;

    /*Emit Functions given from the lectures*/
    void emitRO(String op, int r, int s, int t, String c) {
        System.out.printf("%3d: %5s %d, %d, %d \t%s\n", emitLoc, op, r, s, t, c);
        
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    void emitRM(String op, int r, int d, int s, String c) {
        System.out.printf("%3d: %5s %d, %d(%d) \t%s\n", emitLoc, op, r, d, s, c);
        
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    public void emitRM_Abs(String op, int r, int a, String c) {
        System.out.printf("%3d: %5s %d, %d(%d) \t%s\n", emitLoc, op, r, a - (emitLoc + 1), PC, c);

        emitLoc++;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    int emitSkip(int distance) {
        int i = emitLoc;
        emitLoc += distance;
    
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    
        return i;
    }

    public void emitComment(String c) {
        System.out.println("* " + c);
    }
    
    void emitBackup(int loc) {
        if (loc > highEmitLoc) {
            emitComment("BUG in emitBackup");
        }
        emitLoc = loc;
    }
    
    void emitRestore() {
        emitLoc = highEmitLoc;
    }
    
    
    
    public void visit ( Absyn trees ) {

        //prelude
        emitComment("Standard Prelude:");
        emitRM("LD", GP , 0,  AC , "Load gp with maxaddress");
        emitRM("LDA", FP , 0,  GP , "Copy gp to fp");
        emitRM("ST", AC , 0,  AC , "Clear value at location 0");

        //inputs
        emitComment("Jump around i/o routines here");
        emitComment("code for input routine");
        emitRM("ST", AC , returnOffset,  FP , "store return");
        emitRO("IN", 0, 0, 0, "input");
        emitRM("LD", PC , returnOffset,  FP , "return to caller");

        //outputs
        emitComment("code for output routine");
        emitRM("ST", AC , returnOffset,  FP , "store return");
        emitRM("LD", AC , initialOffset,  FP , "Load output value");
        emitRO("OUT", 0, 0, 0, "output");
        emitRM("LD", PC, returnOffset, FP, "return to caller");
   

        //Start generating the code based off the tree
        trees.accept( this, 0, false );

        //generate finale
        emitRM( "ST", FP, -1, FP, "push ofp" );
        emitRM( "LDA", FP, -1, FP, "push frame" );
        emitRM( "LDA", AC, 1, PC, "load ac with ret ptr" );
        emitRM_Abs( "LDA", PC, mainEntry, "jump to main loc" );
        emitRM( "LD", FP, 0, FP, "pop frame" );
        emitComment("End of execution.");
        emitRO( "HALT", 0, 0, 0, "" );


    }

    //visit functions to red define again.... :| 

    //here we call the declarations with true flag as dec list only occours in global scope according to grammar in cup files
    public void visit( DecList decList, int offset, boolean flag ){

        //loop through all declarations and pass true for isGlobal
        while ( decList != null && decList.head != null) {
            // VarDecList or Function Dec
            decList.head.accept( this, offset, true );
            offset--;
            decList = decList.tail;
        }

    }

    public void visit( ExpList exp, int offset, boolean flag );

    public void visit( AssignExp exp, int offset, boolean flag );
  
    public void visit( IfExp exp, int offset, boolean flag );
  
    public void visit( IntExp exp, int offset, boolean flag );
  
    public void visit( OpExp exp, int offset, boolean flag );
  
    public void visit( WhileExp exp, int offset, boolean flag );
  
    public void visit( VarExp exp, int offset, boolean flag );
  
    public void visit( ArrayDec arrDec, int offset, boolean flag );
  
    public void visit( BoolExp exp , int offset, boolean flag );
  
    public void visit( CallExp exp, int offset, boolean flag );
  
    public void visit( CompoundExp exp, int offset, boolean flag );
  
    public void visit( FunctionDec FunDec, int offset, boolean flag );
  
    public void visit( IndexVar var, int offset, boolean flag );
  
    public void visit( NameTy type, int offset, boolean flag );
  
    public void visit( NilExp exp, int offset, boolean flag );
  
    public void visit( ReturnExp exp, int offset, boolean flag );
  
    public void visit( SimpleDec dec, int offset, boolean flag );
  
    public void visit( SimpleVar var, int offset, boolean flag );
  
    public void visit( VarDecList varDecList, int offset, boolean flag );





}
