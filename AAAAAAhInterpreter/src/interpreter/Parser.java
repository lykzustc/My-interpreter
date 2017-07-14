/** 
* @author lyk
* @version 创建时间：2017年7月6日 下午2:39:16 
* 
*/
package interpreter;

import java.util.ArrayList;
import java.util.HashMap;

import token.Token;
import token.TokenTag;

import interpreter.InterpreterException;

public class Parser {
	
	// These are the types of errors
	
	//eg. (()
	final int REDUNDANTLP = 0;
	
	//eg. ())
	final int REDUNDANTRP = 1;
	
	//eg. if if else endif
	final int REDUNDANTIF = 2;
	
	//eg. if else endif endif
	final int REDUNDANTENDIF = 3;
	
	//eg. if else else endif
	final int REDUNDANTELSE = 4;
	
	//eg. if ((1+1)>1)
	final int MISSURROUNDEDBRACKETS = 5;
	
	//eg. if (1+1)+1	
	final int RELATIONALOPMISSING = 6;
	
	//eg. if 1+1>>1
	final int RELATIONALOPREDUNDANT = 7;
	
	//eg. if[no  condition expression]  endif or a = 
	final int MISSINGEXPRESSION = 8;
	
	//eg. a=b+1 with b unassigned or simply a or if a>0 with a unassigned
	final int UNINITIALIZEDVAR = 9;
	
	//this is in case we can't acquire a boolean value after IF.
	//it works with a guard
	final int INVALIDCONDITIONAFTERIF = 10;
	
	//eg. a = 1+3b
	final int OPERATORMISSING = 11;
	
	//eg. a=1+1+
	final int OPERATORREDUNDANT = 12;
	
	//this is in case we can't get an int from an expression
	//it works as a guard
	final int INVALIDEXPRESSION = 13;

	//We hope every check will return this value
	final int EverythingisOK = 99;
	
	/*illustration:
	 * when encountered IF statement:
	 * first check whether redundant if else endif
	 * then check whether there is a condition expression after IF
	 * then check whether redundant () eg. if (1+1)) ((1+1)
	 * then check whether miss or redundant ROP eg. if 1+1>>1 or if 1+1 1
	 * 		till now if ((1+1)>(1+1)) is accepted
	 * then check whether missurrounded, eg. if (1+1>1) or if 1(>)0  all redundant brackets
	 * last if we can't get a boolean from condition, simply report invalid condition after IF 
	 * 		this situation should never occur because all the above could give us a hint.
	 * 		However, we still define such an error in case some accidents.	
	 * 
	 * 
	 * when encountered expression(in our case an expression is like a+(b-c)/d, it only returns an int!):
	 * first check whether uninitialized variable eg. a = b+1 with b unassigned
	 * then check whether miss operators eg. a = 1+1 1
	 * then check whether redundant operators eg. a = 1+1+
	 * last if we can't calculate an int from the expression, simply report invalid expression
	 * 		this situation should never occur because all the above could give us a hint.
	 * 		However, we still define such an error in case some accidents.	
	 * */
	
	
	// this is the list of tokenized tokens
	public  ArrayList<Token> tokens;
	
	public static void addTokens( ArrayList<Token> tokens){
		tokens.addAll(tokens);
	}
	
	// this is the list contains only If Else Endif TokenObject and Token
	ArrayList<TO> IfElseEndifO = new ArrayList<>();
	ArrayList<Token> IfElseEndifT = new ArrayList<>();
		
	// this is the Pairs containing information about every corresponding IF ELSE ENDIF
	ArrayList<Pair> Pairs = new ArrayList<>();
		
	// this is a map of our variables, name -> Value.
	public static HashMap<String, Integer> variables = new HashMap<String, Integer>();
		
	//used for parsing the whole tokens
	int pos;
	
	//used for parsing if else endif
	int pos1;
	
	//stands for the position of IF
	int current1;
	
	//stands for the position of ELSE
	int current2;
	
