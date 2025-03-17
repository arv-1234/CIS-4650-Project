/* Goal: Traverse the abstract syntax tree in post-order and find/report semantic errors. */

import absyn.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

public class SemanticAnalyzer implements AbsynVisitor {
    /* -----------------------------  SYMBOL TABLE  ------------------------------------- */
    // Declare the variables
    public Stack<HashMap<String, ArrayList<NodeType>>> tableStack;//will change block by block
    final static int SPACES = 4;
    public int currentReturnType = -1;

    private void indent( int level ) {
        for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    }

    // Constructor: Initialize the variables
    public SemanticAnalyzer() {
    
        // Store the tables for each stack, creates a linked table relationship
        tableStack = new Stack<HashMap<String, ArrayList<NodeType>>>(); 
        // The first scope that the stack holds is the global scope
        tableStack.push( new HashMap<String, ArrayList<NodeType>>());

        //declare that we have entered the global scope
        System.out.println("Entering the global scope:");

    }



    /* -----------------------------  TYPE CHECKER  ------------------------------------- */
    /* 
      Given the current scopes table of declarations, use get to get the stored variable and verify it's got the same type as the passed type
      Since everything in the stack are the current scopes declarations and parent scopes we must check them all, however we use the most recently defined
      rule( from the lecture ) where we favor the closest scopes declaration. Assume a variable is being passed
      returns 1 upon success, -1 on type mismatch, 0 on not found.
     */
    public int typeChecker(int level, String varName, int type, int row, int col) {
       

        HashMap<String, ArrayList<NodeType>> tempTable;
        ArrayList<NodeType> tempList;
        Dec tempDec;

        //loop through all from top(current) to bottom(global) 
        for (int i = 0; i < tableStack.size(); i++) {

            tempTable = tableStack.get(i);
            tempList = tempTable.get(varName);

            if(tempList!=null){//found an instance, check if one of the nodes type matches the given type

                for( NodeType tempNode: tempList){
                    tempDec = tempNode.def;
                    if(tempDec.getType() == type){//the types match, return 1 for success
                       return 1;
                    }
                    else{
                        return -1; //type do not match, return -1 to indicate type mismatch
                    }

                }

            }

        }

        //no matches found, wasn't declared, return 0
        return 0;
    }

        
    //Function that checks if a varriable was defined in the current scope or above. used to determine if a VARIABLE was declared before
    //when we find a declaration, we can stop and return 1 for a success, 0 for failure
    public int wasDefined( String name ){

        ArrayList<NodeType> tempList;
        //go through all scopes as now we can reference declarations from higher scopes
        for (int i = 0; i < tableStack.size(); i++) {
           tempList = tableStack.get(i).get(name);
           if(tempList!=null){//non null, found a declaration, return 1
            return 1;
           }
        }

        return 0;
    }
    
    //loop through node list and find if a body was previously defined
    public int funcWasDefined( ArrayList<NodeType> storedNodes, NodeType curNode, int row, int col ){

        //nothing was found 
        if(storedNodes==null){
            return 0;
        }

        FunctionDec tempFunc;
        for( NodeType tempNode: storedNodes){

            tempFunc = (FunctionDec)tempNode.def;

            //if the body is not a NilExp, means it was defined, return 1 as a "found"
            if(tempFunc.body.isNilExp()!=1){
                return 1;
            }

        }

        //nothing found
        return 0;
    }


    /*Visit functions for traversing Tree */

    //loop through expressions and 'visit' each one
    public void visit( ExpList exp, int level ){
        while( exp != null ) {
            exp.head.accept( this, level );
            exp = exp.tail;
          } 
    }

    //check if the right hand sides type matches the left hand side
    public void visit( AssignExp exp, int level ){

        //current scopes table, peek to get the most recent scope(current one)
        HashMap<String, ArrayList<NodeType>> curTable = tableStack.peek();

        SimpleVar leftSide = (SimpleVar)exp.lhs.variable; //should always be simple var as left hand will be a variable
        Exp rightSide = exp.rhs;

        /*  Through re defining type checker we do not need this portion, kept it just in case tho
        String rightType = "";

        
        //check its type
        switch (rightSide.getType()) {
            case 0 -> rightType = "int";
            case 1 -> rightType = "boolean";
            default -> System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) +"Incorrect type given for assign exps right hand side");
        }
        */
            
        //check if the types for the right side and left side match. 1 for match, -1 for mismatch, 0 for lhs wasn't declared
        int res = typeChecker(level, leftSide.name , rightSide.getType(), exp.row, exp.col);

        //only print an error check 
        if(res==-1){
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: In Assignment operator left and right hand side types differ\n");
        }
        else if(res == 0){
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Assign operators left hand side variable was never declared\n");
        }
    
    
    }

