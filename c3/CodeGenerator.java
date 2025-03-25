import absyn.AbsynVisitor;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator implements AbsynVisitor{

    /*Offsets */
    public int emitLoc = 0;
    public int highEmitLoc = 0;
    public int globalOffset = 0;

    /*Special Registers */
    public static final int PC = 7;
    public static final int GP = 6;
    public static final int FP = 5;
    public static final int AC = 0;
    public static final int AC1 = 1;

    /*Emit Functions */
    void emitRO(String op, int r, int s, int t, String c) {
        System.out.printf("%3d: %5s %d, %d, %d \t%s\n", emitLoc, op, r, s, t, c);
        
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    void emitRM(String op, int r, int d, int s, String c) {
        System.out.printf("%3d: %5s %d, %d(%d) \t%s\n", emitLoc, op, r, d, s, c);
        
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    public void emitRM_Abs(String op, int r, int a, String c) {
        System.out.printf("%3d: %5s %d, %d(%d) \t%s\n", emitLoc, op, r, a - (emitLoc + 1), PC, c);

        emitLoc++;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    int emitSkip(int distance) {
        int i = emitLoc;
        emitLoc += distance;
    
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    
        return i;
    }

    public void emitComment(String c) {
        System.out.println("* " + c);
    }
    
    void emitBackup(int loc) {
        if (loc > highEmitLoc) {
            emitComment("BUG in emitBackup");
        }
        emitLoc = loc;
    }
    
    void emitRestore() {
        emitLoc = highEmitLoc;
    }
    
    
}
