public class FunDec extends Dec {
    public NameTy decType;
    public String name;
    public DecList parameters;
    
    //Arrays need a name, set length and a type, row and cols are needed for all objects
    public FunDec ( int row, int col, NameTy decType, String name, DecList dl ) {
        this.row = row;
        this.col = col;
        this.decType = decType;
        this.name = name;
        this.parameters = dl;
    }

    public void accept( AbsynVisitor visitor, int level ) {
        visitor.visit( this, level );
    }
}