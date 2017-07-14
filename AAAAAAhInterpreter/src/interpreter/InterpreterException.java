/** 
* @author 作者 E-mail: 
* @version 创建时间：2017年7月11日 下午9:19:29 
* 类说明 
*/
package interpreter;

public class InterpreterException extends Exception {

	private static final long serialVersionUID = 2044145777646245991L;
	String errStr;
	
	public InterpreterException(String str){
		this.errStr = str;
	}
	
	public String toString(){
		return errStr;
	}
}

