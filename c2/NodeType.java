import absyn.*;

// This class holds the level, variable name, and declared datatype for each key in the symbol table
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