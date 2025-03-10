/* Goal: Traverse the abstract syntax tree in post-order and find semantic errors. */

import java.io.*;
import absyn.*;
import java.util.Stack;
import java.util.HashMap;

public class SemanticAnalyzer {
    /* -----------------------------  SYMBOL TABLE  ------------------------------------- */
    /*  Declare & initialize the variable:
        Stack - represents the current number of scopes or blocks we're in
        HashMap<String1,String2> - each hashmap represents a scope/block's symbol table 
            String1 is the variable name (key), ex. "x"
            String2 is the data type (item), ex. "int" */
    Stack<HashMap<String,String>> Scope = new Stack<HashMap<String,String>>(); 

    // Add a new inner scope (enter)
    public void addScope() {
        Scope.push(new HashMap<String,String>());
    }
    
    // Remove the inner scope (exit)
    public void removeScope() {
        Scope.pop();
    }

    // Constructor - Push the first scope into the stack
    public SemanticAnalyzer() {
        addScope();
    }

    // Look up if a Symbol exists in the Symbol Table (in all scopes)
    // Error Check: Symbol doesn’t exist, Return: Hashmap item using the key
    public String lookup(String variableName) {
        // We look through all of the available scopes to see if there is a key that exists
        for (HashMap<String,String> symbolTable : Scope.keySet()) {
            if (symbolTable.containsKey(variableName)) {
                return symbolTable.get(variableName);
            }
        }

        // Semantic Error: Symbol Table searched, key does not exist.
        return null;
    }

    // Add a Symbol to the Symbol Table (in the top/innermost scope)
    // Error Check: Symbol already exists
    public void insert(String variableName, String dataType) {
        boolean exists = false; 

        // Check if it exists in any of the symbol tables
        for (HashMap<String,String> symbolTable : Scope.keySet()) {
            if (symbolTable.containsKey(variableName)) {
                System.out.println("Semantic Error: Cannot insert, key already exists.");
                exists = true;
                break;
            }
        }

        // If it doesn't exist, add it to the top/innermost symbol table
        if (exists == false) {
            HashMap<String,String> symbolTable = Scope.get(Scope.size()-1);
            symbolTable.put(variableName, dataType);
        }
    }

    // Delete a Symbol that exists in the Symbol Table (in the top/innermost scope)
    // Error Check: Symbol doesn’t exist
    public void delete(String variableName, String dataType) {
        HashMap<String,String> symbolTable = Scope.get(Scope.size()-1);
        
        if (symbolTable.containsKey(variableName)) {
            symbolTable.remove(variableName);
        } else {
            System.out.println("Semantic Error: Cannot delete, key does not exist.");
        }
    }

    /* -----------------------------  TYPE CHECKER  ------------------------------------- */
    /* Checks the variable's type from the symbol table vs. from the current assignment in code
       Mismatch Example: variable "x" is type "int" in the symbol table but assigned to "'helloWorld!'" which is type "string" */
    public void typeChecker(String variableName, String strAssign) {
        // Declare & initialize variables (ST = Symbol Table)
        String dataTypeST = lookup(variableName);
        String dataTypeAssign = "int";

        // Check the string's datatype (letters indicate string, numbers indicate int, etc.)
        /* NOTICE: This needs to be updated, it can only detect chars or ints, not the others / pointers */
        if (dataTypeST != null) {
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
}