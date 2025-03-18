/* Goal: Traverse the abstract syntax tree in post-order and find/report semantic errors. */

import absyn.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

public class SemanticAnalyzer implements AbsynVisitor {
    // Declare variables
    public Stack<HashMap<String, ArrayList<NodeType>>> tableStack; // Will change block by block
    final static int SPACES = 4;
    public int currentReturnType = -1;
    public VarDecList paramsToAdd = null;
    public String funcMsg = "";

    // Indent: creates spacing to organize display
    private void indent( int level ) {
        for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    }

    // Constructor: Initialize the variables
    public SemanticAnalyzer() {
        // Store the tables for each stack, creates a linked table relationship
        tableStack = new Stack<HashMap<String, ArrayList<NodeType>>>(); 
        
        // The first scope that the stack holds is the global scope
        tableStack.push( new HashMap<String, ArrayList<NodeType>>());

        // Declare that we have entered the global scope
        System.out.println("Entering the global scope:");
    }

    /* -----------------------------  SYMBOL TABLE  ------------------------------------- */
    // Insert: Adds a new node to the array of nodes in the topmost hashmap scope by using a key
    // Error Checker: Looks for redefined variables
    public void insert(String key, NodeType node, int col, int row) {
        if (tableStack.peek().get(key) != null) {
            System.err.println("Semantic Error (col "+col+", row "+row+"): cannot insert redefined variable key '"+key+"'.");
        } else {
            ArrayList<NodeType> nodeList = new ArrayList<NodeType>();
            nodeList.add(node);
            tableStack.peek().put(key, nodeList);
        }
    }

    // Lookup: Looks for a key's array of nodes from the all of the currently available hashmap scopes in the stack
    // Error Checker: Looks for undefined variables
    public ArrayList<NodeType> lookup(String key, int col, int row) {
        // Loop through the entire stack (from top to bottom) and find the node list by using the key on every hashmap scope
        for (int i = tableStack.size()-1; i >= 0; i--) {
            if (tableStack.get(i).get(key) != null) {
                return tableStack.get(i).get(key);
            }
        }
        // If we cannot find it in the stack, it doesn't exist
        System.err.println("Semantic Error (col "+col+", row "+row+"): cannot look up undefined variable key '"+key+"'.");
        return null;
    }

    // Delete: Deletes a key and an array of nodes from the topmost hashmap scope
    // Error Checker: Looks for undefined variables
    // Notice: Currently there is no use for delete() as we'll just pop the entire hashmap scope off of the stack when exiting it
    public void delete(String key, int col, int row) {
        if (tableStack.peek().get(key) != null) {
            tableStack.peek().remove(key);
        } else {
            System.err.println("Semantic Error (col "+col+", row "+row+"): cannot delete undefined variable key '"+key+"'.");
        }
    }

