import absyn.*;

/* For the symbol table's nodelist, each node has:
- The level (ex. 3)
- The variable name (ex. "x")
- The definition of when it was declared
- The scope (ex. "fun1") */
public class NodeType {
    public int level;
    public String variableName;
    public Dec def;

    public NodeType(int level, String variableName, Dec def) {
        this.level = level;
        this.variableName = variableName;
        this.def = def;
    }

    public String getName(){
        return variableName;
    }
}