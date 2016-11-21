package wzh.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicNameValuePair;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**For test
 * @author Shawn Peng
 *
 */
public class main {

	public static void main(String[] args) {
        //发送 GET 请求
        //String s=HttpRequest.sendGet("http://localhost:6144/Home/RequestString", "key=123&v=456");
        //System.out.println(s);
        
        //发送 POST 请求
        //String sr=HttpRequest.sendPost("http://localhost:6144/Home/RequestPostString", "key=123&v=456");
        //System.out.println(sr);
		
//		String sr=HttpRequest.sendPost("https://arms3.onezero.com/login_check", "_csrf_token=vtgcJSpa_Wi6l_WHHRhv-QZ5rUhDUUXQhiCn-KRBZc0&_username=shawn.peng%40gmimarkets.com&_password=456456&_submit=");
//        System.out.println(sr);
        
////        String s=HttpRequest.sendGet("https://arms3.onezero.com/login", "");
//        String html=HttpRequest.sendGet("https://arms3.onezero.com", "");
////		System.out.println(s);
//		
//        
////        String s=HttpRequest.sendGet("http://www.baidu.com", "");
////        System.out.println(s);
//        
//		
//		
//        
//        System.out.println();
//        
////		try {
////			Parser parser=new Parser(html);
////			HasAttributeFilter filter = new HasAttributeFilter("id","_submit");
////			NodeList nodeList=parser.extractAllNodesThatMatch(filter);
////			
//////			TagNode tag = (TagNode)nodeList.elementAt(0);
////			System.out.println(nodeList.elementAt(0).getNextSibling().getText());
////			
////		} catch (ParserException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		
//		
//		//String html = "<html><head><title>First parse</title></head>" + "<body><p>Parsed HTML into a doc.</p></body></html>";
//		Document doc = Jsoup.parse(html);
//		Element ele=doc.getElementsByAttributeValue("type", "submit").first();
//		ele=ele.parent().parent();
//		System.out.println("ownText = " + ele.ownText());
//		System.out.println("text = " + ele.text());
//		
//		
//        
////        String s=HttpRequest.sendGet("https://www.baidu.com/s", "ie=utf-8&f=8&rsv_bp=0&rsv_idx=1&tn=baidu&wd=aaa&rsv_pq=fb3fd7960002d255&rsv_t=e005igCXF8ZJEy8ehCuX4OvIL2qLVepa0FoRFjmO2gfjWx14cIyYkRndI5g&rqlang=cn&rsv_enter=0&rsv_sug3=4&rsv_sug1=1&rsv_sug7=100&inputT=1273&rsv_sug4=1512");
////		System.out.println(s);
//		
		String line = "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
		System.out.println(line);
		Scanner input = new Scanner(System.in);
		
		try {			
			String text;
			Document document;
			Elements elements;
			Element element;
			HttpResponse httpResponse;
			Response response;
			
			
			// 拿登陆使用的_csrf_token
			text = Request.Get("https://arms3.onezero.com/login")
					.connectTimeout(20000)  
					.socketTimeout(20000)
//					.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
					.execute()
					.returnContent().asString(Consts.UTF_8);

			document = Jsoup.parse(text);
			elements = document.getElementsByAttributeValue("name", "_csrf_token");
			if(elements.isEmpty()){
				System.out.println("Failed to login: unable to find csrf token!");
				return;
			}
			element = elements.first();
			text = element.attr("value");
			System.out.println("csrf = " + text);
			System.out.println(line);			
			
			//设置登录参数
			System.out.println("Please input your ARMS3 password:");
			String pw = input.nextLine(); //从键盘读取密码
			List forms = Form.form()
					.add("_csrf_token", text)
					.add("_username", "shawn.peng@gmimarkets.com")
					.add("_password", pw)
					.add("_submit", "")
					.build();
			
//	        List<NameValuePair> forms = new ArrayList<NameValuePair>();
//	        forms.add(new BasicNameValuePair("_csrf_token", text));
//	        forms.add(new BasicNameValuePair("_username", "contoso@gmimarkets.com")); 
//	        forms.add(new BasicNameValuePair("_password","xxxx"));
//	        forms.add(new BasicNameValuePair("_submit", ""));
////	        UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(forms, "UTF-8");
			
			//登陆拿Cookie
	        response = Request.Post("https://arms3.onezero.com/login_check")  
			        .connectTimeout(20000)  
			        .socketTimeout(20000)			        
			        .bodyForm(forms, Consts.UTF_8)
			        .execute();
			
			//拿Cookie
			httpResponse=response.returnResponse();
			System.out.println(httpResponse.getStatusLine());			
			Header[] headers = httpResponse.getAllHeaders();			
			for (Header h : headers)
				System.out.println(h.getName() + " ===> " + h.getValue());
			
			System.out.println(line);
			//检查登录是否成功，即看redirect的地址有没有问题
			headers = httpResponse.getHeaders("location");
			if (headers[0].getValue() != "https://arms3.onezero.com/"){
				System.out.println("登陆失败，返回地址为： " + headers[0].getValue() + "\r\n请检查！") ;
				return;
			}
			//打印Cookie
			headers = httpResponse.getHeaders("Set-Cookie");
			text = headers[0].getName() + " ===> " + headers[0].getValue();
			
			System.out.println(text);
			System.out.println(line);
			
			String cookie = headers[0].getValue().split(";", 2)[0];
			System.out.println("Cookie ===> " + cookie);
			System.out.println(line);
			
			//登陆成功后要开始搞事情了：搜ae147这个groupPermission
			String queryPermissionGroup = "ae125";
			String search = queryPermissionGroup+"-2";
			response = Request.Get("https://arms3.onezero.com/broker/list-permission-groups?search=" + search)  
			        .connectTimeout(10000)  
			        .socketTimeout(10000)
			        .addHeader("Cookie", cookie)
//			        .addHeader("Cookie", "PHPSESSID=h6a4fu64v9ob9ffbf287d0di24")
			        .execute();
			        
//			text = response.returnResponse().getStatusLine().toString();
//			System.out.println(text + "\r\n" + line);
			text = response.returnContent().asString(Consts.UTF_8);
			
			System.out.println(text);
						
			//解析HTML，搜某个PermissionGroup的实际地址
			System.out.println(line);
			document = Jsoup.parse(text);
			elements=document.getElementsMatchingOwnText(search);
			if(!elements.isEmpty()){			
				element=elements.first();
				text = element.attributes().get("href");			
				System.out.println("地址： " + text);			
			}else{
				System.out.println("没找到" + search);
			}
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 关闭键盘输入
		input.close();
    }

}
