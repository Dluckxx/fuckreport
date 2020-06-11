package dluck.fuckreport.service;

import com.google.gson.Gson;
import dluck.fuckreport.ScheduledTasks;
import dluck.fuckreport.domain.User;
import dluck.fuckreport.dao.UserRepository;
import dluck.fuckreport.vo.PZDataVo;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MainService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String url_login = "http://xsc.sicau.edu.cn/SPCP/Web/";
	private final String url_report = "http://xsc.sicau.edu.cn/SPCP/Web/Report/Index";
	private final String url_code = "http://xsc.sicau.edu.cn/SPCP/Web/Account/GetLoginVCode";
	private final UserRepository userRepository;
	private final MailService mailService;

	@Autowired
	public MainService(UserRepository userRepository, MailService mailService) {
		this.userRepository = userRepository;
		this.mailService = mailService;
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
	 * 获取全部的用户
	 *
	 * @return 用户列表
	 */
	public List<User> getAllUser() {
		return userRepository.findAll();
	}

	/**
	 * 根据ID获取一个用户，没找到则返回null
	 *
	 * @param uid 学号
	 * @return 用户对象
	 */
	public User getUserByID(String uid) {
		return userRepository.findById(uid).orElse(null);
	}

	/**
	 * 模拟一次登陆，获取Cookie信息并保存到数据库
	 *
	 * @param user 用户对象
	 * @return 成功：true；失败：false
	 */
	public boolean login(User user, String code) {
		//创建HttpClient对象
		CookieStore cookieStore = getLocalCookies(user);
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
		data.add(new BasicNameValuePair("code", code));

		//带入表单数据
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(data, "utf-8"));
			HttpResponse response = httpClient.execute(httpPost);
			Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()));
			if (doc.title().equals("Object moved")) {
				setLocalCookies(user, cookieStore);
				logger.info("login: 用户[{}]完成了一次登陆", user.getName());
				return true;
			} else {
				logger.info("login: 用户[{}]尝试登陆失败！", user.getName());
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取登陆验证码的图片文件输入流
	 *
	 * @return url
	 */
	public InputStream getLoginCodeImage(User user) {
		String url_local = url_code + "?dt=" + new Date().getTime();
		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36")
				.setDefaultCookieStore(cookieStore)
				.build();

		HttpGet httpGet = new HttpGet(url_local);

		try {
			HttpResponse response = httpClient.execute(httpGet);
			setLocalCookies(user, cookieStore); //保存cookie
			if (response.getEntity().getContentType().getValue().equals("image/JPEG")) {
				return response.getEntity().getContent();
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 通过html文本获取打卡数据，如果未打卡获取需要提交的表单，否则获取NULL
	 *
	 * @param html HTML页面内容
	 * @return 表单对象
	 */
	public List<NameValuePair> getPostData(String html) {
		Document document = Jsoup.parse(html);
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
			System.err.println("获取表单信息失败，非正常页面！");
			return null;
		}

		return data;
	}

	/**
	 * 查询打卡状态接口
	 *
	 * @return 状态信息字符串
	 */
	public String check(User user) {
		//创建HttpClient对象
		CookieStore cookieStore = getLocalCookies(user);
		if (cookieStore == null) return "Cookie为null";

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
	 * 打卡接口，返回打卡操作结果
	 *
	 * @param user 用户
	 * @return 操作状态字符串
	 */
	public String report(User user) {
		CookieStore cookieStore = getLocalCookies(user);
		if (cookieStore == null) return "无法获取cookie！";

		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36")
				.build();

		//新建POST对象，开始访问打卡界面
		HttpPost httpPost = new HttpPost(url_report);
		HttpGet httpGet = new HttpGet(url_report);
		HttpResponse response;
		UrlEncodedFormEntity entity;
		List<NameValuePair> data;

		//拉取表单信息
		try {
			response = httpClient.execute(httpGet);
			data = getPostData(EntityUtils.toString(response.getEntity()));
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
			StringBuilder dataHandler = new StringBuilder();
			for (NameValuePair pair : data) {
				dataHandler.append(pair.getName());
				dataHandler.append("：");
				dataHandler.append(pair.getValue());
				dataHandler.append("\n");
			}
			//验证数据，发送邮件给用户
			if (response.getStatusLine().getStatusCode() == 200) {
				mailService.sendReportSuccessEmail(user.getEmail(), dataHandler.toString());
				return "提交成功！";
			} else {
				mailService.sendReportFailedEmail(user.getEmail(), "打卡请求提交失败了！");
				return "提交失败：" + response.getStatusLine();
			}
		} catch (IOException e) {
			return "打卡页面访问失败！";
		}
	}

	/**
	 * 从数据库获取cookie
	 *
	 * @param user 用户对象
	 * @return CookieStore
	 */
	private CookieStore getLocalCookies(User user) {
		if (user == null) return null;

		CookieStore cookieStore = new BasicCookieStore();

		BasicClientCookie cookie1 = new BasicClientCookie("ASP.NET_SessionId", user.getSessionId());
		cookie1.setDomain("xsc.sicau.edu.cn");
		cookie1.setPath("/");

		BasicClientCookie cookie2 = new BasicClientCookie("CenterSoftWeb", user.getCenterSoftWeb());
		cookie2.setDomain("xsc.sicau.edu.cn");
		cookie2.setPath("/");

		cookieStore.addCookie(cookie1);
		cookieStore.addCookie(cookie2);

		return cookieStore;
	}

	/**
	 * 保存cookie到数据库
	 *
	 * @param user        用户对象
	 * @param cookieStore cookie对象
	 */
	private void setLocalCookies(User user, CookieStore cookieStore) {
		List<Cookie> cookies = cookieStore.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("ASP.NET_SessionId"))
				user.setSessionId(cookie.getValue());
			else if (cookie.getName().equals("CenterSoftWeb"))
				user.setCenterSoftWeb(cookie.getValue());
		}
		userRepository.save(user);
	}
}
