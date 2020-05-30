package dluck.fuckreport.controller;

import dluck.fuckreport.service.MainService;
import org.apache.http.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * URL接口Controller
 */
@RestController
@RequestMapping("api")
public class ApiController {

	private final MainService mainService;

	@Autowired
	public ApiController(MainService mainService) {
		this.mainService = mainService;
	}

	/**
	 * 此接口可以查询用户的打卡状态
	 *
	 * @param uid 学号
	 * @return 查询信息字符串
	 */
	@GetMapping("check")
	public String check(@RequestParam("uid") String uid) {
		return mainService.check(uid);
	}

	/**
	 * 获取表单信息接口
	 *
	 * @param uid 学号
	 * @return 表单信息字符串
	 */
	@GetMapping("conform")
	public String conform(@RequestParam("uid") String uid) {
		List<NameValuePair> list;
		list = mainService.getPostData(uid);
		if (list == null) return "已经打过卡或者出现错误！";
		StringBuilder sb = new StringBuilder();
		for (NameValuePair item : list) {
			sb.append(item.getName())
					.append(" : ")
					.append(item.getValue())
					.append("\n");
		}
		return sb.toString();
	}

	/**
	 * 此接口可以完成一次打卡
	 *
	 * @param uid 学号
	 * @return 打卡操作状态
	 */
	@GetMapping("report")
	public String report(@RequestParam("uid") String uid) {
		return mainService.report(uid);
	}

	/**
	 * 添加用户接口
	 *
	 * @param uid      学号
	 * @param name     名称
	 * @param password 密码
	 * @param email    邮箱
	 * @return 状态
	 */
	@PostMapping("register")
	public String register(@RequestParam("uid") String uid,
	                       @RequestParam("name") String name,
	                       @RequestParam("password") String password,
	                       @RequestParam("email") String email) {

		return mainService.addUser(uid, name, password, email) ? "成功添加！" : "添加失败！";
	}

	/**
	 * 删除用户接口
	 *
	 * @param uid 学号
	 * @return 状态
	 */
	@GetMapping("delete")
	public String delete(@RequestParam("uid") String uid) {
		return mainService.removeUser(uid) ? "成功！" : "失败！";
	}
}
