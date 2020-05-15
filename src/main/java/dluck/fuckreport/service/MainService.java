package dluck.fuckreport.service;

import dluck.fuckreport.domain.User;
import dluck.fuckreport.repository.UserRepository;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MainService {
	private final String url_login = "http://xsc.sicau.edu.cn/SPCP/Web/";
	private final String url_report = "http://xsc.sicau.edu.cn/SPCP/Web/Report/Index";
	private final UserRepository userRepository;

	@Autowired
	public MainService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * do report
	 *
	 * @param uid user id
	 * @return status
	 */
	public String report(String uid) {
		//获取User对象
		User user = userRepository.findById(uid).orElse(null);
		if (user == null) return "没有找到用户！";

		//创建HttpClient对象
		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36")
				.build();

		//创建HttpPost对象
		HttpPost httpPost = new HttpPost(url_login);
		httpPost.setHeader(new BasicHeader("Content-type", "application/x-www-form-urlencoded"));

		//创建表单数据
		List<NameValuePair> data = new ArrayList<>();
		data.add(new BasicNameValuePair("txtUid", user.getUid()));
		data.add(new BasicNameValuePair("txtPwd", user.getPassword()));

		try {
			//带入表单数据
			httpPost.setEntity(new UrlEncodedFormEntity(data, "utf-8"));
			httpClient.execute(httpPost);
		} catch (Exception e) {
			return "登陆时出错！";
		}

		//新建GET对象，开始访问打卡界面
		HttpGet httpGet = new HttpGet(url_report);
		HttpResponse response;

		try {
			response = httpClient.execute(httpGet);
		} catch (IOException e) {
			return "打卡界面访问失败";
		}

		//解析打卡界面
		Document document;
		try {
			document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
		} catch (IOException e) {
			return "打卡界面解析失败";
		}

		//判断是否未打卡
		if (document.title().equals("四川农业大学-疫情防控管理平台")) {
			return "未打卡";
		} else {
			Element element = document.body();
			return element.getElementsByTag("script").html();
		}
	}

	/**
	 * do check
	 *
	 * @return status
	 */
	public String check(String uid) {
		//获取User对象
		User user = userRepository.findById(uid).orElse(null);
		if (user == null) return "没有找到用户！";

		//创建HttpClient对象
		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36")
				.build();

		//创建HttpPost对象
		HttpPost httpPost = new HttpPost(url_login);
		httpPost.setHeader(new BasicHeader("Content-type", "application/x-www-form-urlencoded"));

		//创建表单数据
		List<NameValuePair> data = new ArrayList<>();
		data.add(new BasicNameValuePair("txtUid", user.getUid()));
		data.add(new BasicNameValuePair("txtPwd", user.getPassword()));

		try {
			//带入表单数据
			httpPost.setEntity(new UrlEncodedFormEntity(data, "utf-8"));
			httpClient.execute(httpPost);
		} catch (Exception e) {
			return "登陆时出错！";
		}

		//新建GET对象，开始访问打卡界面
		HttpGet httpGet = new HttpGet(url_report);
		HttpResponse response;

		try {
			response = httpClient.execute(httpGet);
		} catch (IOException e) {
			return "打卡界面访问失败";
		}

		//解析打卡界面
		Document document;
		try {
			document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
		} catch (IOException e) {
			return "打卡界面解析失败";
		}

		//判断是否未打卡
		if (document.title().equals("四川农业大学-疫情防控管理平台")) {
			return "未打卡";
		} else {
			String str = document.body().getElementsByTag("script").html();
			String[] strings = str.split("'");
			if (strings[1] != null) {
				return strings[1];
			} else {
				return str;
			}
		}
	}
}
