package dluck.fuckreport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * URL映射的Controller
 */
@Controller
public class MainController {

	/**
	 * 主页
	 * @return 主页
	 */
	@GetMapping("")
	public String index(){
		return "index";
	}

	/**
	 * 注册一个账户
	 * @return 注册页面
	 */
	@GetMapping("register")
	public String register() {
		return "register";
	}

	/**
	 * 删除一个账户
	 * @return 删除页面
	 */
	@GetMapping("delete")
	public String delete(){
		return "delete";
	}
}