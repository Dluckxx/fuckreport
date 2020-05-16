package dluck.fuckreport.vo;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PZDataVoTest {

	@Test
	public void test() {
		Gson gson = new Gson();

		PZDataVo pzDataVo = new PZDataVo("否");
		pzDataVo.setSelectId("08bbb0ac-3914-42b6-838e-68c01f9cef03");
		pzDataVo.setTitleId("c34ebc78-404c-47d7-a5bc-1acbd39c5d0c");
		pzDataVo.setOptionType("0");

		List<PZDataVo> list = new ArrayList<>();
		list.add(pzDataVo);
		list.add(pzDataVo);
		list.add(pzDataVo);
		list.add(pzDataVo);

		System.out.println(gson.toJson(list));
	}
}