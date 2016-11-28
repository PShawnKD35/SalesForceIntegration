package pshawn.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** Salesforce.com 的自动化处理类
 * @arthur Shawn Peng
 */
public class Salesforce {

	static final String line = "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
	static final String headLine ="Trader Login	EN Name	IB	AE	Notification	CCY	Deposit Amount	SF Case #	Promo	Source	Bank Account	Confirmed	Conv Rate	Actual Received	System Reference	Account Input Reference / Notes";
	static final String[] heads = headLine.split("\t");
	private CloseableHttpClient httpClient;
	private String cookie;
	
	public Salesforce() {
		
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		// 读取用户名密码文件(程序根目录) SalesforceCredential.txt
		File file = new File("SalesforceCredential.txt");
		if (!file.exists()) {
			String filePath = file.getAbsolutePath(); // 系统当前目录
			System.out.println("Warning: Can't find Salesforce userName/Password file(SalesforceCredential.txt) at: " + filePath);
			return;
		}
		BufferedReader bufferedReader;
		bufferedReader = new BufferedReader(new FileReader(file));
		String un = bufferedReader.readLine(); // 用户名
		String pw = bufferedReader.readLine(); // 密码
		bufferedReader.close();
		
		Salesforce salesforce = new Salesforce();
		try {
			// 登录			
			salesforce.login(un, pw);
			//修改入金case
			String depositExcelRow =readInputLine("Please copy the deposit details from Excel and paste here as plain text in one line, columns splited by TAB. \r\nEnter \"exit\" to end");
			while(!depositExcelRow.equals("exit")){
				salesforce.editDepositCase(
						readDepositDetails(depositExcelRow));
				depositExcelRow =readInputLine("Please copy the deposit details from Excel and paste here as plain text in one line, columns splited by TAB. \r\nEnter \"exit\" to end");
			}
			
//			salesforce.editDepositCase(new HashMap<String, String>(20));
			
		} finally {
			// TODO: handle finally clause
			//登出Salesforce
			salesforce.logout();
		}
	}
	
