import absyn.*;

// This class holds the level, variable name, and declared datatype for each key in the symbol table
public class NodeType {
    public int level;
    public String variableName;
    public Dec dataType;

    public NodeType(int level, String variableName, Dec dataType) {
        this.level = level;
        this.variableName = variableName;
        this.dataType = dataType;
    }
}