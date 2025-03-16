import absyn.*;

/* For the symbol table's nodelist, each node has:
- The level (ex. 3)
- The variable name (ex. "x")
- The declared datatype (ex. 0 = "int") 
- The scope (ex. "fun1") */
public class NodeType {
    public int level;
    public String variableName;
    public int dataType;
    public String scope;

    public NodeType(int level, String variableName, int dataType, String scope) {
        this.level = level;
        this.variableName = variableName;
        this.dataType = dataType;
        this.scope = scope;
    }
}