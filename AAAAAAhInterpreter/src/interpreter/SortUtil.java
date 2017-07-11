/** 
* @author 作者 E-mail: 
* @version 创建时间：2017年7月6日 下午9:59:46 
* this class translates the infix tokens into postfix tokens 
*/
package interpreter;

import java.util.ArrayList;
import java.util.Stack;

import token.Token;
import token.TokenTag;


public class SortUtil {
	
	private Stack<Token> theStack = new Stack<>();
	
	private ArrayList<Token> tmptokens = new ArrayList<>();
	private ArrayList<Token> postfixtokens = new ArrayList<>();
	
	public SortUtil(ArrayList<Token> infixtokens){
		this.tmptokens = infixtokens; 
		this.theStack = new Stack<Token>();
	}
	
	public ArrayList<Token> trans(){
		for(int j=0; j<tmptokens.size(); j++){
			
			Token tmp = tmptokens.get(j);
			switch(tmp.getTag()){
			case PLUS:
			case MINUS:
				gotOper(tmp,1);
				break;
			case MULTIPLY:
			case DIVIDE:
			case MOD:
				gotOper(tmp,2);
				break;
			case LPARA:
				theStack.push(tmp);
				break;
			case RPARA:
				gotParen(tmp);
				break;
			default:
				postfixtokens.add(tmp);							
			}
		}
		
		while(!theStack.isEmpty()){
			postfixtokens.add(theStack.pop());
		}
		return postfixtokens;
	}
	
	public void gotOper(Token opThis, int prec1){
		while (!theStack.isEmpty()){
			Token opTop = theStack.pop();
			if( opTop.getTag().equals(TokenTag.LPARA)){
				theStack.push(opTop);
				break;
			}
			else{
				int prec2;
				
				if (opTop.getTag().equals(TokenTag.PLUS)){
					prec2 = 1;
				}
				else{
					prec2 = 2;
				}
				if (prec2 < prec1){
					theStack.push(opTop);
					break;
				}
				else{
					postfixtokens.add(opTop);
				}
			}
		}
		theStack.push(opThis);
	}
	
	public void gotParen(Token t){
		while(!theStack.isEmpty()){
			Token tx = theStack.pop();
			if( tx.getTag().equals(TokenTag.LPARA)){
				break;
			}
			else{
				postfixtokens.add(tx);
			}
		}
	}
}

