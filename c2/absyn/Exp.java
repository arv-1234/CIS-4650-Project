package absyn;

public abstract class Exp extends Absyn {

    public int getType(){
        return -1; //no type specified, -1 by default
    }

}
