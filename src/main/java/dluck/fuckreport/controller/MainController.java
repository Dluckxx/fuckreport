package dluck.fuckreport.controller;

import dluck.fuckreport.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * URL映射的Controller
 */
@Controller
public class MainController {

	private final MainService mainService;

	@Autowired
	public MainController(MainService mainService) {
		this.mainService = mainService;
	}

	/**
	 * 主页
	 * @return 主页
	 */
	@GetMapping("")
	public String index(Model model){
		model.addAttribute("userList", mainService.getAllUser());
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