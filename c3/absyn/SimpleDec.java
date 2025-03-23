package absyn;

public class SimpleDec extends VarDec {
    public NameTy typ;
    public String name;

    public SimpleDec ( int row, int col, NameTy decType, String name ) {
        this.row = row;
        this.col = col;
        this.typ = decType;
        this.name = name;
    }

    public void accept( AbsynVisitor visitor, int level ) {
        visitor.visit( this, level );
    }

    
    public int getType() {
        return typ.typeVal; // Return the actual type (0 = int, 3 = bool, etc.)
    }

    public String getName(){
        return name;
    }



}