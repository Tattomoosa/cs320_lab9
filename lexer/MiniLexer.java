package lexer;

import java.util.Hashtable;
import compiler.Source;
import compiler.SourceLexer;
import compiler.Handler;
import compiler.Warning;
import compiler.Failure;

/** A lexical analyzer.
 */
public class MiniLexer extends SourceLexer<MiniToken> {
    /** Construct a lexical analyzer.
     */
    public MiniLexer(Handler handler, Source source) {
        super(handler, source);
    }

    //- Main lexical analysis: ------------------------------------------------

    /** Read the next token and return the corresponding enum.
     */
    public MiniToken nextToken() {
        for (;;) {
            skipWhitespace();
            markPosition();
            switch (c) {
                case EOF  : return token=MiniToken.ENDINPUT;

                // Separators:
                case '('  : nextChar();
		            return token=MiniToken.OPAREN;
                case ')'  : nextChar();
                            return token=MiniToken.CPAREN;
                case '{'  : nextChar();
		            return token=MiniToken.OBRACE;
                case '}'  : nextChar();
		            return token=MiniToken.CBRACE;
                case ';'  : nextChar();
                            return token=MiniToken.SEMI;
                case ','  : nextChar();
                            return token=MiniToken.COMMA;

                // Operators:
                case '='  : nextChar();
                            if (c=='=') {
                                nextChar();
                                return token=MiniToken.EQEQ;
                            } else {
                                return token=MiniToken.EQ;
                            }

                case '!'  : nextChar();
                            if (c=='=') {
                                nextChar();
                                return token=MiniToken.NEQ;
                            } else {
                                return token=MiniToken.LNOT;
                            }

                case '<'  : nextChar();
                            if (c=='=') {
                                nextChar();
                                return token=MiniToken.LTE;
                            } else {
                                return token=MiniToken.LT;
                            }

                case '>'  : nextChar();
                            if (c=='=') {
                                nextChar();
                                return token=MiniToken.GTE;
                            } else {
                                return token=MiniToken.GT;
                            }

                case '&'  : nextChar();
                            if (c=='&') {
                                nextChar();
                                return token=MiniToken.LAND;
                            } else {
                                return token=MiniToken.BAND;
                            }

                case '|'  : nextChar();
                            if (c=='|') {
                                nextChar();
                                return token=MiniToken.LOR;
                            } else {
                                return token=MiniToken.BOR;
                            }

                case '^'  : nextChar();
                            return token=MiniToken.BXOR;

                case '~'  : nextChar();
                            return token=MiniToken.BNOT;

                case '+'  : nextChar();
		            return token=MiniToken.PLUS;

                case '-'  : nextChar();
                            return token=MiniToken.MINUS;

                case '*'  : nextChar();
                            return token=MiniToken.MUL;

                case '/'  : nextChar();
                            if (c=='/') {
                                skipOneLineComment();
                            } else if (c=='*') {
                                skipBracketComment();
                            } else {
                                return token = MiniToken.DIV;
                            }
                            continue;

                default   : if (Character.isJavaIdentifierStart((char)c)) {
                                return identifier();
                            } else if (Character.digit((char)c, 10)>=0) {
                                return number();
                            }
            }
            illegalCharacter();
            nextChar();
        }
    }

    //- Whitespace and comments -----------------------------------------------

    private boolean isWhitespace(int c) {
        return (c==' ') || (c=='\t') || (c=='\f');
    }

    private void skipWhitespace() {
        while (isWhitespace(c)) {
            nextChar();
        }
        while (c==EOL) {
            nextLine();
            while (isWhitespace(c)) {
                nextChar();
            }
        }
    }

    private void skipOneLineComment() { // Assumes c=='/'
        nextLine();
    }

    private void skipBracketComment() { // Assumes c=='*'
        nextChar();
        for (;;) {
            if (c=='*') {
                do {
                    nextChar();
                } while (c=='*');
                if (c=='/') {
                    nextChar();
                    return;
                }
            }
            if (c==EOF) {
                report(new Failure(getPos(), "Unterminated comment"));
                return;
            }
            if (c==EOL) {
                nextLine();
            } else {
                nextChar();
            }
        }
    }

    //- Identifiers, keywords, boolean and null literals ----------------------

    private MiniToken identifier() {          // Assumes isJavaIdentifierStart(c)
        int start = col;
        do {
            nextChar();
        } while (c!=EOF && Character.isJavaIdentifierPart((char)c));
        lexemeText = line.substring(start, col);

        MiniToken kw = reserved.get(lexemeText);
	return token = (kw!=null) ? kw : MiniToken.ID;
    }

    private static Hashtable<String, MiniToken> reserved;
    static {
        reserved = new Hashtable<String, MiniToken>();
        reserved.put("int",     MiniToken.INT);
        reserved.put("boolean", MiniToken.BOOLEAN);
        reserved.put("if",      MiniToken.IF);
        reserved.put("else",    MiniToken.ELSE);
        reserved.put("while",   MiniToken.WHILE);
        reserved.put("print",   MiniToken.PRINT);
        reserved.put("true",    MiniToken.TRUE);
        reserved.put("false",   MiniToken.FALSE);
    }

    //- Numeric integer literals ----------------------------------------------

    /** Records the numeric value of the most recently read integer literal.
     */
    protected int num = 0;

    /** Return the numeric value of the most recently read integer literal.
     */
    public int getNum() {
        return num;
    }

    /** Read an integer literal.
     */
    private MiniToken number() {              // Assumes c is a digit
        num   = 0;
        int d = Character.digit((char)c, 10);
        do {
            num = 10*num + d;
            nextChar();
            d = Character.digit((char)c, 10);
        } while (d>=0);
        return token=MiniToken.INTLIT;
    }

    //- Display token name (for debugging purposes) ---------------------------

    public String tokenName() {
	switch (token) {
	case ID:
	    return token.tokenName + ", " + lexemeText;
	case INTLIT:
	    return token.tokenName + ", " + num;
	default:
	    return token.tokenName;
	}
    }

    //- Error reporting: ------------------------------------------------------

    private void illegalCharacter() {
        report(new Warning(getPos(), "Ignoring illegal character"));
    }
}
