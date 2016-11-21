package wzh.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
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
						readInputLine()));		

	}
	
	/**
	 * 从键盘读一行字符串
	 * @return 读取到的字符串
	 */
	public static String readInputLine(){
		Scanner input = new Scanner(System.in);		
		System.out.println(line + "\r\nPlease copy the deposit details from Excel and paste here as plain text in one line, columns splited by TAB:");
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
			Document document;
			Elements elements;
			Element element;
			HttpResponse httpResponse;
			Response response;
			
			// 拿登陆使用的_csrf_token
			text = Request.Get("https://gmi.my.salesforce.com/")
					.connectTimeout(20000)  
					.socketTimeout(20000)
//				.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
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
			
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		
	}

}
