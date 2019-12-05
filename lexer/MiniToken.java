package lexer;

/** Tokens for the mini language. Each carries a human-readable tokenName field.
 */
public enum MiniToken {
    ENDINPUT("end of input"),
    OPAREN("open parenthesis"),
    CPAREN("close parenthesis"),
    OBRACE("open brace"),
    CBRACE("close brace"),
    SEMI("semicolon"),
    COMMA("comma"),
    EQ("= operator"),
    EQEQ("== operator"),
    NEQ("!= operator"),
    LT("< operator"),
    LTE("<= operator"),
    GT("> operator"),
    GTE(">= operator"),
    PLUS("+ operator"),
    MINUS("- operator"),
    MUL("* operator"),
    DIV("/ operator"),
    BAND("& operator"),
    BXOR("^ operator"),
    BOR("| operator"),
    LAND("&& operator"),
    LOR("|| operator"),
    LNOT("! operator"),
    BNOT("~ operator"),
    IF("if keyword"),
    ELSE("else keyword"),
    WHILE("while keyword"),
    PRINT("print keyword"),
    INT("int keyword"),
    BOOLEAN("boolean keyword"),
    ID("identifier"),            // lexeme carries identifier contents
    INTLIT("integer literal"),   // lexeme carries identifier contents
    TRUE("Boolean literal, true"),
    FALSE("Boolean literal, false")
    ;

    public final String tokenName;
    
    MiniToken(String tokenName) { this.tokenName = tokenName; }
}

