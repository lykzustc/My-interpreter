/** 
* @author 作者 E-mail: 
* @version 创建时间：2017年7月4日 下午3:48:15 
* 类说明 
*/
package interpreter;

import java.io.IOException;
import java.util.Scanner;

public class Test {
	/**
	 * @param args
	 * @throws IOException
	 * @throws Exception
	 */
	public static void main(String[] args) throws IOException, Exception {
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter your code and enter RUN to run it."
				+ " Enter exit to stop the programme");
//		StringBuilder bl=new StringBuilder("");
//		String s = "";
		
		Boolean flag = true;
		while (flag){
			
			StringBuilder bl=new StringBuilder("");
			String s = "";
			while(sc.hasNext()){
				
			s=sc.nextLine();
			bl.append(s+" ");
			if(s.equalsIgnoreCase("RUN")){
				String rs = bl.toString();		
				System.out.println("---我是输入的字符串----"+rs+"--------");		
				@SuppressWarnings("unused")
				TheScan scanner=new TheScan(rs);
				break;
			}
			if(s.equalsIgnoreCase("exit")){
				flag = false;
				break;
			}
			
			}

		}
		
//		while(sc.hasNext()){
//			s=sc.nextLine();
//			bl.append(s+" ");
//			if(s.equalsIgnoreCase("over")){
//				break;
//			}
//		}
//		String rs = bl.toString();		
//		System.out.println("---我是输入的字符串----"+rs+"--------");		
//		@SuppressWarnings("unused")
//		TheScan scanner=new TheScan(rs);
	};
}

