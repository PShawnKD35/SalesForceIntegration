package wzh.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** Salesforce.com 的自动化处理类
 * @arthur Shawn Peng
 */
public class salesforce {

	static final String line = "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
	static final String headLine ="Trader Login	EN Name	IB	AE	Notification	CCY	Deposit Amount	SF Case #	Promo	Source	Bank Account	Confirmed	Conv Rate	Actual Received	System Reference	Account Input Reference / Notes";
	static final String[] heads = headLine.split("\t");
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		createDepositCase(
//				readDepositDetails(
//						readInputLine("Please copy the deposit details from Excel and paste here as plain text in one line, columns splited by TAB")));
		
		createDepositCase(new HashMap<String, String>(20));

	}
	
	/**
	 * 从键盘读一行字符串
	 * @param 告诉用户需要输入什么
	 * @return 读取到的字符串
	 */
	public static String readInputLine(String note) {
		System.out.println(line + "\r\n" + note + ":");
		Scanner input = new Scanner(System.in);
		String outputLine = input.nextLine(); //从键盘读取入金信息行
//		input.close();
		return outputLine;
	}
	
	/**
	 * 读取入金信息
	 * @param depositDetails 从excel表格中复制的入金账户行 字符串
	 * @return 和表头建立映射的map 
	 */
	public static Map readDepositDetails(String depositDetails){
		System.out.println(line);
		Map<String, String> depositDetailsMap = new HashMap<String, String>(20);
		String[] values = depositDetails.split("\t");
		if (values.length == heads.length) {
			for (int i = 0; i < values.length; i++) {
				depositDetailsMap.put(heads[i], values[i]);
				System.out.println(heads[i] + ":\t" + values[i]);
			}
		}else{
			System.out.println("入金账户信息有误，字段数与表头不符！"
					+ "\r\n表头数量: " + heads.length
					+ "\r\n字段数量: " + values.length
					+ "\r\n表头信息为: " + headLine
					+ "\r\n请检查后重新输入。");
			return null;
		}
		return depositDetailsMap;
	}
	
	/**
	 * 根据用户入金情况建立/修改 Salesforce 的case
	 * @param depositDetailsMap
	 */
	public static void createDepositCase(Map depositDetailsMap) {
		System.out.println(line);
		if (depositDetailsMap == null) return;
		System.out.println(depositDetailsMap.get("Trader Login"));
				
		try {
			String text;
			String cookie;
			Document document;
			Elements elements;
			Element element;
			HttpResponse httpResponse;
			Response response;
			
			// 拿登陆使用的cookie
			httpResponse = Request.Get("https://gmi.my.salesforce.com/")
					.connectTimeout(20000)  
					.socketTimeout(20000)
//				.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
					.execute()
					.returnResponse();
			
			//查看所有header			
			System.out.println(httpResponse.getStatusLine());			
			Header[] headers = httpResponse.getAllHeaders();			
			for (Header h : headers)
				System.out.println(h.getName() + " ===> " + h.getValue());			
			System.out.println(line);
//			//检查登录是否成功，即看redirect的地址有没有问题
//			headers = httpResponse.getHeaders("location");
//			System.out.println("location: " + headers[0].getValue());
			
			//打印Cookie
			headers = httpResponse.getHeaders("Set-Cookie");			
			if (headers.length <= 0) {
				System.out.println("未获得cookie有误，请核实!");
				return;			
			}else{
				text = headers[0].getValue();
				System.out.println("Set-Cookie: "+text);
				System.out.println(line);
				cookie = text.split(";", 2)[0]; // 拿BrowserID
				System.out.println(cookie);
			}
			
			//设置登录参数
			String pw = readInputLine("Please input your Salesforce password"); //从键盘读取密码
			List forms = Form.form()
					.add("un", "shawn.peng@gmimarkets.com")
					.add("hasRememberUn", "true")
					.add("useSecure", "true")
					.add("username", "shawn.peng@gmimarkets.com")
					.add("pw", pw)
					.add("Login", "登录")
					.build();
			
//			cookie = "BrowserId=Pi2EjrVTQkSI-sAjoTXXgQ";
			//登陆拿Cookie		
	        httpResponse = Request.Post("https://gmi.my.salesforce.com/")  
			        .connectTimeout(20000)
			        .socketTimeout(20000)
			        .addHeader("Cookie", cookie)
			        .bodyForm(forms, Consts.UTF_8)
			        .execute()
			        .returnResponse();
			
			//拿Cookie
			System.out.println(httpResponse.getStatusLine());			
			headers = httpResponse.getAllHeaders();			
			for (Header h : headers)
				System.out.println(h.getName() + " ===> " + h.getValue());			
			System.out.println(line);
			//看Entity
			text = EntityUtils.toString(httpResponse.getEntity(), Consts.UTF_8);
			System.out.println(text);
			//检查登录是否成功，即看redirect的地址有没有问题			
			headers = httpResponse.getHeaders("location");
			System.out.println("location: " + headers[0].getValue());
//			if (headers[0].getValue().split("?", 2)[0] != "https://gmi.my.salesforce.com/secur/frontdoor.jsp"){
//				System.out.println("登陆失败，返回地址为： " + headers[0].getValue() + "\r\n请检查！") ;
//				return;
//			}
			//打印Cookie
			headers = httpResponse.getHeaders("Set-Cookie");
			for(Header h:headers){
				text = h.getName() + " ===> " + h.getValue();
				System.out.println(text);
				cookie = cookie + ": " + h.getValue().split(";", 2)[0];
				if (headers.length != 5) {
					System.out.println("cookie个数有误，登陆可能未成功，请核实!");
//					return;
				}			
			}
			System.out.println(line);			
			System.out.println("Set-Cookie: " + cookie);
			System.out.println(line);
			
			
			//登陆成功后要开始搞事情了
			response = Request.Get("https://gmi.my.salesforce.com/_ui/core/chatter/ui/ChatterPage")  
			        .connectTimeout(10000)  
			        .socketTimeout(10000)
			        .addHeader("Cookie", cookie)
			        .execute();
			        
//			text = response.returnResponse().getStatusLine().toString();
//			System.out.println(text + "\r\n" + line);
			text = response.returnContent().asString(Consts.UTF_8);
			
			System.out.println(text);
			
			//退出登录
			httpResponse = Request.Get("https://gmi.my.salesforce.com/secur/logout.jsp")
			            .addHeader("Cookie", cookie)
			            .execute()
			            .returnResponse();
			
			//查看headers
			System.out.println(httpResponse.getStatusLine());			
			headers = httpResponse.getAllHeaders();			
			for (Header h : headers)
				System.out.println(h.getName() + " ===> " + h.getValue());			
			System.out.println(line);
			text = EntityUtils.toString(httpResponse.getEntity(), Consts.UTF_8);
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
