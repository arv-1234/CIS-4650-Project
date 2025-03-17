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

        /*  Through re defining type checker we do not need this portion, kept it just in case tho
        String rightType = "";

        //check its type
        switch (rightSide.getType()) {
            case 0 -> rightType = "int";
            case 1 -> rightType = "boolean";
            default -> System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) +"Incorrect type given for assign exps right hand side");
        }
        */
            
        // Check if the types for the right side and left side match. 1 for match, -1 for mismatch, 0 for lhs wasn't declared
        int res = typeChecker(level, leftSide.name , rightSide.getType(), exp.row, exp.col);

        // Only print an error check 
        if (res == -1) {
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: In Assignment operator left and right hand side types differ\n");
        } else if (res == 0) {
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Assign operators left hand side variable was never declared\n");
        }
    }

    public void visit(IfExp exp, int level) {
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
        // Numerical operators
        if (exp.op == OpExp.PLUS || exp.op == OpExp.MINUS || exp.op == OpExp.TIMES || exp.op == OpExp.OVER) {
            // Only type check if both exist
            if (exp.left != null && exp.right != null) {
                // Both must be of type int, if not throw an error
                if (exp.left.getType() != 0) {
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: incorrect type for lefthand, not of type INT\n");
                }
                if (exp.right.getType() != 0) {
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: incorrect type for lefthand, not of type INT\n");
                }
            } else {
                // They must both exist for all of these operators, so if even one doesn't exist throw and error
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Syntax Error: Missing right/left values in mathmatical operator\n");
            }
        } else if (exp.op == OpExp.EQ || exp.op == OpExp.LT || exp.op == OpExp.GT || exp.op == OpExp.UMINUS || exp.op == OpExp.AND || exp.op == OpExp.OR || exp.op == OpExp.APPROX) {
            // Only type check if both exist
            if (exp.left != null && exp.right != null) {
                // Both must be of type bool, if not throw an error
                if (exp.left.getType() != 3) {
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: incorrect type for lefthand, not of type Boolean\n");
                }
                if (exp.right.getType() != 3) {
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: incorrect type for lefthand, not of type Boolean\n");
                }
            } else {
                // They must both exist for all of these operators, so if even one doesn't exist throw and error
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Syntax Error: Missing right/left values in mathmatical operator\n");
            }
        } else {
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Syntax Error: Unrecognized operator\n");
        }
    }

    // Again for this let the compoundExp accept handle semantic analysis
    public void visit(WhileExp exp, int level) {
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
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + "Syntax Error: Invalid array type detected: void\n");
            return;
        } else if (arrDec.typ.typeVal == 2) {
            tempType = "null";
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + "Syntax Error: Invalid array type detected: null\n");
            return;
        } else if (arrDec.typ.typeVal == 3) {
            tempType = "bool";
        } else {
            System.err.println("Error in line " + (arrDec.row + 1) + ", column " + (arrDec.col + 1) + "Syntax Error: Invalid type detected\n");
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

    /* 
    // Verify that the arguments match previous declarations
    public void visit(CallExp exp, int level) {
        // Check if the function exists, check the global scope as they can only be defined there, lastElement is the initially pushed table
        FunctionDec prevDef = null;
        HashMap<String, ArrayList<NodeType>> tempTable = tableStack.lastElement();
        ArrayList<NodeType> tempArr = tempTable.get(exp.fun);

        // Check for instances of the called functions declaration
        if (tempArr != null) {
            for(NodeType tempNode: tempArr) {
                // Previous definition was found! have prevDef point to it, check if its a prototype or definition
                if (tempNode.variableName.equals(exp.fun)) {
                    // Typecast to FunDec type
                    prevDef = (FunctionDec) tempNode.def;
                    
                    // If it is not a prototype, we have found a definition and we can break, there can only be 1
                    if(prevDef.body.isNilExp() == -1) {
                        break;
                    } else {
                        // Revert it back to null to show we havent found anything
                        prevDef = null;
                    }
                }
            }

            // Never found a declaration, throw error and return(no body to analyze)
            if (prevDef == null) {
                
                System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: Function was never defined\n");
                return;
            }
        } else {
            // No declarations initially, no function definition, throw error and return
            System.err.println("1Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + " Semantic Error: Function was never defined\n");
            return;
        }

        // Found the definition, compare args and defined parameters
        // Check if called arguments match defined parameters by looping through the arguments and parameters for the found declaration
        ExpList tempArgList = exp.args;
        VarDecList tempParamList = ((FunctionDec)prevDef).parameters;
        int tempArgType = -1;
        int tempParamType = -1;

        // If both null, most likely was defined as void so consider it valid
        if (tempArgList.head == null && tempParamList.head == null) {
            return;
        } else if (tempArgList.head == null && tempParamList.head != null) { 
            // Passed a void/empty but was expecting something else
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function does not expect void arguments");
            return;   
        } else if (tempArgList.head != null && tempParamList.head == null) { 
            // Expected void but got actual arguments
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function expected void arguments but something else was passed");
            return; 
        } else {
            while(tempArgList!=null && tempParamList!=null){
                // Go through defined parameters and passed arguments
                Dec tempVar = (Dec)tempParamList.head; 
                Exp tempExp = (Exp)tempArgList.head;

                // Check if the types match
                tempArgType = tempExp.getType(); // 0 for int, 3 for bool, -1 for invalid
                tempParamType = tempVar.getType(); // 0 for int, 3 for bool, 1 for void, 2 for null

                if(tempArgType!=tempParamType){
                    System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Function call contains invalid types");
                    System.err.println(tempVar.getName() +" expected a different type\n");
                }

                // Move to the next parameter/argument
                tempArgList = tempArgList.tail;
                tempParamList = tempParamList.tail;
            }
        }
    }
    */
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
    
        // Parameter/argument matching logic remains unchanged
        if (tempArgList.head == null && tempParamList.head == null) {
            return;
        } else if (tempArgList.head == null && tempParamList.head != null) { 
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
                Exp tempExp = (Exp) tempArgList.head;
    
                tempArgType = tempExp.getType();
                tempParamType = tempVar.getType();

                System.err.println("Name of variable: " + tempVar.getName());
                System.err.println("tempArgType : " + tempArgType);
                System.err.println("tempParamType: " + tempParamType);
    
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
        // Scope level has changed, go deeper
        level++;

        // Print for the symbol table
        indent(level);
        System.out.println("Entering a new block:");

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
            System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function not defined in the global scope\n");
            return;
        }
    
        NodeType tempNode = new NodeType(level, FunDec.func, FunDec);
        ArrayList<NodeType> tempArr = tableStack.peek().get(FunDec.func);
    
        if (FunDec.body.isNilExp() != 1) { // Function definition
            int prevReturnType = currentReturnType;
            currentReturnType = FunDec.result.typeVal;
    
            indent(level + 1);
            System.out.print("New Scope for function " + FunDec.func + " ");
    
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
                    System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function '" + FunDec.func + "' was already defined\n");
                    return;
                }
            } else {
                insert(FunDec.func, tempNode, FunDec.col + 1, FunDec.row + 1);
            }
    
            // Add parameters and process body
            VarDecList params = FunDec.parameters;
            while (params != null) {
                params.head.accept(this, level + 1);
                params = params.tail;
            }
    
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
    
    /* 
    public void visit(FunctionDec FunDec, int level) {
        // Check if we are in the global scope, global scope if only one table is in the stack
        if (tableStack.size() != 1) {
            System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function not defined in the global scope\n");
            return;
        }
    
        // Check if any functions of the same name already exists
        NodeType tempNode = new NodeType(level, FunDec.func, FunDec);
        ArrayList<NodeType> tempArr = tableStack.peek().get(FunDec.func);
    
        // Function has a body (definition)
        if (FunDec.body.isNilExp() != 1) {
            int prevReturnType = currentReturnType;
            currentReturnType = FunDec.result.typeVal;
    
            // PRESERVE ORIGINAL PRINT STATEMENTS
            indent(level+1);
            System.out.print("New Scope for function " + FunDec.func + " ");
    
            // Check for previous definitions
            if (funcWasDefined(tempArr, tempNode, FunDec.row, FunDec.col) == 0) {
                insert(FunDec.func, tempNode, FunDec.col + 1, FunDec.row + 1);
            } else {
                System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function was already previously defined\n");
            }

            // ADD PARAMETERS TO FUNCTION SCOPE AFTER CREATING BODY SCOPE
            VarDecList params = FunDec.parameters;
            while (params != null) {
                params.head.accept(this, level + 1);  // Insert params into function scope
                params = params.tail;
            }
    
            // Process function body (this creates new scope via CompoundExp)
            if (FunDec.body != null) {
                FunDec.body.accept(this, level);
            }
    
            // PRESERVE ORIGINAL INDENTATION
            indent(level+1);
            System.out.println("Exiting function " + FunDec.func + " scope");
    
            currentReturnType = prevReturnType;
        } 
        // Function prototype
        else {
            insert(FunDec.func, tempNode, FunDec.col + 1, FunDec.row + 1);
            indent(level);
            System.out.println("Function prototype: " + FunDec.func);
        }
    }
    */
    /*
    // Call insert here, check for any previous declarations
    public void visit(FunctionDec FunDec, int level) {
        // Check if we are in the global scope, global scope if only one table is in the stack
        if (tableStack.size() != 1) {
            System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function not defined in the global scope\n");
            return;
        }

        // Check if any functions of the same name already exists, unlike variables we cannot have more than one function of the same name, so if one is found throw error 
        NodeType tempNode = new NodeType(level, FunDec.func, FunDec);
        
        // Resulting array of nodes for when we are either dealing with a function prototype or function defining declaration, must treat each differently
        ArrayList<NodeType> tempArr = tableStack.peek().get(FunDec.func);

        // Function has a body, dealing with body defining declaration
        if (FunDec.body.isNilExp() != 1) {
            // If it has a body, must have a return type, swap our current global tracking variable to track the return type
            int prevReturnType = currentReturnType;
            currentReturnType = FunDec.result.typeVal;

            // Also if it has a body we must declare a scope change with function
            indent(level+1);
            System.out.print("New Scope for function " + FunDec.func + " "); //print instead of println so it connects to the print in compoundExp

            // No previous function declarations match so we can insert
            if (funcWasDefined(tempArr, tempNode, FunDec.row, FunDec.col) == 0) {
                // If the previous checks are false, valid function declaration, store the function into the table
                insert(FunDec.func, tempNode, FunDec.col + 1, FunDec.row + 1);
            } else {
                // Function was already defined else where, throw an error and continue
                System.err.println("Error in line " + (FunDec.row + 1) + ", column " + (FunDec.col + 1) + "Semantic Error: Function was already previously defined\n");
            }

            // Handle the body/new scope
            if (FunDec.body != null) {
                FunDec.body.accept(this, level);
            }

            // After dealing with the body we leave so revert the return type
            currentReturnType = prevReturnType;
        } else {
            // Dealing with a prototype declaration, if anything else was previously defined with a same name throw an error
            insert(FunDec.func, tempNode, FunDec.col + 1, FunDec.row + 1);
        }
    }
*/
    // Check if the var's index is a int, can be done because index is of type exp
    public void visit(IndexVar var, int level) {
        // We simply need to check if the index is of type int
        if (var.index.getType() != 0) {
            System.err.println("Error in line " + (var.row + 1) + ", column " + (var.col + 1) + "Syntax Error: Index is not of type int\n");
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
        if (currentReturnType != exp.getType()) {
            System.err.println("Error in line " + (exp.row + 1) + ", column " + (exp.col + 1) + "Semantic Error: Invalid return type\n");
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
            System.err.println("Error in line " + (var.row + 1) + ", column " + (var.col + 1) + "Semantic Error: Varriable was not declared\n");
        }
    }

    public void visit(VarDecList varDecList, int level) {
        while(varDecList != null) {
            varDecList.head.accept(this, level);
            varDecList = varDecList.tail;
        }
    }
}