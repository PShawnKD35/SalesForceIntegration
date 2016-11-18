package wzh.http;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
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
		
		String line = "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
		System.out.println(line);
		
		
		
		String text;
		Response response;
		try {
			
			text = Request.Get("https://arms3.onezero.com/login")
			       .connectTimeout(1000)
			       .socketTimeout(1000)
//			       .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
			       .execute()
			       .returnContent().asString(Consts.UTF_8);
			
			Document doc = Jsoup.parse(text);
			Elements eles = doc.getElementsByAttributeValue("name", "_csrf_token");
			Element ele = eles.last();
			text = ele.attr("value");
			System.out.println("csrf = " + text);
			System.out.println(line);
			
			
			response = Request.Post("https://arms3.onezero.com/login_check")  
			        .connectTimeout(1000)  
			        .socketTimeout(1000)			        
			        .addHeader("_csrf_token", text)
			        .addHeader("_username", "shawn.peng@gmimarkets.com")
			        .addHeader("_password","456456")
			        .addHeader("_submit", "")
			        .execute();
			
			HttpResponse hr=response.returnResponse();
			System.out.println(hr.getStatusLine());			
			Header[] hs = hr.getAllHeaders();
			for(Header h:hs)
				System.out.println(h.getName()	+ " ===> " + h.getValue());				
			
			System.out.println(line);
			hs = hr.getHeaders("Set-Cookie");
			text = hs[0].getName() + " ===> " + hs[0].getValue();
			
			System.out.println(text);
			System.out.println(line);
			
			String cookie = hs[0].getValue().split(";", 2)[0];
			System.out.println("Cookie ===> " + cookie);
			System.out.println(line);
			
			Request rq = Request.Get("https://arms3.onezero.com/login")  
			        .connectTimeout(1000)  
			        .socketTimeout(1000)
			        .addHeader("Cookie", cookie);
			        
			text = rq
		        .execute()
		        .returnContent().asString(Consts.UTF_8);
			System.out.println(text);	
			        

			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
    }

}
