/**
 * 
 */
package wzh.http;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**For test
 * @author lankey
 *
 */
public class Example {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		CloseableHttpClient httpclient = HttpClients.createDefault();
//		HttpGet httpGet = new HttpGet("http://targethost/homepage");
//		CloseableHttpResponse response1 = httpclient.execute(httpGet);
//		// The underlying HTTP connection is still held by the response object
//		// to allow the response content to be streamed directly from the network socket.
//		// In order to ensure correct deallocation of system resources
//		// the user MUST call CloseableHttpResponse#close() from a finally clause.
//		// Please note that if response content is not fully consumed the underlying
//		// connection cannot be safely re-used and will be shut down and discarded
//		// by the connection manager. 
//		try {
//		    System.out.println(response1.getStatusLine());
//		    HttpEntity entity1 = response1.getEntity();
//		    // do something useful with the response body
//		    // and ensure it is fully consumed
//		    EntityUtils.consume(entity1);
//		} finally {
//		    response1.close();
//		}
		
		
		//执行一个GET请求,同时设置Timeout参数并将响应内容作为String返回  
        String response;
		try {
			response = Request.Get("http://baidu.com")  
			        .connectTimeout(1000)  
			        .socketTimeout(1000)  
			        .execute().returnContent().asString();
			System.out.println(response);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		

	}

}
