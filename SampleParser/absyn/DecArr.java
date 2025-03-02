public class DecArr extends Dec {
    public NameTy decType;
    public String name;
    public int len;
    
    //Arrays need a name, set length and a type, row and cols are needed for all objects
    public DecArr ( int row, int col, NameTy decType, String name, int len ) {
        this.row = row;
        this.col = col;
        this.decType = decType;
        this.name = name;
        this.len = len;
    }

    public void accept( AbsynVisitor visitor, int level ) {
        visitor.visit( this, level );
    }
}