	/**
	 * 从键盘读一行字符串
	 * @param note 告诉用户需要输入什么的字符串
	 * @return 读取到的字符串
	 */
	public static String readInputLine(String note) {
		System.out.println(line + "\r\n" + note + ":");
		Scanner input = new Scanner(System.in);
		String outputLine = input.nextLine(); //从键盘读取入金信息行
		System.out.println(line);
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
	 * Log into Salesforce.com
	 * @param un Salesforce用户名
	 * @param pw 密码
	 * @throws IOException
	 */
	public void login(String un, String pw) throws IOException {
		
		LaxRedirectStrategy laxRedirectStrategy = new LaxRedirectStrategy(); // 自动处理post重定向
		
		httpClient = HttpClients.custom()
	            .setRedirectStrategy(laxRedirectStrategy) // 允许重定向的client
	            .build();
		
		CloseableHttpResponse closeableHttpResponse;
		HttpClientContext context = new HttpClientContext(); // 用来保存cookies
		CookieStore cookieStore = new BasicCookieStore();
		context.setCookieStore(cookieStore);		

		
		// 设置登录表单
//		String un = readInputLine("Please input your Salesforce User Name"); //从键盘读取用户名
//		String pw = readInputLine("Please input your Salesforce password"); //读取密码
				
		System.out.println("Salesforce User name: " + un + "\r\n" + line);
		
		List<NameValuePair> forms = Form.form()
				.add("un", un)
				.add("hasRememberUn", "true")
				.add("useSecure", "true")
				.add("username", un)
				.add("pw", pw)
				.add("Login", "登录")
//				.add("rememberUn", "on")
				.build();
		
		//登录
		HttpPost post = new HttpPost("https://gmi.my.salesforce.com/");
//		post.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(forms, Consts.UTF_8);
		post.setEntity(formEntity);			
		closeableHttpResponse = httpClient.execute(post, context);
		System.out.println(closeableHttpResponse.getStatusLine());			
		//展示当前cookies
		cookie = ""; // 准备把cookie做成Header的形式（String）
		List<Cookie> cookies = cookieStore.getCookies();
		for(Cookie c : cookies){				
			String domain = c.getDomain();
			System.out.println(domain+ "\t" +c.getName() + "=" + c.getValue());
			if (domain.contains("salesforce.com") && !domain.contains("ap4.salesforce.com")) {
//				cookieStore.addCookie(c); //加入salesforce.com的cookie
				cookie =c.getName() + "=" + c.getValue() + "; " + cookie;
			}
		}
		System.out.println(line);

		String text = EntityUtils.toString(closeableHttpResponse.getEntity(), Consts.UTF_8);
		closeableHttpResponse.close();
		System.out.println(text);
		System.out.println(line);
		
//		//测试是否登录成功
////		HttpGet get = new HttpGet("https://gmi.my.salesforce.com/");
//		URI uri = new URI("https://gmi.my.salesforce.com/500/o");
//		get.setURI(uri);
//		closeableHttpResponse = httpClient.execute(get, context);
//		
//		//展示当前cookies
//		cookies = cookieStore.getCookies();
//		for(Cookie c : cookies){
//			System.out.println(c.getName() + "=" + c.getValue());
//		}
//		System.out.println(line);
//		
//		text = EntityUtils.toString(closeableHttpResponse.getEntity(), Consts.UTF_8);
//		closeableHttpResponse.close();
//		System.out.println(text);
		
		
	}
	
	/**
	 * Log out Salesforce
	 * @throws org.apache.http.ParseException
	 * @throws IOException
	 */
	public void logout() throws org.apache.http.ParseException, IOException{
		//退出登录 Fluent API
		HttpResponse httpResponse = Request.Get("https://gmi.my.salesforce.com/secur/logout.jsp")
		            .addHeader("Cookie", cookie)
		            .execute()
		            .returnResponse();
		
		//查看headers
		System.out.println("☆☆☆☆☆☆☆☆☆☆☆ 退出登录！ ☆☆☆☆☆☆☆☆☆☆☆");
		System.out.println(httpResponse.getStatusLine());			
		Header[] headers = httpResponse.getAllHeaders();			
		for (Header h : headers)
			System.out.println(h.getName() + " ===> " + h.getValue());			
		System.out.println(line);
		String text = EntityUtils.toString(httpResponse.getEntity(), Consts.UTF_8);
		System.out.println(text);
		
//		//退出登录
//		uri =new URI("https://gmi.my.salesforce.com/secur/logout.jsp");
//		get.setURI(uri);
//		closeableHttpResponse = httpClient.execute(get, context);
//		text = EntityUtils.toString(closeableHttpResponse.getEntity(), Consts.UTF_8);
//		System.out.println(text);
//		closeableHttpResponse.close();
		//关闭资源
		if (httpClient!=null)
			httpClient.close();		
		
	}
	
	
	/**
	 * 根据用户入金情况建立/修改 Salesforce 的case
	 * @param depositDetailsMap
	 */
	public void editDepositCase(Map<String, String> depositDetailsMap) {
		System.out.println(line);
		if (depositDetailsMap == null) return;
		System.out.println("User Login: \r\n"+depositDetailsMap.get("Trader Login") +"\r\n"+ line);
				
		try {
			String text;
//			String cookie;
			Document document;
			Elements elements;
			Element element;
			HttpResponse httpResponse;
			Response response;
			List<NameValuePair> forms;
			Header[] headers;
			List<Cookie> cookies;
			URI uri;
			HttpGet get = new HttpGet("https://gmi.my.salesforce.com/");	

			
			//修改系统自动生成的入金case
			// 转换日期格式成Salesforce格式
			String dateString = depositDetailsMap.get("Confirmed");
			try {
				SimpleDateFormat dateFormat =new SimpleDateFormat("dd/mm/yyyy");
				Date date = dateFormat.parse(dateString);
				dateFormat.applyPattern("mm/dd/yyyy");
				dateString = dateFormat.format(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("入金日期: " + dateString);
			System.out.println(line);
			
			// 拿entityID, sysMod, 和ConfirmationToken	
			String confirmToken;
			String entityId;
			String caseUrl;
			entityId = readInputLine("键入case ID，如: 5006F00001IA7KE");
			caseUrl = "https://gmi.my.salesforce.com?ec=302&startURL=%2F" + entityId;
			text = Request.Get(caseUrl)
					.addHeader("Cookie", cookie)
					.execute()
					.returnContent().asString(Consts.UTF_8);
			
			System.out.println("拿case信息\r\n" + line);
			System.out.println(text);
			System.out.println(line);
			confirmToken = text.split("_CONFIRMATIONTOKEN=", 2)[1]
					.split("&", 2)[0];
			String sysMod = text.split("\"sysMod\":\"", 2)[1]
					.split("\"}", 2)[0];
//						String entityId = caseUrl.split("startURL=%2F")[1];
			System.out.println("ConfirmationToken, sysMod, entityId:");
			System.out.println(confirmToken);
			System.out.println(sysMod);
			System.out.println(entityId);
			System.out.println(line);			
			//修改
			forms = Form.form()
					.add("entityId", entityId)
					.add("sysMod", sysMod)
					.add("_CONFIRMATIONTOKEN", confirmToken)
					.add("save", "1")
//					.add("cas4", "Fan XuGuang")
//					.add("cas4_lkid", "sdafsdf")
//					.add("cas4_mod", "1")
					.add("00N9000000E97Jl", "入金")
					.add("00N6F00000DtmTt", "Baofoo")
					.add("00N6F00000DtmTy", depositDetailsMap.get("CCY"))
					.add("00N6F00000DtmU3", depositDetailsMap.get("Account Input Reference / Notes").split("@ ")[1]) // 计算后的汇率
					.add("00N90000005QTGA", depositDetailsMap.get("Trader Login"))
					.add("cas7", "On Hold")
					.add("cas14", "Deposit") // subject
					.add("00N6F00000EfzPF", dateString) // 入金日期
					.add("00N90000005QT62", dateString)
					.add("00N90000005QUg3", "通过")
					.add("00N90000005QUii", depositDetailsMap.get("Actual Received"))
					.add("00N90000005RqF1", "宝付支付")	
					.add("00N90000005QRqn", depositDetailsMap.get("Deposit Amount"))
					.add("cas16", depositDetailsMap.get("Account Input Reference / Notes"))
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
			
			// 移动case到 Back OfficeTeam			
			// 拿ConfirmationToken
			caseUrl = "https://gmi.my.salesforce.com/"+entityId+"/a?retURL=%2F" + entityId;
			text = Request.Get(caseUrl)
					.addHeader("Cookie", cookie)
					.execute()
					.returnContent().asString(Consts.UTF_8);
			
			System.out.println("拿case edit信息\r\n" + line);
			System.out.println(text);
			System.out.println(line);
			document = Jsoup.parse(text);
			element = document.getElementById("_CONFIRMATIONTOKEN");
			confirmToken = element.attr("value");
//			confirmToken = text.split("_CONFIRMATIONTOKEN=", 2)[1]
//					.split("&", 2)[0];
			System.out.println("ConfirmationToken:");
			System.out.println(confirmToken);
			System.out.println(line);			
			//移动
			forms = Form.form()
					.add("_CONFIRMATIONTOKEN", confirmToken)
					.add("id", entityId)
					.add("ids", entityId)
					.add("newOwn_mlktp", "case_queue")
					.add("newOwn_lktp", "case_queue")
					.add("newOwn_lkold", "null")
					.add("newOwn_lspf", "0")
					.add("newOwn_lspfsub", "0")
					.add("newOwn", "Back OfficeTeam")
					.add("newOwn_mod", "1")
					.add("sendMail", "1")
					.add("save", "1")
					.build();
			
			httpResponse = Request.Post("https://gmi.my.salesforce.com/"+entityId+"/a")
					.addHeader("Cookie", cookie)
					.bodyForm(forms, Consts.UTF_8)
					.execute()
					.returnResponse();
			
			System.out.println("☆☆☆☆☆☆☆☆☆☆☆ 移动case到Back OfficeTeam ☆☆☆☆☆☆☆☆☆☆☆");
			System.out.println(httpResponse.getStatusLine());	
			headers = httpResponse.getAllHeaders();
			for (Header h : headers)
				System.out.println(h.getName() + " ===> " + h.getValue());
			System.out.println(line);
			System.out.println(EntityUtils.toString(httpResponse.getEntity(), Consts.UTF_8));
			System.out.println(line);
			
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
	}

}