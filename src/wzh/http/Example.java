/**
 * 
 */
package wzh.http;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**For test
 * @author lankey
 *
 */
public class Example {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
//		CloseableHttpClient httpclient = HttpClients.createDefault();
//		HttpGet httpGet = new HttpGet("http://www.google.com");
//		httpGet.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
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
		
		
		Document result = Request.Get("http://www.baidu.com").execute()
				.handleResponse(new ResponseHandler<Document>() {

					public Document handleResponse(final HttpResponse response) throws IOException {
						StatusLine statusLine = response.getStatusLine();
						HttpEntity entity = response.getEntity();
						if (statusLine.getStatusCode() >= 300) {
							throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
						}
						if (entity == null) {
							throw new ClientProtocolException("Response contains no content");
						}
						DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
						try {
							DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
							ContentType contentType = ContentType.getOrDefault(entity);
							if (!contentType.equals(ContentType.APPLICATION_XML)) {
								throw new ClientProtocolException("Unexpected content type:" + contentType);
							}
							Charset charset = contentType.getCharset();
							if (charset == null) {
								charset = Consts.UTF_8;
							}
							return docBuilder.parse(entity.getContent(), charset.name());
						} catch (ParserConfigurationException ex) {
							throw new IllegalStateException(ex);
						} catch (SAXException ex) {
							throw new ClientProtocolException("Malformed XML document", ex);
						}
					}

				});
		
		System.out.println(result.toString());

	}

}
