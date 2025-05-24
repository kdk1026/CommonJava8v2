package common.util.xml.jaxb;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import common.util.xml.JaxbXmlUtil;

/**
 * <pre>
 * -----------------------------------
 * 개정이력
 * -----------------------------------
 * 2025. 5. 19. kdk	최초작성
 * </pre>
 *
 *
 * @author kdk
 */
class JaxbXmlTest {

//	@Test
	void 객체_변환_테스트() {
		JaxbShop shop = new JaxbShop();
		shop.setCity("서울");
		shop.setType("한식");

		List<JaxbFood> list = new ArrayList<>();

		JaxbFood food = null;

		food = new JaxbFood();
		food.setName("사과");
		food.setSort("과일");
		food.setCost(1000);
		list.add(food);

		food = new JaxbFood();
		food.setName("바나나");
		food.setSort("과일");
		food.setCost(1500);
		list.add(food);

		shop.setFood(list);

		String xmlStr = JaxbXmlUtil.ToXml.converterObjToXmlStr(shop, true);
		System.out.println(xmlStr);
	}

//	@Test
	void 스트림_읽기_테스트() {
		String xmlFilePath = "common/util/xml/shop.xml";

		try ( InputStream is = this.getClass().getClassLoader().getResourceAsStream(xmlFilePath) ) {
			if ( is != null ) {
				JaxbShop shop = JaxbXmlUtil.FromXml.converterXmlStreamToClass(is, JaxbShop.class);
				System.out.println(shop);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void 파일_일기_테스트() {
		String xmlFilePath = "common/util/xml/shop.xml";
		File file = new File(this.getClass().getClassLoader().getResource(xmlFilePath).getFile());

		JaxbShop shop = JaxbXmlUtil.ReadXmlFile.convertXmlFileToObject(file, JaxbShop.class);

		System.out.println(shop);
	}

}
