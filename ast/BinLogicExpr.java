package ast;
import compiler.Failure;
import compiler.Position;

/** Abstract syntax for binary logical expressions.
 */
public abstract class BinLogicExpr extends BinExpr {

    /** Default constructor.
     */
    public BinLogicExpr(Position pos, Expr left, Expr right) {
        super(pos, left, right);
    }

    /** Run type checking analysis on this expression.  The typing
     *  parameter provides access to the type analysis phase (in
     *  particular, to the associated error handler).
     */
    public Type analyze(TypeAnalysis typing) {  // LAnd, LOr
        left.require(typing, Type.BOOLEAN);
        right.require(typing, Type.BOOLEAN);
        return type = Type.BOOLEAN;
    }

    /** Run initialization analysis on this expression.
     * Boolean logic expressions are a special case because
     * they short circuit, so only the left-hand side will
     * always evaluate an initialization.
     * */
    public VarSet analyze(InitAnalysis init, VarSet initialized) {
        return left.analyze(init, initialized);
    }
}

