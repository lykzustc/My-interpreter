package token;

/*
 This defines all the keywords/token tags for our tokens.
 */
public enum TokenTag
{
	NONE,	// this really isn't a token or tag, it's used for parsing
	PRINT, 
	LPARA, RPARA,
	IF, ELSE,ENDIF,GT, LT,EQ,
	OP, PLUS, MINUS, MULTIPLY, DIVIDE, MOD,
	TRUE, FALSE, ASSIGN,
	INT, STRING, VARIABLE,DELIMITER,
	OVER
}
