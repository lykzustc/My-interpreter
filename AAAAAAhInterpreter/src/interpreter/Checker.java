/** 
* @author 作者 E-mail: 
* @version 创建时间：2017年7月11日 下午4:36:27 
* 类说明 
*/
package interpreter;

import java.util.ArrayList;
import java.util.Stack;

import token.Token;
import token.TokenTag;


public class Checker {
	//used in loop
	int i;
	int j;
	int m;	
	// k is the number of relational operators in your condition expression
	int k;
	
	Stack<Token> theStack;
	Stack<Token> theStack1;
	Stack<Token> theStack2;
	Stack<Token> theStack3;
	ArrayList<Token> tokens;
	public Checker( ArrayList<Token> tokens){
		this.tokens = tokens;		
	}
	
	public int checkParenthesis(){
		this.theStack = new Stack<>();
		for ( i = 0; i <= tokens.size()-1; i++){
			if (tokens.get(i).getTag().equals(TokenTag.LPARA)){
				theStack.push(tokens.get(i));
			}
			else if (tokens.get(i).getTag().equals(TokenTag.RPARA)){
				if (!theStack.isEmpty()){
					theStack.pop();
				}else{
					return 1;
				}
			}

		}
		if (!theStack.isEmpty()){
				return 0;
		}
		return 99;
	}
	
	public int checkIfEndif(){
		this.theStack1 = new Stack<>();		
		for (i = 0; i <= tokens.size()-1; i++){
			if(tokens.get(i).getTag().equals(TokenTag.IF)){
				theStack1.push(tokens.get(i));
			} else if ( tokens.get(i).getTag().equals(TokenTag.ENDIF)){
				if (!theStack1.isEmpty()){
					theStack1.pop();					
				}
				else{
					return 3;
				}
			}
		}
		if (!theStack1.isEmpty()){
			return 2;
		}		
		return 99;
	}
	
	int checkElse(){
		this.theStack2 = new Stack<>();
		for (j = 0; j <= tokens.size()-1; j++){
			if(tokens.get(j).getTag().equals(TokenTag.ELSE)){
				theStack2.push(tokens.get(j));
			} else if (tokens.get(j).getTag().equals(TokenTag.ENDIF)){
				if (!theStack2.isEmpty()){
					theStack2.pop();	
				}
			}
		}
		
		if (!theStack2.isEmpty()){
			return 4;
		}
		return 99;
	}
	
	int checkRop (){
		k = 0;
		for (Token t :tokens){
			if(t.getTag().equals(TokenTag.GT) || t.getTag().equals(TokenTag.LT)||
					t.getTag().equals(TokenTag.EQ)){
				k++;
			}
		}

		if (k == 0){
			return 6;
		} else if (k > 1){
			return 7;
		}else{
			return 99;
		}
	}
	
	int checkMissingExpression(){
		
		if (tokens.isEmpty()){
			return 8;
		}else{
			return 99;
		}

	}
	
	int checkMisSurroundedBrackets(){
		this.theStack3 = new Stack<>();
		for(m = 0; m <= tokens.size()-1; m++){
			if (tokens.get(m).getTag().equals(TokenTag.LPARA)){
				theStack3.push(tokens.get(m));
			}else if(tokens.get(m).getTag().equals(TokenTag.RPARA)){
				if (!theStack3.isEmpty()){
					theStack3.pop();					
				}
			}else if(tokens.get(m).getTag().equals(TokenTag.LT) ||
					tokens.get(m).getTag().equals(TokenTag.GT)){
				break;
			}
		}
		if (!theStack3.isEmpty()){
			return 5;
		} else{
			return 99;	
		}
	}
}