    /* -----------------------------  TYPE CHECKER  ------------------------------------- */
    /* 
      Given the current scopes table of declarations, use get to get the stored variable and verify it's got the same type as the passed type
      Since everything in the stack are the current scopes declarations and parent scopes we must check them all, however we use the most recently defined
      rule( from the lecture ) where we favor the closest scopes declaration. Assume a variable is being passed
      returns 1 upon success, -1 on type mismatch, 0 on not found.
    */
    public int typeChecker(int level, String varName, int type, int row, int col) {
        // Declare variables
        ArrayList<NodeType> tempList;
        Dec tempDec;

        // Loop through the stack from top(current) to bottom(global) 
        tempList = lookup(varName, col + 1, row + 1);

        // If there's an instance, check if one of the node types match the given type
        if (tempList != null) {
            for(NodeType tempNode: tempList) {
                tempDec = tempNode.def;

                // Type match where 1 = success and -1 = mismatch 
                if (tempDec.getType() == type) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }

        // 0 = no matches were found or it wasn't declared
        return 0;
    }

    // Function that checks if a varriable was defined in the current scope or above. used to determine if a VARIABLE was declared before
    // When we find a declaration, we can stop and return 1 for a success, 0 for failure
    public int wasDefined(String name) {
        // Declare Variable
        ArrayList<NodeType> tempList;

        // Go through all of the scopes, we can reference declarations from higher scopes
        tempList = lookup(name, 0, 0);

        // 1 = not null, declaration found
        if (tempList != null) {
            return 1;
        }
        
        // 0 = null, no declaration found
        return 0;
    }
    
    // Loop through the node list and find if a body was previously defined
    public int funcWasDefined(ArrayList<NodeType> storedNodes, NodeType curNode, int row, int col) {
        // Nothing was found 
        if (storedNodes == null) {
            return 0;
        }

        FunctionDec tempFunc;
        for( NodeType tempNode: storedNodes){
            tempFunc = (FunctionDec)tempNode.def;

            // If the body is not a NilExp, means it was defined, return 1 as a "found"
            if(tempFunc.body.isNilExp()!=1){
                return 1;
            }
        }

        // Nothing was found
        return 0;
    }


    //any exp types, this will get its datat type
    public int getExpType(Exp exp){

        ArrayList<NodeType> tempVarList;
        String varName;

        if(exp==null){
            return -1;
        }


        if(exp instanceof VarExp){//means we have a varriable being passed, is probably of type simpleVar
            varName = ((VarExp)exp).variable.getName();
            //find previous instance, look at the prev dec and get the type, loop through all scopes
            if(!"".equals(varName)){
            
                tempVarList = lookup(varName, exp.row, exp.col);
                return tempVarList.get(0).def.getType();//get the type value of the time it was declared


            }
            else{//not found, simply default
                return exp.getType();
            }
        }
        else if(exp instanceof CallExp){//if a function call was detected, find its type

            varName = ((CallExp)exp).fun;

            tempVarList = lookup(varName, exp.row, exp.col);
            FunctionDec tempFunDec = (FunctionDec)tempVarList.get(0).def;//prototype or not will still have the type 
            return tempFunDec.result.typeVal;//return the declared return type
        }
        else if(exp instanceof OpExp){//expression passed is an operator expression

            OpExp tempExp = (OpExp)exp;
            //if of the numerical type return 0
            if (tempExp.op == OpExp.PLUS || tempExp.op == OpExp.MINUS || tempExp.op == OpExp.TIMES || tempExp.op == OpExp.OVER) {
                return 0;
            }
            else if (tempExp.op == OpExp.EQ || tempExp.op == OpExp.LT || tempExp.op == OpExp.GT || tempExp.op == OpExp.UMINUS || tempExp.op == OpExp.AND || tempExp.op == OpExp.OR || tempExp.op == OpExp.APPROX){//will yield a boolean type
                return 3;
            }
            else{
                return -1;//unknown type, return -1
            }


        }
        else{
            return  exp.getType();
        }
    }



    /* ----------------------  VISIT FUNCTIONS FOR TREE TRAVERSAL  ---------------------- */
    // Loop through expressions and 'visit' each one
    public void visit(ExpList exp, int level) {
        while(exp != null) {
            exp.head.accept(this, level);
            exp = exp.tail;
        }
    }

    // Check if the right hand sides type matches the left hand side
    public void visit(AssignExp exp, int level) {
        // Current scopes table, peek to get the most recent scope(current one)
        HashMap<String, ArrayList<NodeType>> curTable = tableStack.peek();
        SimpleVar leftSide = (SimpleVar)exp.lhs.variable; // Should always be simple var as left hand will be a variable
        Exp rightSide = exp.rhs;
            
        // Check if the types for the right side and left side match. 1 for match, -1 for mismatch, 0 for lhs wasn't declared
        int res = typeChecker(level, leftSide.name , rightSide.getType(), exp.row, exp.col);

        // Only print an error check 
        if (res == -1) {
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: In Assignment operator left and right hand side types differ\n");
        } else if (res == 0) {
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: Assign operators left hand side variable was never declared\n");
        }

        //handle the left and right hand expressions seperately
        exp.lhs.accept( this, level );
        exp.rhs.accept( this, level );

    }

    public void visit(IfExp exp, int level) {

          //check if the test is a boolean expression
        if(getExpType(exp.test)!=3){
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: if Conditional statement is not of the boolean type\n");
        }

        // Conditional statement
        exp.test.accept(this, level);

        // This is going to be a compound stmt, let the respective visit function handle it
        exp.thenpart.accept(this, level);
        if (exp.elsepart != null) {
            exp.elsepart.accept(this, level);
        }
    }

    public void visit(IntExp exp, int level) {
        // Will never be needed for the semantic analyzer, used as a placeholder for errors
    }

    // Check if its a logic or math operator and type check accordingly
    public void visit(OpExp exp, int level) {

        //System.err.println("WHAT VAREXPGET GOT:" + getExpType(exp.left));

        // operators that require numbers
        if (exp.op == OpExp.PLUS || exp.op == OpExp.MINUS || exp.op == OpExp.TIMES || exp.op == OpExp.OVER || exp.op == OpExp.EQ || exp.op == OpExp.LT || exp.op == OpExp.GT) {
            // Only type check if both exist
            if (exp.left != null && exp.right != null) {
                // Both must be of type int, if not throw an error
                if (getExpType(exp.left) != 0) {
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: incorrect type for lefthand, not of type INT\n");
                }
                if (getExpType(exp.right)!= 0) {
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: incorrect type for righthand, not of type INT\n");
                }
            } else {
                // They must both exist for all of these operators, so if even one doesn't exist throw and error
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Syntax Error: Missing right/left values in mathmatical operator\n");
            }
        } else if (exp.op == OpExp.UMINUS || exp.op == OpExp.AND || exp.op == OpExp.OR || exp.op == OpExp.APPROX) {//operators that require boolean values
            // Only type check if both exist
           
        
            if (exp.left != null && exp.right != null) {
                // Both must be of type bool, if not throw an error
                if ( getExpType(exp.left)!= 3) {
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: incorrect type for lefthand, not of type Boolean\n");
                }
                if (getExpType(exp.right) != 3) {
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: incorrect type for righthand, not of type Boolean\n");
                }
            } else {
                // They must both exist for all of these operators, so if even one doesn't exist throw and error
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Syntax Error: Missing right/left values in mathmatical operator\n");
            }
        } else {
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Syntax Error: Unrecognized operator\n");
        }
    }

    // Again for this let the compoundExp accept handle semantic analysis
    public void visit(WhileExp exp, int level) {

        //check if the test is a boolean expression
        if(getExpType(exp.test)!=3){
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: While Conditional statement is not of the boolean type\n");
        }

        // If test expression exists, preform semantic analysis
        if (exp.test != null) {
            exp.test.accept(this,level);
        }

        exp.body.accept(this, level);
    }
    
    public void visit(DecList decList, int level) {
        // Print out the decs stored
        DecList tempDecList = decList;
  
        // Make sure it's not empty
        if (tempDecList.head != null) {
            while(tempDecList != null) {
                tempDecList.head.accept(this, level);
                tempDecList = tempDecList.tail;
            }
        }
    }

    // Call insert here,type check as well with previous instances
    public void visit(ArrayDec arrDec, int level) {
        String tempType = "";

        // Get the current type
        if (arrDec.typ.typeVal == 0) {
            tempType = "int";
        } else if (arrDec.typ.typeVal == 1) {
            tempType = "void";
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + " Syntax Error: Invalid array type detected: void\n");
            return;
        } else if (arrDec.typ.typeVal == 2) {
            tempType = "null";
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + " Syntax Error: Invalid array type detected: null\n");
            return;
        } else if (arrDec.typ.typeVal == 3) {
            tempType = "bool";
        } else {
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + " Syntax Error: Invalid type detected\n");
            return;
        }

        indent(level);

        // Print out declaration for symbol table
        System.out.println(arrDec.name + ": " + tempType);

        // Insert a new node for the variable to keep track of the level, varriable name, type, and scope
        insert(arrDec.name, new NodeType(level, arrDec.name, arrDec), arrDec.col + 1, arrDec.row + 1);
    }

    public void visit(BoolExp exp , int level) {
        // Will never be needed for the semantic analyzer, used as a placeholder for errors
    }


    public void visit(CallExp exp, int level) {
        
        // CHANGE 1: Use FIRST ELEMENT (global scope) instead of lastElement()/0 index table
        HashMap<String, ArrayList<NodeType>> tempTable = tableStack.get(0);
        ArrayList<NodeType> tempArr = tempTable.get(exp.fun);
        FunctionDec prevDef = null;
    
        // CHANGE 2: Simplify prototype/definition check
        if (tempArr != null && !tempArr.isEmpty()) {
            // Accept either prototype or definition
            for(NodeType tempNode: tempArr) {
                prevDef = (FunctionDec) tempNode.def;
                if(prevDef.body.isNilExp()!=1){ //if the body isn't a null exp we found instance
                    break;
                }
                else{ //was an empty body, most likely prototype, reset it to null
                    prevDef = null;
                }
            }

            //went through all matching names in scope, if prevDef is still null never found so throw error and return
            if(prevDef == null){
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) 
                            + ": Function '" + exp.fun + "' was never defined");
                return;
            }


        } else {
            // CHANGE 3: Consistent error message formatting
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) 
                            + ": Function '" + exp.fun + "' was never defined");
            return;
        }
    
        // CHANGE 4: Remove redundant null check (tempArr guaranteed non-null here)
        ExpList tempArgList = exp.args;
        VarDecList tempParamList = prevDef.parameters;
        int tempArgType = -1;
        int tempParamType = -1;
    

        //System.err.println("IN CALL NAME IS :" + tempParamList.head.getName()+ " END");

        // Parameter/argument matching logic remains unchanged
        if ( tempArgList.head.isNilExp() == 1 && tempParamList.head == null) { //expects void, no need to continue on
            return;
        } else if (tempArgList.head.isNilExp() == 1 && tempParamList.head != null) { 
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) 
                            + ": Function does not expect void arguments");
            return;   
        } else if (tempArgList.head != null && tempParamList.head == null) { 
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) 
                            + ": Function expected void arguments but got parameters");
            return; 
        } else {
            while(tempArgList != null && tempParamList != null) {
                Dec tempVar = (Dec) tempParamList.head; 
                Exp tempExp =  (Exp) tempArgList.head;
    
                if(tempArgList.head instanceof VarExp){//means we have a varriable being passed, is probably of type simpleVar
                    String varName = ((VarExp)tempArgList.head).variable.getName();
                    //find previous instance, look at the prev dec and get the type, loop through all scopes
                    if(!"".equals(varName)){
                       
                        ArrayList<NodeType> tempVarList;
                        tempVarList = lookup(varName, exp.row, exp.col);
                        tempArgType = tempVarList.get(0).def.getType();//get the type value of the time it was declared


                    }
                    else{
                        tempArgType = tempExp.getType();
                    }
                }
                else{
                    tempArgType = tempExp.getType();
                }

                
                tempParamType = tempVar.getType();

                /* 
                System.err.println("Name of variable: " + tempVar.getName());
                System.err.println("tempArgType : " + tempArgType);
                System.err.println("tempParamType: " + tempParamType);
                */
    
                if(tempArgType != tempParamType) {
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: Function call contains invalid types");
                    System.err.println(tempVar.getName() +" expected a different type\n");
                }
    
                tempArgList = tempArgList.tail;
                tempParamList = tempParamList.tail;
            }
        }
    }

    
    // Only time scope level changes
    public void visit(CompoundExp exp, int level) {
        // New scope, go down a level and create a new symbol table for the new scope
        tableStack.push(new HashMap<String, ArrayList<NodeType>>());

        // Print for the symbol table
        indent(level);
        System.out.println("Entering a new block " + funcMsg+ ":");
        funcMsg = "";//reset it after
        // Scope level has changed, go deeper
        level++;

        //handle possible function parameter declarations, need to reference it in the scope
        while (paramsToAdd != null) {
            paramsToAdd.head.accept(this, level + 1);
            paramsToAdd = paramsToAdd.tail;
        }

        paramsToAdd = null;//reset it for the next possible function dec

        /*****Deal with the declarations/expressions*****/
        VarDecList tempVarDecList = exp.decs;
        while(tempVarDecList != null) {
            tempVarDecList.head.accept(this, level);
            tempVarDecList  = tempVarDecList.tail;
        }

        ExpList tempList = exp.exps;
        while(tempList != null) {
            tempList.head.accept(this, level);
            tempList = tempList.tail;
        }

        // Finished with the current scope, pop it from the stack and move back up to the previous scope and level
        tableStack.pop();
        level--;
    }

    

     
    public void visit(FunctionDec FunDec, int level) {
        if (tableStack.size() != 1) {
            System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + " Semantic Error: Function not defined in the global scope\n");
            return;
        }
    
        NodeType tempNode = new NodeType(level, FunDec.func, FunDec);
        ArrayList<NodeType> tempArr = tableStack.peek().get(FunDec.func);
    

        if (FunDec.body.isNilExp() != 1) { // Function definition
            int prevReturnType = currentReturnType;
            currentReturnType = FunDec.result.typeVal;//we use this to check if return exp matches
    
            funcMsg = "for function " + FunDec.func;//passes this to compoundExp
    
            if (tempArr != null) {
                boolean hasPrototype = false;
                for (NodeType node : tempArr) {
                    FunctionDec existingFunc = (FunctionDec) node.def;
                    if (existingFunc.body.isNilExp() == 1) { // Check if existing entry is a prototype
                        hasPrototype = true;
                        break;
                    }
                }
                if (hasPrototype) {
                    // Replace prototype with definition
                    tempArr.clear();
                    tempArr.add(tempNode);
                } else {
                    System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + " Semantic Error: Function '" + FunDec.func + "' was already defined\n");
                    return;
                }
            } else {
                insert(FunDec.func, tempNode, FunDec.col + 1, FunDec.row + 1);
            }
    
            // Add parameters and process it in the body
            paramsToAdd = FunDec.parameters; //we are gonna handle this in the boddy
           /*  while (params != null) {
                params.head.accept(this, level + 1);
                params = params.tail;
            }
            */

            if (FunDec.body != null) {
                FunDec.body.accept(this, level);
            }
    
            indent(level + 1);
            System.out.println("Exiting function " + FunDec.func + " scope");
            currentReturnType = prevReturnType;
        } else { // Function prototype
            if (tempArr == null) {
                insert(FunDec.func, tempNode, FunDec.col + 1, FunDec.row + 1);
                indent(level);
                System.out.println("Function prototype: " + FunDec.func);
            } else {
                // Check if prototype already exists; if not, add it
                boolean prototypeExists = false;
                for (NodeType node : tempArr) {
                    FunctionDec existingFunc = (FunctionDec) node.def;
                    if (existingFunc.body.isNilExp() == 1) {
                        prototypeExists = true;
                        break;
                    }
                }
                if (!prototypeExists) {
                    tempArr.add(tempNode);
                }
            }
        }
    }
    
    // Check if the var's index is a int, can be done because index is of type exp
    public void visit(IndexVar var, int level) {
        // We simply need to check if the index is of type int
        if (getExpType(var.index) != 0) {
            System.err.println("Error in line " + (var.row + 1) + ", column " + (var.col + 1) + " Syntax Error: Index is not of type int\n");
        }
    }

    public void visit(NameTy type, int level) {
        // Will never be needed for the semantic analyzer, used as a placeholder for errors
    }

    public void visit(NilExp exp, int level) {
        // Will never be needed for the semantic analyzer, used as a placeholder for errors
    }

    // Matches the functions return type?
    public void visit(ReturnExp exp, int level) {
        // Check if current scopes return type matches the return statements type
        if (currentReturnType != getExpType(exp.exp)) {
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: Invalid return type\n");
        }
    }

    public void visit(VarExp exp, int level) {
        exp.variable.accept(this, level);
    }

    // Check if it was previously declared or not. if it was throw an error, if it wasn't insert into the current scopes stack
    // ONLY CHECK THE CURRENT SCOPE 
    public void visit( SimpleDec dec, int level ) {
        indent(level);
        String dataType = "";

        // Get its type in string form, maybe defn a function for this if we have time
        if (dec.getType() == 0) {
            dataType = "int";
        } else if (dec.getType() == 3) {
            dataType = "boolean";
        } else {
            dataType = "invalid";
        }

        // Print declared varriable details like the symbol table
        System.out.println(dec.name + ": " + dataType );

        // Get the current scopes table and find an instance of the declared variables name in the current scope
        insert(dec.name, new NodeType(level, dec.name, dec), dec.col + 1, dec.row + 1);
    }

    // Found an instance of a varriable, check to see if it was declared previously
    public void visit(SimpleVar var, int level) {
        if (wasDefined(var.name) != 1) {
            System.err.println("Error in line " + (var.row + 1) + ", column " + (var.col + 1) + " Semantic Error: Varriable was not declared\n");
        }
    }

    public void visit(VarDecList varDecList, int level) {
        while(varDecList != null) {
            varDecList.head.accept(this, level);
            varDecList = varDecList.tail;
        }
    }
}