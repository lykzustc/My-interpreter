/** 
* @author 作者 E-mail: 
* @version 创建时间：2017年7月4日 下午3:20:34 
* 类说明 
*/
package token;

/*
This class is the Token object, and is created/used by the scanner.  
*/
public class Token
{
	private String value;
	private TokenTag tag;

	public Token ( String text, TokenTag tag ) 
	{
		this.tag = tag;
		this.value = text;
	}

	public TokenTag getTag()
	{
		return tag;
	}

	public void setTag(TokenTag t)
	{
		this.tag = t;
	}

	public String getValue()
	{
		return value;
	}
}