	//stands for the corresponding index in Pairs for IF token
	int tmp1;
	
	//stands for the corresponding index in Pairs for ELSE token
	int tmp2;
	
	//used for temporary storage for current position in condition() method
	int tmp3;
	
	//stands for the trailing flag for IF token
	Boolean flag1;
	
	public Parser(ArrayList<Token> tmp){
		
		this.tokens = tmp;
		
		//produce the list contains only if else endif tokens
		for (int i =0; i<tmp.size(); i++){
			if (tmp.get(i).getTag().equals(TokenTag.IF)||
				tmp.get(i).getTag().equals(TokenTag.ELSE)||
				tmp.get(i).getTag().equals(TokenTag.ENDIF)){
				this.IfElseEndifO.add(new TO(tmp.get(i),i));
				this.IfElseEndifT.add(tmp.get(i));
			}					
		}
		
		try
		{
			pos1 = 0;
			parse1();		
		}
		catch(Exception e)
		{
			System.err.println ( "You've got unpaired IF statement!!!" );
			e.printStackTrace();
		}
		
		try{
			pos = 0;
			parse();
		}
		catch (Exception e){
			System.err.println ( "You've got syntax errors!!!" );
			e.printStackTrace();
		}				
	}
	
	/* produce the Pairs
	 * if else endif => a pair
	 * if endif => a pair
	 * */
	private void parse1() throws Exception{
					
		int f = new Checker(IfElseEndifT).checkIfEndif();
		if (f==REDUNDANTIF){
			handlerError(REDUNDANTIF);
		} else if (f==REDUNDANTENDIF){
			handlerError(REDUNDANTENDIF);
		} 
		
		if( REDUNDANTELSE == new Checker(IfElseEndifT).checkElse()){
			handlerError(REDUNDANTELSE);
		}
				
		while (pos1 < IfElseEndifO.size()){			

			    if (match1(TokenTag.ELSE,TokenTag.ENDIF)){
				
				TO EndifO = IfElseEndifO.remove(pos1-1);
				TO ElseO = IfElseEndifO.remove(pos1-2);
				TO IfO = IfElseEndifO.remove(pos1-3);
				Pairs.add(new Pair(IfO, ElseO, EndifO));
				pos1 = 0;
			}
			else{
				pos1++;
				continue;	
			}
		}
		
		if (!IfElseEndifO.isEmpty()){			
			pos1 = 0;
			while (pos1 < IfElseEndifO.size()){
				if (match1(TokenTag.IF, TokenTag.ENDIF)){
					TO EndifO = IfElseEndifO.remove(pos1-1);
					TO IfO = IfElseEndifO.remove(pos1-2);
					
					//For if endif pair, the endif pos serves as else pos and the endif pos itself is useless
					Pairs.add(new Pair(IfO, EndifO, EndifO));
				}	
			}
		}
	}
	
	// parse and execute statements
		private void parse() throws Exception{
			while ( pos < tokens.size()){
				if( match(TokenTag.OVER)){
					break;
				}
				else if ( match(TokenTag.VARIABLE, TokenTag.ASSIGN)){
					
					// this is the variable name
					String tmp = lookBack(2).getValue();
					
					// this is the expression value
					Integer value = calculate();
					
					new Assignment(tmp, value).execute();
				
				}
				else if (match(TokenTag.PRINT))
				{
					if (match(TokenTag.STRING)){
						pos--;
						new Print(evaluate()).execute();
					}else{
						new Print(calculate()).execute();
					}
				}
	 			else if (match(TokenTag.IF)){
					current1 = pos-1;
					
					//get the flag
					flag1 = condition();
					
					// find the pair contains this IF in Pairs
					for (int i = 0; i < Pairs.size(); i++){
						if (Pairs.get(i).Ifidx == current1){
							tmp1 = i;
						}
					}
					
					//set the flag into the pair
					Pairs.get(tmp1).condition = flag1;
					
					new IfStatement(Pairs.get(tmp1).condition, Pairs.get(tmp1).Elseidx).execute();
				}
	 			else if (match(TokenTag.ELSE)){
					current2 = pos-1;
					
					// find the pair contains this ELSE in Pairs
					for (int j = 0; j<Pairs.size(); j++){
						if (Pairs.get(j).Elseidx == current2){
							tmp2 = j;
						}
					}
					new ElseStatement(Pairs.get(tmp2).condition, Pairs.get(tmp2).Endifidx).execute();
				}
				else if (match(TokenTag.ENDIF)){
					//Do nothing
				}
				else if(match(TokenTag.VARIABLE)){
						handlerError(UNINITIALIZEDVAR);
				}
				else{
					//This serves a guard
					pos++;	
					System.err.println("Attention!!! You've entered invalid tokens");
					continue;
				}	 
			}
		}


			
//										内部类	
//
//
//
//

