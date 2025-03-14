/* Goal: Traverse the abstract syntax tree in post-order and find/report semantic errors. */

import absyn.*;
import java.util.HashMap;
import java.util.ArrayList;

public class SemanticAnalyzer implements AbsynVisitor {
    /* -----------------------------  SYMBOL TABLE  ------------------------------------- */
    // Declare the variable
    HashMap<String, ArrayList<NodeType>> table;

    // Constructor: Initialize the variable
    public SemanticAnalyzer() {
        table = new HashMap<String, ArrayList<NodeType>>();
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


    

    /*Visit functions for traversing Tree */

    public void visit(NilExp exp, int level)
    {

    }

    public void visit( ExpList exp, int level );

    public void visit( AssignExp exp, int level );

    public void visit( IfExp exp, int level );

    public void visit( IntExp exp, int level );

    public void visit( OpExp exp, int level );

    public void visit( WhileExp exp, int level );

    public void visit( VarExp exp, int level );

    public void visit( DecList decList, int level );

    public void visit( ArrayDec arrDec, int level );

    public void visit( BoolExp exp , int level );

    public void visit( CallExp exp, int level );

    public void visit( CompoundExp exp, int level );

    public void visit( FunctionDec FunDec, int level );

    public void visit( IndexVar var, int level );

    public void visit( NameTy type, int level );

    public void visit( NilExp exp, int level );

    public void visit( ReturnExp exp, int level );

    public void visit( SimpleDec dec, int level );

    public void visit( SimpleVar var, int level );

    public void visit( VarDecList varDecList, int level );

}