/** 
* @author 作者 E-mail: 
* @version 创建时间：2017年7月6日 下午2:39:16 
* 类说明 
*/
package interpreter;

import java.util.ArrayList;
import java.util.HashMap;

import token.Token;
import token.TokenTag;

public class Parser {
	// this is the list of tokenized tokens
	ArrayList<Token> tokens;
	
	// this is the list contains only If Else Endif Over TokenObject
	ArrayList<TO> IfElseEndifO = new ArrayList<>();	
	
	// this is the Pairs containing information about every corresponding IF ELSE ENDIF
	ArrayList<Pair> Pairs = new ArrayList<>();
		
	// this is a map of our variables, name -> Value.
	public static HashMap<String, Integer> variables = new HashMap<String, Integer>();
	
	// this is used for IF and Else statements
	public ArrayList<Statement> statements = new ArrayList<Statement>();
	
	//used for parsing the whole tokens
	int pos;
	
	//used for parsing if else endif over
	int pos1;
	
	//stands for the position of IF
	int current1;
	
	//stands for the position of ELSE
	int current2;
	
	//stands for the corresponding index in Pairs for IF token
	int tmp1;
	
	//stands for the corresponding index in Pairs for ELSE token
	int tmp2;
	
	//stands for the trailing flag for IF token
	Boolean flag1;

	
	public Parser(ArrayList<Token> tmp){
		
		this.tokens = tmp;
		for (int i =0; i<tmp.size(); i++){
			if (tmp.get(i).getTag().equals(TokenTag.IF)||
				tmp.get(i).getTag().equals(TokenTag.ELSE)||
				tmp.get(i).getTag().equals(TokenTag.ENDIF)||
				tmp.get(i).getTag().equals(TokenTag.OVER)){
				this.IfElseEndifO.add(new TO(tmp.get(i),i));
				if (tmp.get(i).getTag().equals(TokenTag.OVER)){
					break;
				}
			}					
		}
		
		try
		{
			pos1 = 0;
			parse1();
//			for ( Pair pair : Pairs){
//				System.out.println("我是IF ELSE ENDIF 的位置"+pair.Ifidx+"  "+pair.Elseidx+"  "+pair.Endifidx);
//			}		
		}
		catch(Exception e)
		{
			System.err.println ( "You've misenterd unpaired IF statement" );
			e.printStackTrace();
		}
		
		try{
			pos = 0;
			parse();
		}
		catch (Exception e){
			System.err.println ( "Exception in parsing" );
			e.printStackTrace();
		}

		// execute the statements!  This needs to be done this way because of the
		// jumps in execution; i.e. if and return and stuff
		//
//		pos = 0;
//		while ( pos < tokens.size())
//		{
//			
//			int current = pos;
//			if ( current >= statements.size() )
//				break;
//			
//			statements.get(current).execute();
//			
//			//printVars();
//			pos++;			
//		}				
	}
	
