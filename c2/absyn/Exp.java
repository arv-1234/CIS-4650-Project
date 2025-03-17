package absyn;

public abstract class Exp extends Absyn {

    public Dec dtype = null;


    public int getType(){
        return -1; //no type specified, -1 by default
    }

    //helps us determine if an expression was a nilExp or not, will return 1 if it is
    public int isNilExp(){
        return -1;
    }

}
