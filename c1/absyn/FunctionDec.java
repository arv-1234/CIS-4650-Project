package absyn;

public class FunctionDec extends Dec {
    public NameTy result;
    public String func;
    public VarDecList parameters;
    public Exp body;
    
    //Arrays need a name, set length and a type, row and cols are needed for all objects
    public FunctionDec ( int row, int col, NameTy type, String name, VarDecList vdl, Exp b ) {
        this.row = row;
        this.col = col;
        this.result = type;
        this.func = name;
        this.parameters = vdl;
        this.body = b;
    }

    public void accept( AbsynVisitor visitor, int level ) {
        visitor.visit( this, level );
    }
}