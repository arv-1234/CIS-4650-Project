package absyn;

public class ArrayDec extends VarDec {
    public NameTy typ;
    public String name;
    public int size;
    
    //Arrays need a name, set length and a type, row and cols are needed for all objects
    public ArrayDec ( int row, int col, NameTy decType, String name, int len ) {
        this.row = row;
        this.col = col;
        this.typ = decType;
        this.name = name;
        this.size = len;
    }

    public void accept( AbsynVisitor visitor, int level ) {
        visitor.visit( this, level );
    }

    public int getType(){
        return typ.typeVal;
    }

    public String getName(){
        return name;
    }

}