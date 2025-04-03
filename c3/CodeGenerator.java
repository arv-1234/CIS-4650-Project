import absyn.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator implements AbsynVisitor{

    //add to it whenever we are making declarations, holds the offset value for the respective declaration
    public HashMap<String, Integer> framePtr = new HashMap<String, Integer>();
    //stores the name of a function and its corresponding address in memory to start the execution of instructions
    public HashMap<String, Integer> funcAddrMap = new HashMap<String, Integer>();

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
    public void visit( DecList decList, int offset, boolean isAddr ){

        //loop through all declarations and pass true for isGlobal
        while ( decList != null && decList.head != null) {
            // VarDecList or Function Dec
            decList.head.accept( this, offset, true );
            offset--;//decrease offset to 'move' to next memory location
            decList = decList.tail;
        }

    }

    //similar to declist
    public void visit( ExpList exp, int offset, boolean isAddr ){
        while(exp != null && exp.head!=null) {
            exp.head.accept(this, offset, false);
            offset--;
            exp = exp.tail;
        }
    }

    public void visit( AssignExp exp, int offset, boolean isAddr ) {
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
  
    public void visit( IfExp exp, int offset, boolean isAddr ) {
        emitComment("-> if");
        
        // if
        exp.test.accept(this, offset-1, false);

        int skipThen = emitSkip(1);

        // then (true = jump to the end, false = skip & jump to else)
        if (exp.thenpart != null) {
            emitComment("-> then");
            exp.thenpart.accept(this, offset-2, false);
            emitComment("<- then");
        }
        
        // if then == false, we skip to here
        emitBackup(skipThen);
        emitRM_Abs("JEQ", AC, emitLoc, "jmp to else, false case");
        emitRestore();
        int skipElse = emitSkip(1);

        // else (true/false = jump to the end)
        if (exp.elsepart != null) {
            emitComment("-> else");
            exp.elsepart.accept(this, offset-2, false);
            emitComment("<- else");
        }

        // if else == false, we skip to here
        emitBackup(skipElse);
        emitRM_Abs("LDA", PC, emitLoc, "jmp to end");
        emitRestore();  

        emitComment("<- if");
    }
  
    //this is basically a constant, simply load it into AC
    public void visit( IntExp exp, int offset, boolean isAddr ){

        emitComment("-> constant");
        emitRM("LDC", AC, exp.value, 0, "load const"); //from gcd.tm I think the 3rd argument is to be the actual value itself
        emitComment("<- constant");

    }
  
    public void visit( OpExp exp, int offset, boolean isAddr ){
        // Handle the opExp emits
        emitComment("-> op");

        // Handle the left and right sides
        exp.left.accept(this, offset-1, false);
        emitRM("ST", AC, offset, FP, "op: push left");

        //exp.left.accept(this, offset-2, false); // you previously wrote
        exp.right.accept(this, offset-2, false);
        emitRM("LD", AC1, offset, FP, "op: load left");

        if (exp.op <= 3) {
            // Handle Arithmetic
            emitRO(exp.getOpName(), AC, AC1, AC, "op " + exp.getOpSymbol());
        } else {
            // Handle Boolean
            // Might have to update this to handle APPROX, AND, OR, NOT, and UMINUS (10-14)
            emitRO("SUB", AC, 1, AC, "op " + exp.getOpSymbol());
            emitRM("J" + exp.getOpName(), AC, 2, PC, "br if true");
            emitRM("LDC", AC, 0, AC, "false case");
            emitRM("LDA", PC, 1, PC, "unconditional jmp");
            emitRM("LDC", AC, 1, AC, "true case");
        }

        emitComment("<- op");
    }
  
    public void visit(WhileExp exp, int offset, boolean isAddr) {
        emitComment("-> while");

        int startCondition = emitLoc; // The start address of condition check

        exp.test.accept(this, offset, false); // evaluate the condition

        emitComment("Result of condition stored in AC");

        // Dont know the exit address yet 
        // Will reserve space for the exit instruction for later
        int locExitLoop = emitSkip(1); // Save location for loop exit

        emitComment("JEQ to exit loop if the condition fails");

        // Help generate the body of the loop
        int loopBodyStart = emitLoc;
        exp.body.accept(this, offset, false);
        emitComment("The body of the loop ends at " + emitLoc);

        // Jump backwards to check the condition again
        // After the body, we need to jump back to re-evaluate the condition
        emitRM_Abs("LDA", PC, startCondition, "Jump back to conditon check at " + startCondition);

        // Backpatch
        // Now that we know when the loop wends we can add the JEQ instruction that was mentioned earlier
        int endOfLoop = emitLoc; // Address after the loop 
        emitBackup(locExitLoop); // Return to JEQ instruction
        emitRM("JEQ", AC, endOfLoop - locExitLoop, PC, "Loop exits if the condition is false");
        emitRestore();

        emitComment("<- while");

    }
  
    public void visit( VarExp exp, int offset, boolean isAddr ){
        
    }
  
    public void visit( ArrayDec arrDec, int offset, boolean isAddr ){
        if(isAddr) {
            emitComment("allocating global var: " + arrDec.name);
        } else {
            emitComment("processing local var: " + arrDec.name);
            framePtr.put(arrDec.name, offset);
        }
        emitComment("<- vardecl");
    }
    
  
    public void visit( BoolExp exp , int offset, boolean isAddr ){
        emitComment("-> boolean");

        int valueBool;

        // Determine the boolean value 
        if(exp.value)
        {
            valueBool = 1;
        }
        else
        {
            valueBool = 0;
        }
        
        // Load constant
        emitRM("LDC", AC, valueBool, 0, "load " + exp.value.toString());

        emitComment("<- boolean");
    }
  
    //handles the function call
    public void visit( CallExp exp, int offset, boolean isAddr ){

        emitComment("-> call of function: " + exp.fun);

        int functionLoc = -emitLoc;
        if (exp.args != null){ //if arguments were actually passed, utilize the accept function to let it handle it
            functionLoc++;
            exp.args.accept( this, offset, false );
            if (exp.args instanceof ExpList != true) {
                emitRM("ST", AC, offset + initialOffset, FP, "store arg val");
                offset--;
            }
        }

        //jump to the function and run the functions code, restore FP once we finish processising
        emitRM("ST", FP, offset, FP, "push ofp");
        emitRM("LDA", FP, offset, FP, "push frame");
        emitRM("LDA", AC, 1, PC, "load ac with ret ptr");
        emitRM("LDA", PC, functionLoc, FP, "jump to fun loc");
        emitRM("LD", FP, 0, FP, "pop frame");

        emitComment("<- call");

    }
  
     public void visit( CompoundExp exp, int offset, boolean isAddr ){
        emitComment("-> compound statement");

        visit(exp.decs, offset, false);

        visit(exp.exps, offset, false);

        emitComment("<- compound statement");
    }
  
    public void visit( FunctionDec FunDec, int offset, boolean isAddr ){

        int functionStartAddress = -emitLoc;//need to do this as emitLoc will constantly change, cannot just pass it on its own

        //necessary comments similar to the given tm file
        emitComment("processing function: " + FunDec.func);
        emitComment("jump around function body here");

        //if in main set the mainEntry to the current emitloc
        if("main".equals(FunDec.func)){
            mainEntry = functionStartAddress;
        }
        int savedLoc = emitSkip(1);

        //retrieve the current address with emitLoc and store it into our hashmap of function addresses
        funcAddrMap.put(FunDec.func, functionStartAddress);

        offset = -2;//move down 2 for return and ofp and start at -2 since we are now in the scope of a function

        //loop through the parameters and store them as declarations within the function
        //move the offset for each declaration we find as to not overwrite others
        VarDecList params = FunDec.parameters;
        while(params != null) {
            if (params.head instanceof SimpleDec){//if it is an instance of a simple dec, store it as such
                framePtr.put(((SimpleDec)params.head).name, offset);
                offset--;
            }
            else if (params.head instanceof ArrayDec){//if it is an instance of arrayDec store it as such
                framePtr.put(((ArrayDec)params.head).name, offset);
                offset--;
            }
            params = params.tail;
        }

        //let the body handle itself
        FunDec.body.accept(this,offset,false);

        //handle the functions 'return'
        int savedLoc2 = emitSkip(0);
        emitRM("LD", PC, returnOffset, FP, "return to caller");
        emitBackup(savedLoc);
        emitRM("LDA", PC, savedLoc2 - savedLoc, PC, "jump around fn body");
        emitComment("<- fundecl");
        emitRestore();

    }
  
    public void visit( IndexVar var, int offset, boolean isAddr ){
        emitComment("-> subs"); // Comments present in the given tm file

        // Checks if array variable exists in the current frame
        if(framePtr.containsKey(var.name))
        {
            // get offset from frame pointer
            offset = framePtr.get(var.name);

            if(isAddr)
            {
                // Need address of array
                emitRM("LDA", AC, offset, FP, "load id address");
                emitRM("ST", AC, globalOffset, FP, "store array addr");
            }
            else
            {
                // Need value of array
                emitRM("LD", AC, offset, FP, "load id value");
                emitRM("ST", AC, globalOffset, FP, "store array addr");
            }
        }
        else
        { // Array is not in the current frame

            if(isAddr)
            {
                // Need address of global array
                offset = framePtr.get(var.name);

                emitRM("LDA", AC, offset, GP, "load id address");
                emitRM("ST", AC, globalOffset, FP, "store array addr");
            }
            else
            { // Need value of globalArray
                emitRM("LD", AC, offset, GP, "load id value");
                emitRM("ST", AC, globalOffset, FP, "store array addr");
            }
        }

        // Process the index expression
        var.index.accept(this, offset, false);
        emitComment("<- subs"); // Comments present in the given tm file
    }
  
    // Can leave definition empty 
    public void visit( NameTy type, int offset, boolean isAddr ){

    }

    // Can leave definition empty
    public void visit( NilExp exp, int offset, boolean isAddr ){

    }
  
    public void visit( ReturnExp exp, int offset, boolean isAddr )
    {
        emitComment("-> return");

        exp.exp.accept(this, offset, isAddr);

        emitRM("LD", PC, returnOffset, FP, "return to caller");

        emitComment("<- return");
    }
  
    //here we have to add to the hasmap since we are declaring a variable, no need to store an actual value as declarations do not have values in C--
    public void visit( SimpleDec dec, int offset, boolean isAddr ) {

        //if it is true, its global
        if (isAddr) {
            emitComment("processing global var: " + dec.name);
            emitComment("<- vardecl");
        }
        else{//local dec
            emitComment("processing local var: " + dec.name);
        }

        //add it to the hashmap with its offset for either global or local
        framePtr.put(dec.name, offset);

    }
  
    public void visit( SimpleVar var, int offset, boolean isAddr ){

        //follow the format from gcd.tm
        emitComment("->id");
        emitComment("Looking up id: " + var.name);
        int tmpOffset = 0;

        //if it is a local variable it will exist in the framePtr
        if(framePtr.containsKey(var.name)){
            tmpOffset = framePtr.get(var.name);

            //if isAddress is true load its address into AC from the lecture slides
            if (isAddr) {
                emitRM("LDA",AC, tmpOffset, FP, "Load id Address");
            }
            else{ //load its value
                emitRM("LD", AC, tmpOffset, FP, "load id value");
            }


        }
        else{//not in framePtr so must be global, use GP

            //if isAddress is true load its address into AC from the lecture slides
            if (isAddr) {
                emitRM("LDA", AC, offset, GP, "Load id Address");
            }
            else{ //load its value
                emitRM("LD", AC, offset, GP, "load id value");
            }

        }

        emitComment("<-id");
    }
  
    public void visit( VarDecList varDecList, int offset, boolean isAddr ){
        while(varDecList != null)
        {
            varDecList.head.accept(this, offset, false);
            offset = offset - 1;

            varDecList = varDecList.tail;
        }
    }





}