	// Pair for If flag else endif	
	class Pair{
		
		// the position of IF in tokens
		int Ifidx;
		
		// the position of ELSE in tokens
		int Elseidx;
		
		// the position of ENDIF in tokens
		int Endifidx;
		
		// the corresponding flag
		public Boolean condition;
		
		public Pair(TO IfO, TO ElseO, TO EndIfO) throws Exception{
			
			this.Ifidx = IfO.getIdx();
			this.Elseidx = ElseO.getIdx();
			this.Endifidx = EndIfO.getIdx();

		}

	}
	

	//Token Object, record  the token and its pos
	class TO{
		
		Token token;
		
		// the position in tokens
		int idx;
		
		public Token getToken() {
			return token;
		}
		public void setToken(Token token) {
			this.token = token;
		}
		public int getIdx() {
			return idx;
		}
		public void setIdx(int idx) {
			this.idx = idx;
		}
		public TO(Token token, int idx){
			this.token = token;
			this.idx = idx;
		}
	}
	
	
	public interface Statement
	{
		void execute();
	}
	
	class IfStatement implements Statement{
		private Boolean flag;
		private int Elseidx;

		
		public IfStatement(Boolean flag, int Elseidx){
			this.flag = flag;
			this.Elseidx = Elseidx;

		}
		
		public void execute(){
			if (this.flag){
				//Do nothing
			}
			else{
				pos = Elseidx+1;
			}
		};
	}
	
	class ElseStatement implements Statement{
		private boolean flag;
		private int Endifidx;
		public ElseStatement( Boolean flag, int Endifidx){
			this.flag = flag;
			this.Endifidx = Endifidx;
		}
		public void execute() {
				
			if (this.flag){
				pos = Endifidx + 1;
			}else{
				//Do nothing
			}					
		}
	}
	
	class Assignment implements Statement
	{
		private String variable_name;
		private Integer value;
		public Assignment ( String v, Integer value )
		{
			this.variable_name = v;
			this.value = value;
		}
		
		public void execute()
		{	
			// Calculate the int and assign it to the variable
			variables.put(variable_name.toLowerCase(), value );
		}
	}
	
	public class Print implements Statement
	{
		// this could be just an int or a variable or a string!
		private Integer value=null;
		private String svalue=null;
		
		public Print ( Integer value ) 
		{
			this.value = value;
		}
		
		public Print ( String svalue ){
			this.svalue = svalue;			
		}
	
		public void execute()
		{
			// Print int
			if ( this.value != null){
			System.out.println ( value.toString() ); 
			}
			// Print string
			if ( this.svalue != null){
				System.out.println(svalue);
			}
		}
	}
	
	
	
	
	
	
	
//
//	
//										Util方法
//	
//	
//	

