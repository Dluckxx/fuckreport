package dluck.fuckreport.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * URL接口Controller
 */
@RestController
@RequestMapping("api")
public class ApiController {

	/**
	 * 打卡接口
	 * @return 打卡信息
	 */
	@GetMapping("report")
	public String report(){
		return "暂未配置该接口";
	}

	@GetMapping("register")
	public String register(){
		return "暂未配置该接口";
	}

	@GetMapping("delete")
	public String delete(){
		return "暂未配置该接口";
	}
}