	// produce the Pairs
	private void parse1() throws Exception{
		while (pos1 < IfElseEndifO.size()){
			if (match1(TokenTag.OVER)){
				break;
			}
			else if (match1(TokenTag.ELSE,TokenTag.ENDIF)){
				
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
				
				Integer value = calculate();
				
				new Assignment(tmp, value).execute();
				
//				statements.add ( new Assignment(tmp, value) );
//				System.out.println("Assignment statement added");				
			}
			else if (match(TokenTag.PRINT))
			{
//				statements.add(new Print(calculate()));
//				System.out.println("PRINT statement added");
				if (match(TokenTag.STRING)){
					new Print(lookBack(1).getValue()).execute();
				}else{
					new Print(calculate()).execute();
				}
			}
 			else if (match(TokenTag.IF)){
				current1 = pos-1;
				if(match(TokenTag.TRUE)){
					flag1 = true;
				}else if(match (TokenTag.FALSE)){
					flag1 = false;
				}else{
					flag1 = condition();
				}
				for (int i = 0; i < Pairs.size(); i++){
					if (Pairs.get(i).Ifidx == current1){
						tmp1 = i;
					}
				}
				Pairs.get(tmp1).condition = flag1;
				statements.add(new IfStatement(Pairs.get(tmp1).condition, Pairs.get(tmp1).Elseidx));
				statements.remove(0).execute();
			}
 			else if (match(TokenTag.ELSE)){
				current2 = pos-1;
				//System.out.println("current2的值是"+current2);
				
				for (int j = 0; j<Pairs.size(); j++){
					if (Pairs.get(j).Elseidx == current2){
						tmp2 = j;
					}
				}
				statements.add(new ElseStatement(Pairs.get(tmp2).condition, Pairs.get(tmp2).Endifidx));
				statements.remove(0).execute();
				
//				for (Pair pair : Pairs){
//					if (pair.Elseidx == current2){
//						statements.add(new ElseStatement(pair.condition, pair.Endifidx));
//						statements.remove(0).execute();
//					}
//				}
			}
			else if (match(TokenTag.ENDIF)){
				//pos++;
			}
			else{
				pos++;	
				System.out.println("Attention!!! You've entered invalid tokens");
				continue;
			}	 
		}
	}
	
	
	
	
	
//										内部类	
//
//
//
//
//	public class Variable{
//		private String var_name;
//		private Integer var_value;
//		
//		public Variable(String var){
//			this.var_name = var;
//		}
//		
//		public Integer evaluate(){
//			if(variables.containsKey(var_name.toLowerCase()))
//			{
//				this.var_value = variables.get(var_name.toLowerCase());
//				return variables.get(var_name.toLowerCase());
//			}
//			//the default value of a variable is 0
//			this.var_value = 0;
//			return 0;
//		}
//	}
	
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
				//pos++; not needed because already move forward
			}
			else{
				pos = Elseidx+1;
			}
			//System.out.println("IF statement executed...");
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
				//System.out.println("我的flag是"+this.flag);
				pos = Endifidx + 1;
			}else{
				//System.out.println("我的flag是"+this.flag);
				//pos++;
			}
			//System.out.println("Else statement executed...");						
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
			// evaluate the expression and assign it to the variable
			variables.put(variable_name.toLowerCase(), value );
			//System.out.println("Assignment statement executed...");
		}
	}
	
	public class Print implements Statement
	{
		// this could be just a int or a variable or something!
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
			// output!
			if ( this.value != null){
			System.out.println ( value.toString() ); 
			//System.out.println("print statement executed...");
			}
			
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

	//Get the flag after IF
	private Boolean condition() throws Exception{
		Token tmp = null;
		
		//计算左表达式的值
		int left = calculate();
		
		if (match(TokenTag.GT) || match(TokenTag.LT)){
			tmp = lookBack(1);			
		}
		else{
			System.out.println("missing logical operators");
		}
		int right = calculate();
		
		switch (tmp.getTag()){
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

		default:
			return true;
		}
	}
	
	
	//returns the forward token but don't change the position
	private Token lookAhead(int offset)
	{
		if ( (pos + offset) >= tokens.size() ){
			System.out.println ( "NOOOOOOOOOOOOOOO" );
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
	
//	private TO lookBack1(int offset){
//		return IfElseEndifO.get(pos1-offset);
//	}
	
//	private Token consume ( TokenTag tag )
//			throws Exception
//	{
//		if ( tokens.get(pos).getTag() != tag )
//			throw new Exception();
//		return tokens.get(pos++);
//	}
	
	
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
	// one index, now the position refers to the next.
	private boolean match1 ( TokenTag t1){
		if ( lookAhead1(0).getToken().getTag()!=t1)
			return false;
		pos1 ++;
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
		int result;	
		
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
		
		SortUtil util = new SortUtil(infixtokens);
		postfixtokens = util.trans();
		
//		
//		for (Token s:postfixtokens){
//			System.out.println ( s.getValue().toString() + " : " + s.getTag() );
//		}
		
		DoEvaluate evaluater = new DoEvaluate(postfixtokens);
		
		try{
			result = evaluater.evaluate();
			return result;
		}
		catch (Exception e){
			System.out.println("invalid expression, maybe unpaird parenthesis");
			e.printStackTrace();
		}
//		System.out.println("evaluater计算的结果是"+result);
		
		// it should never be reached
		return 0;
	}
	
	
}

