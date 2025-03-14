import absyn.*;

// This class holds the level, variable name, and declared datatype for each key in the symbol table
public class NodeType {
    public int level;
    public String variableName;
    public int dataType;

    public NodeType(int level, String variableName, int dataType) {
        this.level = level;
        this.variableName = variableName;
        this.dataType = dataType;
    }
}