    public void visit( IfExp exp, int level ){
  
        //conditional statement
        exp.test.accept( this, level );
        //this is going to be a compound stmt, let the respective visit function handle it
        exp.thenpart.accept( this, level );
        if (exp.elsepart != null ){
            exp.elsepart.accept( this, level );
        }
    }

    //I don't think ever need to call this, it wouldn't do much for semantic analyzer, leave as a placeholder or else errors will arise 
    public void visit( IntExp exp, int level ){

    }

    //check if its a logic or math operator and type check accordingly
    public void visit( OpExp exp, int level ){
       

        //Numerical operators
        if( exp.op == OpExp.PLUS || exp.op == OpExp.MINUS || exp.op == OpExp.TIMES || exp.op == OpExp.OVER){
            //only type check if both exist
            if (exp.left != null && exp.right != null){
                //both must be of type int, if not throw an error
                if(exp.left.getType()!=0){
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: incorrect type for lefthand, not of type INT\n");
                }
                if(exp.right.getType()!=0){
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: incorrect type for lefthand, not of type INT\n");

                }
            }
            else{//They must both exist for all of these operators, so if even one doesn't exist throw and error
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Syntax Error: Missing right/left values in mathmatical operator\n");
            }
        }
        else if (exp.op == OpExp.EQ || exp.op == OpExp.LT || exp.op == OpExp.GT || exp.op == OpExp.UMINUS || exp.op == OpExp.AND || exp.op == OpExp.OR || exp.op == OpExp.APPROX){
            //only type check if both exist
            if (exp.left != null && exp.right != null){
                //both must be of type bool, if not throw an error
                if(exp.left.getType()!=3){
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: incorrect type for lefthand, not of type Boolean\n");
                }
                if(exp.right.getType()!=3){
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: incorrect type for lefthand, not of type Boolean\n");

                }
            }
            else{//They must both exist for all of these operators, so if even one doesn't exist throw and error
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Syntax Error: Missing right/left values in mathmatical operator\n");
            }
        }
        else{
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Syntax Error: Unrecognized operator\n");
           
        }

    }

    //again for this let the compoundExp accept handle semantic analysis
    public void visit( WhileExp exp, int level ){

        //if test expression exists, preform semantic analysis
        if(exp.test!=null){
            exp.test.accept(this,level);
        }

        exp.body.accept(this, level);

    }
    

    public void visit( DecList decList, int level ){
        //print out the decs stored
        DecList tempDecList = decList;
  
        if(tempDecList.head != null){ //make sure it's not empty
            while( tempDecList != null ) {
                tempDecList.head.accept( this, level );
                tempDecList   = tempDecList.tail;
            }
        }
    }

