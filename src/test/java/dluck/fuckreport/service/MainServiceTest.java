package dluck.fuckreport.service;

import com.google.gson.Gson;
import dluck.fuckreport.FuckreportApplication;
import org.apache.http.NameValuePair;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = FuckreportApplication.class)
class MainServiceTest {

	@Autowired
	private MainService mainService;

	@Test
	void getPostData() {
		Gson gson = new Gson();
		try {
			for (NameValuePair item : mainService.getPostData("201703880")) {
				System.out.println(item.getName() + " : " + item.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void report() {
		System.out.println(mainService.report("201703880"));
	}

	@Test
	void check() {
		System.out.println(mainService.check("201703880"));
	}
}