/* Goal: Traverse the abstract syntax tree in post-order and find/report semantic errors. */

import absyn.*;
import java.util.HashMap;
import java.util.ArrayList;

public class SemanticAnalyzer implements AbsynVisitor {
    /* -----------------------------  SYMBOL TABLE  ------------------------------------- */
    // Declare the variables
    HashMap<String, ArrayList<NodeType>> table;
    Stack <String> scopeStack;

    // Constructor: Initialize the variable
    public SemanticAnalyzer() {
        table = new HashMap<String, ArrayList<NodeType>>(); //store the nodes with asscociated string keys
        scopeStack = new Stack<String>(); //stores the scopes in a stack like manor, pop when exiting a scope(end of compound_stmt visit), push when entering a scope
        scopeStack.push("global"); //initially push global scope to the stack
    }

    // Add a Symbol to the Symbol Table
    public void insert(String key, NodeType node) {
        // Declare the variable
        ArrayList<NodeType> nodeList;

        // Check if the key exists in the symbol table
        if (table.containsKey(key)) {
            // True: If it exists, get the node list from the key and add to it
            nodeList = table.get(key);
            nodeList.add(node);
        } else {
            // False: If it doesn't exist, create a new node list, add the node, and insert it using the key
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
            return null;
        }
    }

    // Delete a Symbol that exists in the Symbol Table
    public void delete(String key) {
        if (table.containsKey(key)) {
            table.remove(key);
        }
    }

    /* -----------------------------  TYPE CHECKER  ------------------------------------- */
    /* Checks the variable's type from the symbol table vs. from the current assignment in code
       Mismatch Example: variable "x" is type "int" in the symbol table but assigned to "'helloWorld!'" which is type "string" 
       Scope dependant as well, check the level?*/
    public void typeChecker(String variableName, String strAssign, int curLevel) {
        // Declare & initialize variables (ST = Symbol Table)
        ArrayList<NodeType> listOfNodes = lookup(variableName);
        String dataTypeAssign = "int";

        // Check the string's datatype (letters indicate string, numbers indicate int, etc.)
        /* NOTICE: This needs to be updated, it can only detect chars or ints, not the others / pointers */
        if (listOfNodes != null) {
            // Check if it's an integer, catching an error means it's char
            try {
                Integer.parseInt(dataTypeAssign);
            } catch (NumberFormatException e) {
                dataTypeAssign = "char";
            }

            // Check for a mismatch and report it
            if (dataTypeST != dataTypeAssign) {
                System.out.println("Type Checker Error: key(" + variableName + ") does not have matching datatypes.");
            }
        } else {
            System.out.println("Semantic Error: Symbol Table searched, key("+ variableName +") does not exist.");
        }
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


        VarExp leftSide = exp.lhs.varriable;
        Exp rightSide = exp.rhs;

        String rightType = "";

        //check if int
        if(rightSide.value == (int)rightSide.value){
            rightType = "int";
        }//check if string
        else if(rightSide.value instanceof String ){
            rightType = "string";
        }//check if boolean
        else if(rightSide.value instanceof Boolean ){
            rightType = "boolean";
        }
        else{
            System.out.println("how???");
        }

        //get the type of the left hand side and compare their types
        ArrayList<NodeType> leftNode = lookup(leftSide.name);
        if(rightType == leftNode.type){
            
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
    public void visit( IntExp exp, int level );

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
    

    public void visit( VarExp exp, int level );

    public void visit( DecList decList, int level );

    //call insert here
    public void visit( ArrayDec arrDec, int level );

    public void visit( BoolExp exp , int level );

    public void visit( CallExp exp, int level );

    public void visit( CompoundExp exp, int level ){ //only time scope level changes

        level++;//scope level has changed, go deeper

        //*****Deal with the declarations/expressions******

        VarDecList tempVarDecList = exp.decs;
        while( tempVarDecList  != null ) {
            tempVarDecList.head.accept( this, level );
            tempVarDecList  = tempVarDecList.tail;
        }

        //Next print out the exps
        ExpList tempList = exp.exps;
        while( tempList != null ) {
            tempList.head.accept( this, level );
            tempList = tempList.tail;
        }
    }

    
    //Call insert here
    public void visit( FunctionDec FunDec, int level ){

        //new overall scope, push function name to stack, level change handled in the compoundExp
        scopeStack.push(FunDec.func);

        if(FunDec.body!=null){
            //handle the compound expression
            FunDec.body.accept(this, level);
        }
        scopeStack.pop();//remove the pushed function name to revert to the previous scope

    }

    public void visit( IndexVar var, int level );

    public void visit( NameTy type, int level );

    public void visit( NilExp exp, int level );

    public void visit( ReturnExp exp, int level );

    //call insert here
    public void visit( SimpleDec dec, int level );

    public void visit( SimpleVar var, int level );

    public void visit( VarDecList varDecList, int level );

}