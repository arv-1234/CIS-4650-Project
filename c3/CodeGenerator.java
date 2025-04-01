import absyn.AbsynVisitor;
import absyn.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator implements AbsynVisitor{

    //add to it whenever we are making declarations, holds the offset value for the respective declaration
    public HashMap<String, Integer> framePtr = new HashMap<String, Integer>();

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
        int savedLoc = emitSkip(1);//from slide 36

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

        //from slide 36, shown to need after outputs
        int savedLoc2 = emitSkip(0);
        emitBackup(savedLoc);
        emitRM_Abs("LDA", PC, savedLoc2, "jump around i/o code");
        emitRestore();
        emitComment("End of standard prelude.");
   

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

    //visit functions to redefine again.... :| 

    //here we call the declarations with true flag as dec list only occours in global scope according to grammar in cup files
    public void visit( DecList decList, int offset, boolean flag ){

        //loop through all declarations and pass true for isGlobal
        while ( decList != null && decList.head != null) {
            // VarDecList or Function Dec
            decList.head.accept( this, offset, true );
            offset--;//decrease offset to 'move' to next memory location
            decList = decList.tail;
        }

    }

    //similar to declist
    public void visit( ExpList exp, int offset, boolean flag ){
        while(exp != null && exp.head!=null) {
            exp.head.accept(this, offset, flag);
            offset--;
            exp = exp.tail;
        }
    }

    public void visit( AssignExp exp, int offset, boolean flag ){

        emitComment("-> op");

        //from here we copy the process as described from the lecture slides and from gcd.tm(The lecture slides were very confusing when they were explaining it)

        //call the visit functions for both the left and right hand sides and let them handle it
        exp.lhs.accept(this,offset-1,true);
        //from the lecture slides, after simplevar handles the LDA use ST
        emitRM("ST", AC, offset, FP, "op: push left"); //similar message in gcd.tm

        exp.rhs.accept(this,offset-2, false);

        //Got this process from the lec slides and gcd.tm for y=10
        emitRM("LD", AC1, offset, FP, "op: Load left");
        emitRM("ST", AC, 0, AC1, "Store AC1 contents into FP");


        emitComment("<- op");
    }
  
    public void visit( IfExp exp, int offset, boolean flag );
  
    //this is basically a constant, simply load it into AC
    public void visit( IntExp exp, int offset, boolean flag ){

        emitComment("-> constant");
        emitRM("LDC", AC, exp.value, 0, "load const"); //from gcd.tm I think the 3rd argument is to be the actual value itself
        emitComment("<- constant");

    }
  
    public void visit( OpExp exp, int offset, boolean flag ){
        // Handle the opExp emits
        emitComment("-> op");

        // Handle the left and right sides
        exp.left.accept(this, offset-1, false);
        emitRM("ST", AC, offset, FP, "op: push left");

        //exp.left.accept(this, offset-2, false);
        exp.right.accept(this, offset-1, false);
        emitRM("LD", AC1, offset, FP, "op: load left");

        if (exp.op <= 3) {
            // Handle Arithmetic
            emitRO(exp.getOpName(), AC, AC1, AC, "op " + exp.getOpSymbol());
        } else {
            // Handle Boolean
            emitRO("SUB", AC, 1, AC, "op " + exp.getOpSymbol());
            emitRM("J" + exp.getOpName(), AC, 2, PC, "br if true");
            emitRM("LDC", AC, 0, AC, "false case");
            emitRM("LDA", PC, 1, PC, "unconditional jmp");
            emitRM("LDC", AC, 0, AC, "true case");
        }

        emitComment("<- op");
    }
  
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
  
    public void visit( SimpleDec dec, int offset, boolean flag ) {
        emitComment("processing local var: " + dec.name);
    }
  
    public void visit( SimpleVar var, int offset, boolean flag ){

        //follow the format from gcd.tm
        emitComment("->id");
        emitComment("Looking up id: " + var.name);
        int tmpOffset = 0;

        //if it is a local variable it will exist in the framePtr
        if(framePtr.containsKey(var.name)){
            tmpOffset = framePtr.get(var.name);

            //if isAddress is true load its address into AC from the lecture slides
            if (flag) {
                emitRM("LDA",AC, tmpOffset, FP, "Load id Address");
            }
            else{ //load its value
                emitRM("LD", AC, tmpOffset, FP, "load id value");
            }


        }
        else{//not in framePtr so must be global, use GP

            //if isAddress is true load its address into AC from the lecture slides
            if (flag) {
                emitRM("LDA", AC, offset, GP, "Load id Address");
            }
            else{ //load its value
                emitRM("LD", AC, offset, GP, "load id value");
            }

        }

        emitComment("<-id");
    }
  
    public void visit( VarDecList varDecList, int offset, boolean flag );





}
