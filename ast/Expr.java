package ast;
import compiler.Failure;
import compiler.Position;

/** Abstract syntax for expressions.
 */
public abstract class Expr {

    protected Position pos;

    /** Default constructor.
     */
    public Expr(Position pos) {
        this.pos = pos;
    }

    /** Return a string describing the position/coordinates
     *  of this abstract syntax tree node.
     */
    String coordString() { return pos.coordString(); }

    /** Create an assignment with this expression as its left hand
     *  side.  This will raise an exception whenever the expression
     *  is not a valid left hand side.
     */
    public Expr assignTo(Position pos, Expr exp) throws Failure {
      throw new Failure(pos, "Invalid left hand side for assignment");
    }

    /** Print an indented description of this abstract syntax node,
     *  including a name for the node itself at the specified level
     *  of indentation, plus more deeply indented descriptions of
     *  any child nodes.
     */
    public abstract void indent(IndentOutput out, int n);

    /** Generate a pretty-printed description of this expression
     *  using the concrete syntax of the mini programming language.
     */
    public abstract void print(TextOutput out);

    /** Print out this expression, wrapping it in parentheses if the
     *  expression includes a binary or unary operand.
     */
    public void parenPrint(TextOutput out) {
        this.print(out);
    }

    /** Output a description of this node (with id n), including a
     *  link to its parent node (with id p) and returning the next
     *  available node id.
     */
    public int toDot(DotOutput dot, int p, String attr, int n) {
        dot.join(p, n, attr);
        return toDot(dot, n);
    }

    /** Output a description of this node (with id n) in dot format,
     *  adding an extra node for each subtree.
     */
    public abstract int toDot(DotOutput dot, int n);

    /** Output a dot description of this abstract syntax node
     *  using the specified label and id number.
     */
    protected int node(DotOutput dot, String lab, int n) {
        return dot.node(lab + "\\n" + pos.coordString(), Type.color(type), n);
    }

    /** Run scope analysis on this expression.  The scoping parameter
     *  provides access to the scope analysis phase (in particular,
     *  to the associated error handler), and the env parameter
     *  reflects the environment in which the expression is evaluated.
     *  Unlike scope analysis for statements, there is no return
     *  result here: an expression cannot introduce new variables in
     *  to a program, so the final environment will always be the same
     *  as the initial environment.
     */
    public abstract void analyze(ScopeAnalysis scoping, Env env);

    protected Type type = null;

    /** Run type checking analysis on this expression.  The typing
     *  parameter provides access to the type analysis phase (in
     *  particular, to the associated error handler).
     */
    public abstract Type analyze(TypeAnalysis typing);

    public void analyze(UseAnalysis use) {}

    /** Run the type checking analysis on an expression that is required to
     *  have the type specified by the expected parameter.
     */
    Type require(TypeAnalysis typing, Type expected) {
      Type t = analyze(typing);
      if (t!=expected) {
        typing.report(new Failure(pos, "An expression of type " + expected +
                                       " was expected"));
        return expected;
      }
      return t;
    }

    /** Run the type checking analysis on an expression that is required to
     *  have either the type specified by the expected parameter or else the
     *  type specified by the alternative parameter.
     */
    Type require(TypeAnalysis typing, Type expected, Type alternative) {
      Type t = analyze(typing);
      if (t!=expected && t!=alternative) {
        typing.report(new Failure(pos, "An expression of type " + expected +
                                       " was expected"));
        return expected;
      }
      return t;
    }

    /** Run initialization analysis on this expression.  The init parameter
     *  provides access to the initialization analysis phase (in particular,
     *  to the associated error handler), and the initialized parameter
     *  reflects the set of variables that are known to have been initialized
     *  before this expression is evaluated.  The return result is the set of
     *  variables that are known to be initialized after the expression has
     *  been evaluated.
     */
    public abstract VarSet analyze(InitAnalysis init, VarSet initialized);

    /** Rewrite this expression using algebraic identities to reduce
     *  the amount of computation that is required at runtime.  The
     *  algorithms used here implement a range of useful optimizations
     *  including, for example:
     *     x + 0  ==>  x
     *     n + m  ==>  (n+m)           if n,m are known integers
     *     (x + n) +m ==>  x + (n+m)   if n,m are known integers
     *  etc. with corresponding rules for *, &, |, and ^.  However,
     *  there are still plenty of other opportunities for simplification,
     *  including:
     *    use of identities/constant folding on Booleans
     *    removing double negations, complements, etc...
     *    distributivity properties, such as (x+n)+(y+m) ==> (x+y)+(n+m)
     *    and so on ...
     */
    abstract Expr simplify();