	//handle error
	public static void handlerError(int error) throws InterpreterException{
		String[] err = {
				"REDUNDANT LPARA!!!",
				"REDUNDANT RPARA!!!",
				"A IF STATEMENT MUST HAVE COMPLETE IF ELSE ENDIF, YOU'VE REDUNDANT IF!!!",
				"A IF STATEMENT MUST HAVE COMPLETE IF ELSE ENDIF, YOU'VE REDUNDANT ENDIF!!!",
				"A IF STATEMENT MUST HAVE COMPLETE IF ELSE ENDIF, YOU'VE REDUNDANT ELSE!!!",
				"YOU MAY HAVE SURROUND THE CONDITION OR THE ROP WITH BRACKETS,PELASE CHECK IT!!!",
				"Y'VE MISSED RELATIONAL OPERATOR!!!",
				"Y'VE MORE THAN ONE ROP, PLEASE USE ONLY ONE ROP!!!",
				"YOU'VE MISSED AN EXPRESSION, PLEASE CHECK IT!!!",
				"YOU'VE UNINITIALED VAR, PLEASE INITIALIZE IT!!!",
				"YOU'VE MISTAKES IN IF CONDITION, PLEASE CHECK IT!!!",
				"YOU'VE MISSED AN OPERATOR IN EXPRESSION, PELASE CHECK IT!!!",
				"YOU'VE REDUNDANT OPERATORS IN EXPRESSION, PELASE CHECK IT!!!",
				"YOU'VE MISTAKES WHEN CALCULATE, PLEASE CHECK THE EXPRESSION!!!"
		};
		
		throw new InterpreterException(err[error]);
	}
	

	//Get the flag after IF
	private Boolean condition() throws Exception{
		//used for store condition expression
		ArrayList<Token> conditionT = new ArrayList<>();
		
		tmp3 = pos;	
		
		Token tmpToken = null;
		int left = 0;
		int right = 0;
		
		if (match(TokenTag.TRUE)){
			return true;
		} else if (match(TokenTag.FALSE)){
			return false;
		}
			
		while ( match(TokenTag.PLUS) || match(TokenTag.MINUS) ||
				 match(TokenTag.MULTIPLY) || match(TokenTag.DIVIDE) ||
				 match(TokenTag.MOD)|| match(TokenTag.LPARA)||
				 match(TokenTag.RPARA)|| match(TokenTag.INT) ||
				 match(TokenTag.VARIABLE)|| match(TokenTag.GT) ||
				 match(TokenTag.LT) || match(TokenTag.EQ)||match(TokenTag.ASSIGN)){
			conditionT.add(lookBack(1));
		}
		
		Checker checker = new Checker(conditionT);
		
		// check whether there is an expression after IF 
		if (MISSINGEXPRESSION == checker.checkMissingExpression()){
			handlerError(MISSINGEXPRESSION);
		}
		
		// check whether the parenthesis balanced	
		if (REDUNDANTLP == checker.checkParenthesis()){
			handlerError(REDUNDANTLP);
		} else if (REDUNDANTRP == checker.checkParenthesis()){
			handlerError(REDUNDANTRP);
		}
		
		// check the number of relational operators
		if (RELATIONALOPMISSING == checker.checkRop()){
			handlerError(RELATIONALOPMISSING);
		} else if(RELATIONALOPREDUNDANT == checker.checkRop()){
			handlerError(RELATIONALOPREDUNDANT);
		}
		
		// check whether missurrounded
		if (MISSURROUNDEDBRACKETS == checker.checkMisSurroundedBrackets()){
			handlerError(MISSURROUNDEDBRACKETS);
		}
				
		pos = tmp3;
		
		
		try{
			//calculate left exp
			left = calculate();
		}catch (InterpreterException e){
			throw e;
		}catch (Exception e){
			handlerError(INVALIDCONDITIONAFTERIF);
		}
		
		try{
			//match rop
			if (match(TokenTag.GT) || match(TokenTag.LT)||match(TokenTag.EQ)){
				tmpToken = lookBack(1);			
			}
		}catch (Exception e){
			handlerError(INVALIDCONDITIONAFTERIF);
		}
		
		try{
			//calculate right exp
			right = calculate();
		}catch (InterpreterException e){
			throw e;
		}catch (Exception e){
			handlerError(INVALIDCONDITIONAFTERIF);
		}
		

		
		try{
			switch (tmpToken.getTag()){
			case GT:
				if (left>right){
					return true;
				}
				else{
					return false;
				}
				
			case LT:
				if (left<right){
					return true;
				}
				else{
					return false;
				}
			case EQ:
				if (left == right){
					return true;
				}else{
					return false;
				}
			default:
				break;
			}
		}catch (Exception e){
			handlerError(INVALIDCONDITIONAFTERIF);
		}
		return true;
		
	}
	
	
	//returns the forward token but don't change the position
	private Token lookAhead(int offset)
	{
		if ( (pos + offset) >= tokens.size() ){
			System.err.println ( "NOOOOOOOOOOOOOOO" );
			return null;
		}

		return tokens.get(pos + offset);
	}
	
