public class SingleDec extends Dec {
    public NameTy decType;
    public String name;

    public SingleDec ( int row, int col, NameTy decType, String name ) {
        this.row = row;
        this.col = col;
        this.decType = decType;
        this.name = name;
    }

    public void accept( AbsynVisitor visitor, int level ) {
        visitor.visit( this, level );
    }
}