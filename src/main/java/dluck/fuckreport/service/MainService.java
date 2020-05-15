package dluck.fuckreport.service;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MainService {
	private static final String url_login = "http://xsc.sicau.edu.cn/SPCP/Web/";
	private static final String url_report = "http://xsc.sicau.edu.cn/SPCP/Web/Report/Index";

	/**
	 * do report
	 * @param uid user id
	 * @return status
	 */
	public String report(String uid) {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpClient = httpClientBuilder.build();

		HttpPost httpPost = new HttpPost(url_login);
		List<NameValuePair> data = new ArrayList<>();
		data.add(new BasicNameValuePair("txtUid", "201703880"));
		data.add(new BasicNameValuePair("txtPwd", "289110"));

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			HttpResponse result = httpClient.execute(httpPost);
			String cookie = result.getFirstHeader("Set-Cookie").getValue();
			System.out.println(cookie);
			return "成功！";
		} catch (Exception e) {
			e.printStackTrace();
			return "错误！";
		}
	}

	/**
	 * do check
	 * @return status
	 */
	public String check(String uid){
		return "null";
	}
}
