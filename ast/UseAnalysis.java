package ast;
import compiler.Failure;
import compiler.Position;
import compiler.Handler;
import compiler.Phase;

/** Represents a static analysis phase that checks to make sure that all declared
 *  variables are used. 
 */
public class UseAnalysis extends Phase {

    /** Default constructor.
     */
    public UseAnalysis(Handler handler) {
        super(handler);
    }

    /** Run use analysis on the specified statement.
     */
    public void analyze(Stmt stmt)
      throws Failure {
	// fill in here
        if (getHandler().hasFailures()) {
            throw new Failure("Aborting: errors detected during use checking");
        }
    }
}
