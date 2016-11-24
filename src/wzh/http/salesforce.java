package wzh.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
		
		createDepositCase(
				readDepositDetails(
						readInputLine("Please copy the deposit details from Excel and paste here as plain text in one line, columns splited by TAB")));
		
//		createDepositCase(new HashMap<String, String>(20));

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
	public static Map<String, String> readDepositDetails(String depositDetails){
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
	public static void createDepositCase(Map<String, String> depositDetailsMap) {
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
			List<NameValuePair> forms;
			Header[] headers;
			
			// 拿登陆使用的cookie
			httpResponse = Request.Get("https://gmi.my.salesforce.com/")
					.connectTimeout(20000)  
					.socketTimeout(20000)
//				.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
					.execute()
					.returnResponse();
			
			//查看所有header			
			System.out.println(httpResponse.getStatusLine());			
			headers = httpResponse.getAllHeaders();			
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
			forms = Form.form()
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
			System.out.println("☆☆☆☆☆☆☆☆☆☆☆ 登陆成功！☆☆☆☆☆☆☆☆☆☆☆");
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
				cookie = cookie + "; " + h.getValue().split(";", 2)[0];
				if (headers.length != 5) {
					System.out.println("cookie个数有误，登陆可能未成功，请核实!");
//					return;
				}			
			}
			System.out.println(line);			
			System.out.println("Set-Cookie: " + cookie);
			System.out.println(line);
			
//			cookie = "BrowserId=bMnWukcOSNivcwsugMHDCw; inst=APP_6F; rememberUn=false; login=; com.salesforce.LocaleInfo=us; oinfo=\"c3RhdHVzPUFDVElWRSZ0eXBlPTImb2lkPTAwRDkwMDAwMDAwWmNtVA==\"";			
			// 登陆成功后要开始搞事情了
			response = Request.Get("https://gmi.my.salesforce.com/_ui/core/chatter/ui/ChatterPage")  
			        .connectTimeout(10000)  
			        .socketTimeout(10000)
			        .addHeader("Cookie", cookie)
			        .execute();
			        
//			text = response.returnResponse().getStatusLine().toString();
//			System.out.println(text + "\r\n" + line);
			text = response.returnContent().asString(Consts.UTF_8);
			
			System.out.println(text);
			System.out.println(line);
			
			// 拿entityID, sysMod, 和ConfirmationToken
			String caseUrl = "https://gmi.my.salesforce.com?ec=302&startURL=%2F5006F00001IA7KE";
			text = Request.Get(caseUrl)
					.addHeader("Cookie", cookie)
					.execute()
					.returnContent().asString(Consts.UTF_8);
			
			System.out.println("拿case信息\r\n" + line);
			System.out.println(text);
			System.out.println(line);
			String confirmToken = text.split("_CONFIRMATIONTOKEN=", 2)[1]
					.split("&", 2)[0];
			String sysMod = text.split("\"sysMod\":\"", 2)[1]
					.split("\"});")[0];
			String entityId = caseUrl.split("gmi.my.salesforce.com?ec=302&startURL=%2F")[1];
			System.out.println("ConfirmationToken, sysMod, entityId:");
			System.out.println(confirmToken);
			System.out.println(sysMod);
			System.out.println(entityId);
			System.out.println(line);
			
			//修改系统自动生成的入金case
			forms = Form.form()
					.add("entityId", entityId)
					.add("sysMod", sysMod)
					.add("_CONFIRMATIONTOKEN", confirmToken)
					.add("save", "1")
		//			.add("cas4", "Fan XuGuang")
		//			.add("cas4_lkid", "sdafsdf")
					.add("00N9000000E97Jl", "入金")
					.add("00N6F00000DtmTt", "Baofoo")
					.add("00N6F00000DtmTy", depositDetailsMap.get("CCY"))
					.add("00N6F00000DtmU3", depositDetailsMap.get("Account Input Reference / Notes").split("@ ")[1]) // 计算后的汇率
					.add("00N90000005QTGA", depositDetailsMap.get("Trader Login"))
					.add("cas7", "On Hold")
					.add("cas14", "Deposit") // subject
					.add("00N6F00000EfzPF", depositDetailsMap.get("Confirmed"))
					.add("00N90000005QUg3", depositDetailsMap.get("通过"))
					.add("00N90000005QUii", depositDetailsMap.get("Actual Received"))
					.add("00N90000005RqF1", "宝付支付")	
					.add("00N90000005QRqn", depositDetailsMap.get("Deposit Amount"))
					.build();
			
			httpResponse = Request.Post("https://gmi.my.salesforce.com/ui/common/InlineEditEntitySave")
					.addHeader("Cookie", cookie)
					.bodyForm(forms, Consts.UTF_8)
					.execute()
					.returnResponse();
			
			System.out.println("☆☆☆☆☆☆☆☆☆☆☆ 提交入金case修改 ☆☆☆☆☆☆☆☆☆☆☆");
			System.out.println(httpResponse.getStatusLine());	
			headers = httpResponse.getAllHeaders();
			for (Header h : headers)
				System.out.println(h.getName() + " ===> " + h.getValue());
			System.out.println(line);
			System.out.println(EntityUtils.toString(httpResponse.getEntity(), Consts.UTF_8));
			System.out.println(line);
			
			//退出登录
			httpResponse = Request.Get("https://gmi.my.salesforce.com/secur/logout.jsp")
			            .addHeader("Cookie", cookie)
			            .execute()
			            .returnResponse();
			
			//查看headers
			System.out.println("☆☆☆☆☆☆☆☆☆☆☆ 退出登录！ ☆☆☆☆☆☆☆☆☆☆☆");
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