    /** Simplify an addition with a known integer as the right argument.
     */
    Expr simpAdd(Add orig, int m) { return newAdd(orig.pos, m); }

    /** Construct an abstract syntax tree for an addition with a known
     *  integer as the right argument.
     */
    Expr newAdd(Position pos, int n) {
        return (n==0) ? this : new Add(pos, this, new IntLit(pos, n));
    }

    /** Simplify a multiplication with a known integer as the right argument.
     */
    Expr simpMul(Mul orig, int m) { return newMul(orig.pos, m); }

    /** Construct an abstract syntax tree for a multiplication with a known
     *  integer as the right argument.
     */
    Expr newMul(Position pos, int n) {
        return (n==1) ? this                 // x * 1 == x
             : (n==0) ? new IntLit(pos, 0)   // x * 0 == 0
             : new Mul(pos, this, new IntLit(pos, n));
    }

    /** Simplify a bitwise and with a known integer as the right argument.
     */
    Expr simpBAnd(BAnd orig, int m) { return newBAnd(orig.pos, m); }

    /** Construct an abstract syntax tree for a bitwise and with a known
     *  integer as the right argument.
     */
    Expr newBAnd(Position pos, int n) {
        return (n==(-1)) ? this                // x & (-1) == x
             : (n==0)    ? new IntLit(pos, 0)  // x & 0    == 0
             : new BAnd(pos, this, new IntLit(pos, n));
    }

    /** Simplify a bitwise or with a known integer as the right argument.
     */
    Expr simpBOr(BOr orig, int m) { return newBOr(orig.pos, m); }

    /** Construct an abstract syntax tree for a bitwise or with a known
     *  integer as the right argument.
     */
    Expr newBOr(Position pos, int n) {
        return (n==(-1)) ? new IntLit(pos, -1) // x | (-1) == (-1)
             : (n==0)    ? this                // x | 0    == x
             : new BOr(pos, this, new IntLit(pos, n));
    }

    /** Simplify a bitwise xor with a known integer as the right argument.
     */
    Expr simpBXor(BXor orig, int m) { return newBXor(orig.pos, m); }

    /** Construct an abstract syntax tree for a bitwise xor with a known
     *  integer as the right argument.
     */
    Expr newBXor(Position pos, int n) {
        return (n==(-1)) ? new BNot(pos, this) // x ^ (-1) == ~x
             : (n==0)    ? this                // x ^ 0    == x
             : new BXor(pos, this, new IntLit(pos, n));
    }

    /** Test to see if this expression is an integer literal.
     */
    IntLit isIntLit() { return null; }

    /** Return the depth of this expression as a measure of how complicated
     *  the expression is / how many registers will be needed to evaluate it.
     */
    abstract int getDepth();

    /** Used as a depth value to indicate an expression that has a
     *  potential side effect, and hence requires order of evaluation
     *  to be preserved.  (The same depth value could, in theory, be
     *  produced as the depth of a stunningly complex but side-effect
     *  free expression; oh well, we'll just miss the attempt to
     *  minimize register usage in such (highly unlikely) cases. :-)
     */
    public static final int DEEP = 1000;

    /** Generate assembly language code for this expression that will
     *  evaluate the expression when it is executed and leave the result
     *  in the specified free register, preserving any lower numbered
     *  registers in the process.
     */
    public abstract void compileExpr(IA32Output a, int pushed, int free);

    /** Generate code that will evaluate this (boolean-valued) expression
     *  and jump to the specified label if the result is true.
     */
    void branchTrue(IA32Output a, int pushed, int free, String lab) {
        compileExpr(a, pushed, free);
        a.emit("orl", a.reg(free), a.reg(free));
        a.emit("jnz", lab);
    }

    /** Generate code that will evaluate this (boolean-valued) expression
     *  and jump to the specified label if the result is false.
     */
    void branchFalse(IA32Output a, int pushed, int free, String lab) {
        compileExpr(a, pushed, free);
        a.emit("orl", a.reg(free), a.reg(free));
        a.emit("jz", lab);
    }
}
