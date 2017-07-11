/** 
* @author 作者 E-mail: 
* @version 创建时间：2017年7月7日 上午9:33:17 
* this class returns result of a postfix expression  
*/
package interpreter;

import java.util.ArrayList;
import java.util.Stack;

import token.Token;
import token.TokenTag;
import interpreter.Parser;

public class DoEvaluate {
	


	private Stack<Integer> theStack;
	private ArrayList<Token> postfixtokens;
	
	public DoEvaluate(ArrayList<Token> postfixtokens){
		this.postfixtokens = postfixtokens;
	}
	
	
	public int evaluate(){
		theStack = new Stack<>();
		Token t;
		int j;
		int num1, num2, interAns;
		
		for (j=0; j<postfixtokens.size(); j++){
			t = postfixtokens.get(j);
			if(t.getTag().equals(TokenTag.INT) || t.getTag().equals(TokenTag.VARIABLE)){
				if(t.getTag().equals(TokenTag.INT)){
					theStack.push(Integer.parseInt(t.getValue()));
				}
				if(t.getTag().equals(TokenTag.VARIABLE)){
					if(Parser.variables.containsKey(t.getValue())){
						theStack.push(Parser.variables.get(t.getValue()));
					}
					else{
						//the default value for unassigned variable is 0;
						theStack.push(0);
					}
				}
			}
			else{
				num2 = theStack.pop();
				num1 = theStack.pop();
				
				switch(t.getTag()){
				case PLUS:
					interAns = num1 + num2;
					break;
				case MINUS:
					interAns = num1 - num2;
					break;
				case MULTIPLY:
					interAns = num1 * num2;
					break;
				case DIVIDE:
					interAns = num1 / num2;
					break;
				case MOD:
					interAns = num1 % num2;
					break;
				default:
					interAns = 0;
				}
				theStack.push(interAns);
			}
		}
		interAns = theStack.pop();
		
		return interAns;
	}
}

