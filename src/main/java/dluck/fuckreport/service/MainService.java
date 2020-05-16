package dluck.fuckreport.service;

import com.google.gson.Gson;
import dluck.fuckreport.domain.User;
import dluck.fuckreport.dao.UserRepository;
import dluck.fuckreport.vo.PZDataVo;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
	 * 添加一个用户
	 *
	 * @param uid   学号
	 * @param name  姓名
	 * @param pwd   密码
	 * @param email 邮箱
	 * @return 成功返回true，否则false
	 */
	public boolean addUser(String uid, String name, String pwd, String email) {
		User user = new User();
		user.setUid(uid);
		user.setName(name);
		user.setPassword(pwd);
		user.setEmail(email);

		if (userRepository.findById(uid).isPresent()) {
			return false;
		} else {
			userRepository.save(user);
			return true;
		}
	}

	/**
	 * 删除一个用户
	 *
	 * @param uid 学号
	 * @return 如果成功返回true，否则false
	 */
	public boolean removeUser(String uid) {
		if (userRepository.findById(uid).isPresent()) {
			userRepository.delete(userRepository.findById(uid).get());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 模拟一次登陆，获取Cookie信息
	 *
	 * @param uid 学号
	 * @return CookieStore对象
	 */
	public CookieStore getLoginCookie(String uid) throws IOException {
		User user = userRepository.findById(uid).orElse(null);
		if (user == null) return null;

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


		//带入表单数据
		httpPost.setEntity(new UrlEncodedFormEntity(data, "utf-8"));
		httpClient.execute(httpPost);

		try {
			httpClient.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		return cookieStore;
	}

	/**
	 * 模拟一次登陆，如果未打卡获取需要提交的表单，否则获取NULL
	 *
	 * @param uid 学号
	 * @return 表单对象
	 */
	public List<NameValuePair> getPostData(String uid) throws IOException {
		//创建HttpClient对象
		CookieStore cookieStore = getLoginCookie(uid);
		CloseableHttpClient httpClient = HttpClients.custom()
				.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36")
				.setDefaultCookieStore(cookieStore)
				.build();

		HttpGet httpGet = new HttpGet(url_report);
		HttpResponse response = httpClient.execute(httpGet);
		Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
		List<NameValuePair> data = new ArrayList<>();
		Gson gson = new Gson();

		//获取打卡表单信息
		if (document.title().equals("四川农业大学-疫情防控管理平台")) {
			//基础个人信息
			data.add(new BasicNameValuePair("StudentId", document.getElementById("StudentId").val()));
			data.add(new BasicNameValuePair("Name", document.getElementById("Name").val()));
			data.add(new BasicNameValuePair("Sex", document.getElementById("Sex").val()));
			data.add(new BasicNameValuePair("SpeType", document.getElementById("SpeType").val()));
			data.add(new BasicNameValuePair("CollegeNo", document.getElementById("CollegeNo").val()));
			data.add(new BasicNameValuePair("SpeGrade", document.getElementById("SpeGrade").val()));
			data.add(new BasicNameValuePair("SpecialtyName", document.getElementById("SpecialtyName").val()));
			data.add(new BasicNameValuePair("ClassName", document.getElementById("ClassName").val()));
			data.add(new BasicNameValuePair("MoveTel", document.getElementById("MoveTel").val()));

			//地址信息
			data.add(new BasicNameValuePair("Province", document.getElementById("ProvinceName").val()));
			data.add(new BasicNameValuePair("City", document.getElementById("CityName").val()));
			data.add(new BasicNameValuePair("County", document.getElementById("CountyName").val()));
			data.add(new BasicNameValuePair("ComeWhere", document.getElementsByAttributeValue("name", "ComeWhere").first().val()));
			data.add(new BasicNameValuePair("FaProvince", document.getElementById("FaProvinceName").val()));
			data.add(new BasicNameValuePair("FaCity", document.getElementById("FaCityName").val()));
			data.add(new BasicNameValuePair("FaCounty", document.getElementById("FaCountyName").val()));
			data.add(new BasicNameValuePair("FaComeWhere", document.getElementsByAttributeValue("name", "FaComeWhere").first().val()));

			//处理单选项目
			List<PZDataVo> dataVos = new ArrayList<>();
			int radio = Integer.parseInt(document.getElementById("radioCount").val());

			for (int i = 1; i <= radio; i++) {
				Elements elements = document.select("input[name=radio_" + i + "]")
						.select("[checked=checked]");

				//判断解析HTML是是否出错
				if (elements.isEmpty()) {
					System.err.println("elements is empty!");
				} else if (elements.size() > 1) {
					System.err.println("elements has more than one element!");
				}

				//处理单个选项
				Element element = elements.get(0);
				PZDataVo item = new PZDataVo(element.attr("data-optionName"));
				item.setSelectId(element.val());
				item.setTitleId(element.parent().attr("data-tid"));
				item.setOptionType(element.attr("id"));

				//添加表单数据
				dataVos.add(item);
				data.add(new BasicNameValuePair("radio_" + i, item.getSelectId()));
			}

			//多余的信息
			data.add(new BasicNameValuePair("GetAreaUrl", document.getElementById("GetAreaUrl").val()));
			data.add(new BasicNameValuePair("IdCard", document.getElementById("IdCard").val()));
			data.add(new BasicNameValuePair("ProvinceName", document.getElementById("ProvinceName").val()));
			data.add(new BasicNameValuePair("CityName", document.getElementById("CityName").val()));
			data.add(new BasicNameValuePair("CountyName", document.getElementById("CountyName").val()));
			data.add(new BasicNameValuePair("FaProvinceName", document.getElementById("FaProvinceName").val()));
			data.add(new BasicNameValuePair("FaCityName", document.getElementById("FaCityName").val()));
			data.add(new BasicNameValuePair("FaCountyName", document.getElementById("FaCountyName").val()));
			data.add(new BasicNameValuePair("radioCount", document.getElementById("radioCount").val()));
			data.add(new BasicNameValuePair("checkboxCount", document.getElementById("checkboxCount").val()));
			data.add(new BasicNameValuePair("blackCount", document.getElementById("blackCount").val()));
			data.add(new BasicNameValuePair("PZData", gson.toJson(dataVos)));
			data.add(new BasicNameValuePair("ReSubmiteFlag", document.getElementsByAttributeValue("name", "ReSubmiteFlag").val()));
		} else {
			System.err.println(check(uid));
			return null;
		}

		//释放资源
		try {
			httpClient.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return data;
	}

	/**
	 * 查询打卡状态接口
	 *
	 * @return 状态信息字符串
	 */
	public String check(String uid) {
		//创建HttpClient对象
		CookieStore cookieStore;
		try {
			cookieStore = getLoginCookie(uid);
		} catch (IOException e) {
			e.printStackTrace();
			return "登陆失败!";
		}

		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36")
				.build();

		//新建GET对象，开始访问打卡界面
		HttpGet httpGet = new HttpGet(url_report);
		HttpResponse response;
		try {
			response = httpClient.execute(httpGet);
		} catch (IOException e) {
			return "打卡界面访问失败";
		}

		//解析HTML
		Document document;
		try {
			document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
		} catch (IOException e) {
			return "解析HTML失败";
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

	/**
	 * 打卡接口
	 *
	 * @param uid 学号
	 * @return 操作状态字符串
	 */
	public String report(String uid) {
		//创建HttpClient对象
		CookieStore cookieStore;
		try {
			cookieStore = getLoginCookie(uid);
		} catch (IOException e) {
			e.printStackTrace();
			return "登陆失败！";
		}

		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36")
				.build();

		//新建POST对象，开始访问打卡界面
		HttpPost httpPost = new HttpPost(url_report);
		HttpResponse response;
		UrlEncodedFormEntity entity;
		List<NameValuePair> data;

		//拉取表单信息
		try {
			data = getPostData(uid);
			entity = new UrlEncodedFormEntity(data, "utf-8");
			httpPost.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "表单信息解码失败：不支持的编码！";
		} catch (IOException e) {
			e.printStackTrace();
			return "获取表单信息失败！";
		}

		//提交数据
		try {
			response = httpClient.execute(httpPost);
			//验证数据
			if (response.getStatusLine().getStatusCode() == 200) {
				return "提交成功！";
			} else {
				return "提交失败：" + response.getStatusLine();
			}
		} catch (IOException e) {
			return "打卡界面访问失败！";
		}
	}

}
