/* Goal: Traverse the abstract syntax tree in post-order and find/report semantic errors. */

import absyn.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

public class SemanticAnalyzer implements AbsynVisitor {
    /* -----------------------------  SYMBOL TABLE  ------------------------------------- */
    // Declare the variables
    public HashMap<String, ArrayList<NodeType>> table;
    public Stack<String> scopeStack;

    // Constructor: Initialize the variables
    public SemanticAnalyzer() {
        // Store the nodes with associated string keys
        table = new HashMap<String, ArrayList<NodeType>>();
        // Store the scopes in a stack, pop when exiting a scope (end of compound_stmt visit), push when entering a scope
        scopeStack = new Stack<String>(); 
        // The first scope that the stack holds is the global scope
        scopeStack.push("global");
    }

    // Add a Symbol to the Symbol Table
    public void insert(String key, NodeType node) {
        // Declare the variable
        ArrayList<NodeType> nodeList;

        // Check if the key exists in the symbol table
        if (table.containsKey(key)) {
            // True: Get the existing node list from the key and add the new node to it
            nodeList = table.get(key);
            nodeList.add(node);
        } else {
            // False: Create a new node list, add the new node, and insert it into the hashmap using the key
            nodeList = new ArrayList<NodeType>();
            nodeList.add(node);
            table.put(key, nodeList);
        }
    }

    // Look up if a Symbol exists in the Symbol Table
    // Return: HashMap's node list using the key
    public ArrayList<NodeType> lookup(String key) {
        // We look through the symbol table to see if there is a matching key that exists
        if (table.containsKey(key)) {
            // True: return the array list of nodes
            return table.get(key);
        } else {
            // False: return null to indicate that it doesn't exist
            System.err.println("Error: cannot lookup and return the undefined variable key '"+ key +"'.");
            return null;
        }
    }

    // Delete a Symbol that exists in the Symbol Table
    public void delete(String key) {
        if (table.containsKey(key)) {
            table.remove(key);
        } else {
            System.err.println("Error: cannot delete the undefined variable key '"+ key +"'.");
        }
    }

    /* -----------------------------  TYPE CHECKER  ------------------------------------- */
    /* 
        Takes in a array list of nodes yielded from a query from the symbol table and a node representing the variable we have just read in
        Preform the necessary type check against all instances. This is done by first checking if they are within the same scope, if not no type mismatch
        and we can proceeed, next check if it is on a level that is the same or lower if not proceed. If they are within the same scope and same level check if the
        current node has a matching type with the previously defined instance then we are safe, if not print out a error
     */
    public int typeChecker(ArrayList<NodeType> storedNodes, int level, String scope, String type, int row, int col) {
       
        //loop through each node
        for( NodeType tempNode: storedNodes){
            //check if same scope and at a level that is lower or the same as the previous definition and check if their types do not match
            if( tempNode.scope == scope && level <= tempNode.level && type!=tempNode.type){
            
                System.err.println("Error in line " + (row + 1) + ", column " + (col + 1) + "Semantic Error: Variable " + tempNode.variableName + " was previously declared as a " + tempNode.dataType + " but was treated as : " + type);
                return -1;
            }
        }
        return 0;
    }

