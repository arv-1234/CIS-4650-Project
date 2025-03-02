package absyn;
public class TypeName extends Absyn {
    
    public final static int INT = 0;
    public final static int VOID = 1;
    public final static int NULL = 2;
    public final static int BOOL = 3;

    public int typeVal;
    
    public SingleDec ( int row, int col, int type) {
        this.row = row;
        this.col = col;
        this.typeVal = type;
    }

    public void accept( AbsynVisitor visitor, int level ) {
        visitor.visit( this, level );
    }
}