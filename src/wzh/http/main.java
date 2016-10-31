package wzh.http;

public class main {

	public static void main(String[] args) {
        //发送 GET 请求
        //String s=HttpRequest.sendGet("http://localhost:6144/Home/RequestString", "key=123&v=456");
        //System.out.println(s);
        
        //发送 POST 请求
        //String sr=HttpRequest.sendPost("http://localhost:6144/Home/RequestPostString", "key=123&v=456");
        //System.out.println(sr);
		
		String sr=HttpRequest.sendPost("https://arms3.onezero.com/login_check", "_csrf_token=vtgcJSpa_Wi6l_WHHRhv-QZ5rUhDUUXQhiCn-KRBZc0&_username=shawn.peng%40gmimarkets.com&_password=456456&_submit=");
        System.out.println(sr);
        
        String s=HttpRequest.sendGet("https://arms3.onezero.com/login", "");
		System.out.println(s);
        
//        String s=HttpRequest.sendGet("https://www.baidu.com/s", "ie=utf-8&f=8&rsv_bp=0&rsv_idx=1&tn=baidu&wd=aaa&rsv_pq=fb3fd7960002d255&rsv_t=e005igCXF8ZJEy8ehCuX4OvIL2qLVepa0FoRFjmO2gfjWx14cIyYkRndI5g&rqlang=cn&rsv_enter=0&rsv_sug3=4&rsv_sug1=1&rsv_sug7=100&inputT=1273&rsv_sug4=1512");
//		System.out.println(s);
    }

}