    //Function that checks if a varriable was already defined within the same scope, if it is report it, similar to type checker. -1 on error, 0 on nothing found
    public int wasDefined( ArrayList<NodeType> storedNodes, NodeType curNode, int row, int col ){

        if(storedNodes == null){
            return -1;
        }

        //loop through each node
        for( NodeType tempNode: storedNodes){
            //check if same name and at a level that is lower or the same as the previous definition, since its a variable type does not matter, only care if it was defined before
            if( tempNode.variableName == curNode.variableName && curNode.level >= tempNode.level){
                
                System.err.println("Error in line " + (row + 1) + ", column " + (col + 1) + "Semantic Error: " + curNode.variableName + " was already declared previously");
                return -1;
            }
        }
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

        Var leftSide = exp.lhs.variable; // Was varExp
        Exp rightSide = exp.rhs;

        String rightType = "";

        //check if int
        if(rightSide.getType() == 0){
            rightType = "int";
        }//check if boolean
        else if(rightSide.getType() == 1){
            rightType = "boolean";
        }
        else{
            System.out.println("how???");
        }

        //get the type of the left hand side and compare their types
        ArrayList<NodeType> leftNodeList = table.get(scopeStack.peek()); // Was leftSide.name

        //check if the types for the right side and left side match
        if(leftNodeList!=null){
            typeChecker(leftNodeList, level, scopeStack.peek(), rightType, exp.row, exp.col);
        }
        else{ // varriable doesn't exist, call an error for an undeclared variable
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Undeclared varriable: " + leftSide.toString());
               
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
                if(exp.left.getType()!=1){
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: incorrect type for lefthand, not of type Boolean\n");
                }
                if(exp.right.getType()!=1){
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

        //get the list of declarations in the current scope from the table
        ArrayList<NodeType> tempList = table.get(scopeStack.peek()); 
        //create a new node for the varriable, keep track of the level, varriable name, type and scope
        NodeType tempNode = new NodeType(level, arrDec.name, arrDec);

        int result = -1;

        if(tempList == null){//no previous instances of anything, insert without a check
            insert(scopeStack.peek(), tempNode);
            return;
        }
        else{//declarations already exist, check if it was previously defined in the same scope
            result = wasDefined(tempList,tempNode, arrDec.row, arrDec.col);
        }

        //if we find there are no conflicting previous declarations, insert it into the current scopes list of nodes
        if(result == 0){
            insert(scopeStack.peek(), tempNode);
        }

    }

    public void visit( BoolExp exp , int level ){

    }

    //verify the arguments match previous declarations
    public void visit( CallExp exp, int level ){

        //check if the function exists, check the global scope as they can only be defined there
        ArrayList<NodeType> tempArr = table.get("global");
        FunctionDec prevDef = null; // Was Fundec

        //check for instances of the called functions declaration
        if(tempArr!=null){
            for( NodeType tempNode: tempArr){
                if(tempNode.variableName == exp.fun){//previous definition was found! have prevDef point to it and break the loop
                    prevDef = (FunctionDec) tempNode.def; //typecast to FunDe type
                    break;
                }
            }

            /* Uncomment code once we get confirmation this is suppose to checj prevDef
            if (prevDef == null) {
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function was never defined\n");
                return;
            }
            */

            //after checking all global declarations no matching function declaration was found, print error and skip over everything else
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function was never defined\n");
            return;

        }
        else{//no declarations initially, no function definition, throw error and return
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function was never defined\n");
            return;
        }

        //check if called arguments match defined parameters by looping through the arguments and parameters for the found declaration
        ExpList tempArgList = exp.args;
        VarDecList tempParamList = prevDef.parameters;
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
            while(tempArgList.head!=null && tempParamList.head!=null){

                SimpleDec randomVar = (SimpleDec)tempParamList.head; // Change variable name


                //check if the types match
                tempArgType = tempArgList.head.getType(); // 0 for int, 3 for bool, -1 for invalid
                tempParamType = randomVar.getType(); //0 for int, 3 for bool, 1 for void, 2 for null


                if(tempArgType!=tempParamType){
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function call contains invalid types");
                    System.err.println(randomVar.name +" expected a different type\n");
                }

                //move to the next parameter/argument
                tempArgList = tempArgList.tail;
                tempParamList = tempParamList.tail;
            }
        }



    }

    public void visit( CompoundExp exp, int level ){ //only time scope level changes

        level++;//scope level has changed, go deeper

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
    }

    
    //Call insert here, check for any previous declarations
    public void visit( FunctionDec FunDec, int level ){

        //check if we are in the global scope, if not throw an error and do not insert as functions can only be defined in global
        if(scopeStack.peek()!="global"){
            System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function not defined in the global scope\n");
            return;
        }

        //check if any functions of the same name already exists, unlike variables we cannot have more than one function of the same name, so if one is found throw error 
        ArrayList<NodeType> tempArr = table.get("global");
        NodeType tempNode = new NodeType(level, FunDec.func, FunDec);


        //no previous function declarations match so we can insert
        if(wasDefined(tempArr, tempNode, FunDec.row, FunDec.col) == 0){
            //if the previous checks are false, valid function declaration, store the function into the table
            insert("global", tempNode);
        }
        else{
            //function was already defined else where, do not analyze further and return
            return;
        }

        //new overall scope, push function name to stack, level change handled in the compoundExp
        scopeStack.push(FunDec.func);

        if(FunDec.body!=null){
            //handle the compound expression
            FunDec.body.accept(this, level);
        }
        scopeStack.pop();//remove the pushed function name to revert to the previous scope

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

    }

    public void visit( VarExp exp, int level ){
        exp.variable.accept(this, level);
    }

    //call insert here
    public void visit( SimpleDec dec, int level ) {

    }

    public void visit( SimpleVar var, int level ) {

    }

    public void visit( VarDecList varDecList, int level ) {
        while(varDecList != null) {
            varDecList.head.accept(this, level);
            varDecList = varDecList.tail;
        }
    }

}