	//returns the forward tokenObject but don't change the position
	private TO lookAhead1(int offset)
	{
		if ( (pos1 + offset) >= IfElseEndifO.size() ){
			System.out.println ( "NOOOOOOOOOOOOOOO" );
			return null;
		}

		return IfElseEndifO.get(pos1 + offset);
	}
	
	//returns the backward token but don't change the position
	private Token lookBack(int offset)
	{
		return tokens.get(pos - offset);
	}
	
	
	// if it's a match, then we move the parsing position forward
	// one index, now the position refers to the next.
	private boolean match (TokenTag tag )
	{
		if ( tokens.get(pos).getTag() != tag )
 			return false;
		
		pos++;
		return true;
	}
	
	// if it's a match, then we move the parsing position forward
	// two index
	private boolean match ( TokenTag t1, TokenTag t2)
	{
		if ( lookAhead(0).getTag() != t1 ) 
			return false;
		if ( lookAhead(1).getTag() != t2 )
			return false;

		pos += 2;
		return true;
	}
	
	
	// if it's a match, then we move the parsing position forward
	// two index
	private boolean match1 ( TokenTag t1, TokenTag t2){
		if ( lookAhead1(0).getToken().getTag()!=t1)
			return false;
		if ( lookAhead1(1).getToken().getTag()!=t2)
			return false;
		pos1 += 2;
		return true;
	}
	
	
	//calculate the result
	private Integer calculate() throws Exception {
		int result = 0;	
		
		ArrayList<Token> infixtokens = new ArrayList<>();
		ArrayList<Token> postfixtokens = new ArrayList<>();
		
		while ( match(TokenTag.PLUS) || match(TokenTag.MINUS) ||
				 match(TokenTag.MULTIPLY) || match(TokenTag.DIVIDE) ||
				 match(TokenTag.MOD)|| match(TokenTag.LPARA)||
				 match(TokenTag.RPARA)|| match(TokenTag.INT) ||
				 match(TokenTag.VARIABLE)){
			infixtokens.add(lookBack(1));
		}
		
		if (match(TokenTag.ASSIGN)){
			infixtokens.remove(infixtokens.size()-1);
			pos=pos-2;
		}
		
		if (MISSINGEXPRESSION == new Checker(infixtokens).checkMissingExpression()){
			handlerError(MISSINGEXPRESSION);
		}
				
		Checker checker = new Checker(infixtokens);
		if (checker.checkParenthesis()==0){
			handlerError(REDUNDANTLP);
		}
		
		if (checker.checkParenthesis()==1){
			handlerError(REDUNDANTRP);
		}
		
		SortUtil util = new SortUtil(infixtokens);
		postfixtokens = util.trans();
		
		
		DoEvaluate evaluater = new DoEvaluate(postfixtokens);
		

		try{
			result = evaluater.evaluate();
		}
		catch (InterpreterException e){
			throw e;
		}
		catch (Exception e){
			handlerError(INVALIDEXPRESSION);
		}
		return result;
	}
	
	private String evaluate(){
		if (match(TokenTag.STRING)){
			return lookBack(1).getValue();
		}else{
			return null;
		}		
	}
}