    //call insert here,type check as well with previous instances
    public void visit( ArrayDec arrDec, int level ){

        String tempType = "";
        //get the current type
        if(arrDec.typ.typeVal == 0){
            tempType = "int";
        }
        else if(arrDec.typ.typeVal == 1){
            tempType = "void";
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + "Syntax Error: Invalid array type detected: void\n");
            return;
        }
        else if(arrDec.typ.typeVal == 2){
            tempType = "null";
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + "Syntax Error: Invalid array type detected: null\n");
            return;
        }
        else if(arrDec.typ.typeVal == 3){
            tempType = "bool";
        }
        else{
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + "Syntax Error: Invalid type detected\n");
            return;
        }


        indent(level);
        //print out declaration for symbol table
        System.out.println(arrDec.name + ": " + tempType);

        //get the list of declarations in the current scope from the table
        ArrayList<NodeType> tempList = tableStack.peek().get(arrDec.name);

        //create a new node for the varriable, keep track of the level, varriable name, type and scope
        NodeType tempNode = new NodeType(level, arrDec.name, arrDec);
        ArrayList<NodeType> tempNodeList = new  ArrayList<NodeType>();
        tempNodeList.add(tempNode);

        if(tempList == null){//no previous instances of anything, insert into the current scopes symbol table
            tableStack.peek().put( arrDec.name,tempNodeList);
        }
        else{//declaration already exists in the scope, regardless of anything throw an error
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + "Semantic Error: Another varriable of the same name was already declared\n");
            
        }

    }

    public void visit( BoolExp exp , int level ){

    }

    //verify the arguments match previous declarations
    public void visit( CallExp exp, int level ){

        //check if the function exists, check the global scope as they can only be defined there, lastElement is the initially pushed table
        HashMap<String, ArrayList<NodeType>> tempTable = tableStack.lastElement();
        FunctionDec prevDef = null;
        ArrayList<NodeType> tempArr = tempTable.get(exp.fun);
        //check for instances of the called functions declaration
        if(tempArr!=null){
            for( NodeType tempNode: tempArr){
                if(tempNode.variableName.equals(exp.fun)){//previous definition was found! have prevDef point to it, check if its a prototype or definition
                    prevDef = (FunctionDec) tempNode.def; //typecast to FunDe type
                    
                    //if it is not a prototype, we have found a definition and we can break, there can only be 1
                    if(prevDef.body.isNilExp() == -1){
                        break;
                    }
                    else{
                        prevDef = null; //revert it back to null to show we havent found anything
                    }

                }
            }

            //never found a declaration, throw error and return(no body to analyze)
            if (prevDef == null) {
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function was never defined\n");
                return;
            }
            

        }
        else{//no declarations initially, no function definition, throw error and return
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function was never defined\n");
            return;
        }

        //found the definition, compare args and defined parameters
        //check if called arguments match defined parameters by looping through the arguments and parameters for the found declaration
        ExpList tempArgList = exp.args;
        VarDecList tempParamList = ((FunctionDec)prevDef).parameters;
        int tempArgType = -1;
        int tempParamType = -1;

        //if both null, most likely was defined as void so consider it valid
        if(tempArgList.head==null && tempParamList.head==null){
            return;
        }
        else if(tempArgList.head==null && tempParamList.head!=null){ //passed a void/empty but was expecting something else
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function does not expect void arguments");
            return;   
        }
        else if(tempArgList.head!=null && tempParamList.head==null){ //expected void but got actual arguments
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function expected void arguments but something else was passed");
            return; 
        }
        else{
            while(tempArgList!=null && tempParamList!=null){//go through defined parameters and passed arguments

                Dec tempVar = (Dec)tempParamList.head; 
                Exp tempExp = (Exp)tempArgList.head;

                //check if the types match
                tempArgType = tempExp.getType(); // 0 for int, 3 for bool, -1 for invalid
                tempParamType = tempVar.getType(); //0 for int, 3 for bool, 1 for void, 2 for null


                if(tempArgType!=tempParamType){
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function call contains invalid types");
                    System.err.println(tempVar.getName() +" expected a different type\n");
                }

                //move to the next parameter/argument
                tempArgList = tempArgList.tail;
                tempParamList = tempParamList.tail;
            }
        }
    }

    public void visit( CompoundExp exp, int level ){ //only time scope level changes

        //new scope, go down a level and create a new symbol table for the new scope
        tableStack.push(new HashMap<String, ArrayList<NodeType>>());
        level++;//scope level has changed, go deeper

        //print for the symbol table
        indent(level);
        System.out.println("Entering a new block:");

        //*****Deal with the declarations/expressions******

        VarDecList tempVarDecList = exp.decs;
        while( tempVarDecList  != null ) {
            tempVarDecList.head.accept( this, level );
            tempVarDecList  = tempVarDecList.tail;
        }

        ExpList tempList = exp.exps;
        while( tempList != null ) {
            tempList.head.accept( this, level );
            tempList = tempList.tail;
        }

        //finished with the current scope, pop it from the stack and move back up to the previous scope and level
        tableStack.pop();
        level--;

    }

    //Call insert here, check for any previous declarations
    public void visit( FunctionDec FunDec, int level ){

    
        //check if we are in the global scope, global scope if only one table is in the stack
        if(tableStack.size()!=1){
            System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function not defined in the global scope\n");
            return;
        }

        //check if any functions of the same name already exists, unlike variables we cannot have more than one function of the same name, so if one is found throw error 
        ArrayList<NodeType> tempArr;
        NodeType tempNode = new NodeType(level, FunDec.func, FunDec);
        tempArr = tableStack.peek().get(FunDec.func); //Resulting array of nodes for when 


        //we are either dealing with a function prototype or function defining declaration, must treat each differently

        //Function has a body, dealing with body defining declaration
        if(FunDec.body.isNilExp()!=1){

            //if it has a body, must have a return type, swap our current global tracking variable to track the return type
            int prevReturnType = currentReturnType;
            currentReturnType = FunDec.result.typeVal;

            //also if it has a body we must declare a scope change with function
            indent(level+1);
            System.out.print("New Scope for function " + FunDec.func + " "); //print instead of println so it connects to the print in compoundExp


            //no previous function declarations match so we can insert
            if(funcWasDefined(tempArr, tempNode, FunDec.row, FunDec.col) == 0){
                //if the previous checks are false, valid function declaration, store the function into the table
                ArrayList<NodeType> newArr = new ArrayList<NodeType>();
                newArr.add(tempNode); 
                tableStack.peek().put(FunDec.func, newArr);
            }
            else{
                //function was already defined else where, throw an error and continue
                System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function was already previously defined\n");
            
            }
            if(FunDec.body!=null){
                //handle the body/new scope
                FunDec.body.accept(this, level);
            }

            //after dealing with the body we leave so revert the return type
            currentReturnType = prevReturnType;
        }
        else{//dealing with a prototype declaration, if anything else was previously defined with a same name throw an error

            //no such declaration, add it to the current table
            if(tableStack.peek().get(FunDec.func)==null){
                ArrayList<NodeType> newArr = new ArrayList<NodeType>();
                newArr.add(tempNode); 
                tableStack.peek().put(FunDec.func, newArr);
            }
            else{//already exists so we cannot have a prototype
                System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function Prototype was already previously defined\n");     
            }

        }
    }

    //check if the var's index is a int, can be done because index is of type exp
    public void visit( IndexVar var, int level ){

        //we simply need to check if the index is of type int
        if( var.index.getType() !=0){
            System.err.println("Error in line " + (var.row + 1) + ", column " + (var.col + 1) + "Syntax Error: Index is not of type int\n");
        }
    }

    public void visit( NameTy type, int level ) {

    }

    public void visit( NilExp exp, int level ) {

    }

    //matches the functions return type?
    public void visit( ReturnExp exp, int level ) {
        //check if current scopes return type matches the return statements type
        if(currentReturnType!=exp.getType()){
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Invalid return type\n");
        }
    }


    public void visit( VarExp exp, int level ){
        exp.variable.accept(this, level);
    }

    //check if it was previously declared or not. if it was throw an error, if it wasn't insert into the current scopes stack
    //ONLY CHECK THE CURRENT SCOPE 
    public void visit( SimpleDec dec, int level ) {

        indent(level);
        String dataType = "";

        //get its type in string form, maybe defn a function for this if we have time
        if(dec.getType()==0){
            dataType = "int";
        }
        else if(dec.getType()==3){
            dataType = "boolean";
        }
        else{
            dataType = "invalid";
        }

        //print declared varriable details like the symbol table
        System.out.println(dec.name + ": " + dataType );

        //get the current scopes table and find an instance of the declared variables name in the current scope
        HashMap<String, ArrayList<NodeType>> tempTable = tableStack.peek();
        ArrayList<NodeType> tempList = tempTable.get(dec.name);

        if(tempList==null){//does not exist, create node and insert
            tempList = new ArrayList<NodeType>();
            tempList.add(new NodeType(level, dec.name, dec));
            tempTable.put(dec.name, tempList);//update the current scopes symbol table
        }
        else{//already declared, throw error and move on
            System.err.println("Error in line " + (dec.row + 1) + ", column " + (dec.col + 1) + "Semantic Error: Already previously declared\n");
        }

    }

    //found an instance of a varriable, check to see if it was declared previously
    public void visit( SimpleVar var, int level ) {

        if(wasDefined(var.name)!=1){
            System.err.println("Error in line " + (var.row + 1) + ", column " + (var.col + 1) + "Semantic Error: Varriable was not declared\n");
        }
    }

    public void visit( VarDecList varDecList, int level ) {
        while(varDecList != null) {
            varDecList.head.accept(this, level);
            varDecList = varDecList.tail;
        }
    }

}