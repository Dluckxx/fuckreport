package dluck.fuckreport.service;

import dluck.fuckreport.FuckreportApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = FuckreportApplication.class)
class MainServiceTest {

	@Autowired
	private MainService mainService;

	@Test
	void report() {
	}

	@Test
	void check() {
		System.out.println(mainService.check("201703880"));
	}
}