package ast;
import compiler.Failure;
import compiler.Position;

/** Abstract syntax for if-then-else statements.
 */
public class If extends PosStmt {

    /** The test expression.
     */
    private Expr test;

    /** The true branch.
     */
    private Stmt ifTrue;

    /** The false branch.
     */
    private Stmt ifFalse;

    /** Default constructor.
     */
    public If(Position pos, Expr test, Stmt ifTrue, Stmt ifFalse) {
        super(pos);
        this.test = test;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    /** Print an indented description of this abstract syntax node,
     *  including a name for the node itself at the specified level
     *  of indentation, plus more deeply indented descriptions of
     *  any child nodes.
     */
    public void indent(IndentOutput out, int n) {
        out.indent(n, "If");
        test.indent(out, n+1);
        ifTrue.indent(out, n+1);
        ifFalse.indent(out, n+1);
    }

    /** Generate a pretty-printed description of this abstract syntax
     *  node using the concrete syntax of the mini programming language.
     */
    public void print(TextOutput out, int n) {
        out.indent(n);
        out.print("if (");
        test.print(out);
        out.print(")");
        ifTrue.printThenElse(out, n, ifFalse);
    }

    /** Output a description of this node (with id n) in dot format,
     *  adding an extra node for each subtree.
     */
    public int toDot(DotOutput dot, int n) {
        return ifFalse.toDot(dot, n, "ifFalse",
               ifTrue.toDot(dot, n, "ifTrue",
               test.toDot(dot, n, "test",
               node(dot, "If", n))));
    }

    /** Run scope analysis on this statement.  The scoping parameter
     *  provides access to the scope analysis phase (in particular,
     *  to the associated error handler), and the env parameter
     *  reflects the environment at the start of the statement.  The
     *  return result is the environment at the end of the statement.
     */
    public Env analyze(ScopeAnalysis scoping, Env env) {
        test.analyze(scoping, env);
        ifTrue.analyze(scoping, env);
        ifFalse.analyze(scoping, env);
        return env;
    }

    /** Generate a dot description for the environment structure of this
     *  program.
     */
    public void dotEnv(DotEnvOutput dot) {
        ifTrue.dotEnv(dot);
        ifFalse.dotEnv(dot);
    }

    /** Run type checker on this statement.  The typing parameter
     *  provides access to the type analysis phase (specifically,
     *  to the associated error handler).
     */
    public void analyze(TypeAnalysis typing) {
	test.require(typing, Type.BOOLEAN);
        ifTrue.analyze(typing);
        ifFalse.analyze(typing);
    }

    public void analyze(UseAnalysis use) {
        ifTrue.analyze(use);
        ifFalse.analyze(use);
    }

    /** Run initialization analysis on this statement.  The init
     *  parameter provides access to an initialization analysis phase
     *  object (specifically, to an associated error handler).  The
     *  initialized parameter is the set of variables (each represented
     *  by pointers to environment entries) that have definitely been
     *  initialized before this statement is executed.
     */
    public VarSet analyze(InitAnalysis init, VarSet initialized) {
        initialized = test.analyze(init, initialized);
        VarSet tis  = ifTrue.analyze(init, initialized);
        VarSet fis  = ifFalse.analyze(init, initialized);
        return VarSet.union(VarSet.intersect(VarSet.trim(tis, initialized),
                                             VarSet.trim(fis, initialized)),
                            initialized);
    }

    /** Attempt to simplify all of the expressions in this statement.
     */
    public void simplify() {
        test = test.simplify();
        ifTrue.simplify();
        ifFalse.simplify();
    }

    /** Generate code for executing this statement.
     */
    public void compile(IA32Output a, int pushed) {
        String lab1 = a.newLabel();
        String lab2 = a.newLabel();
        test.branchFalse(a, pushed, 0, lab1);
        ifTrue.compile(a, pushed);
        a.emit("jmp", lab2);
        a.emitLabel(lab1);
        ifFalse.compile(a, pushed);
        a.emitLabel(lab2);
    }
}
