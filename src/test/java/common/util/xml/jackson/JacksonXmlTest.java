package common.util.xml.jackson;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import common.util.xml.JacksonXmlUtil;

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
class JacksonXmlTest {

//	@Test
	void 객체_변환_테스트() {
		JacksonShop shop = new JacksonShop();
		shop.setCity("서울");
		shop.setType("한식");

		List<JacksonFood> list = new ArrayList<>();

		JacksonFood food = null;

		food = new JacksonFood();
		food.setName("사과");
		food.setSort("과일");
		food.setCost(1000);
		list.add(food);

		food = new JacksonFood();
		food.setName("바나나");
		food.setSort("과일");
		food.setCost(1500);
		list.add(food);

		shop.setFood(list);

		String xmlStr = JacksonXmlUtil.ToXml.converterObjToXmlStr(shop, true);
		System.out.println(xmlStr);
	}

//	@Test
	void 스트림_읽기_테스트() {
		String xmlFilePath = "common/util/xml/shop.xml";

		try ( InputStream is = this.getClass().getClassLoader().getResourceAsStream(xmlFilePath) ) {
			if ( is != null ) {
				JacksonShop shop = JacksonXmlUtil.FromXml.converterXmlStreamToClass(is, JacksonShop.class);
				System.out.println(shop);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	@Test
	void 문자열_읽기_테스트() {
		String xmlFilePath = "common/util/xml/shop.xml";
		StringBuilder sb = new StringBuilder();

		try (
				InputStream is = this.getClass().getClassLoader().getResourceAsStream(xmlFilePath);
				InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
				BufferedReader br = new BufferedReader(isr)
		) {
			if ( is != null ) {
				String line;
				while ( (line = br.readLine()) != null ) {
					sb.append(line).append("\n");
				}

				String xmlStr = sb.toString();

				JacksonShop shop = JacksonXmlUtil.FromXml.converterXmlStrToClass(xmlStr, JacksonShop.class);
				System.out.println(shop);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(true);
	}

	@Test
	void 파일_일기_테스트() {
		String xmlFilePath = "common/util/xml/shop.xml";
		File file = new File(this.getClass().getClassLoader().getResource(xmlFilePath).getFile());

		TypeReference<JacksonShop> typeReference = new TypeReference<JacksonShop>() {};
		JacksonShop shop = (JacksonShop) JacksonXmlUtil.ReadXmlFile.convertXmlFileToObject(file, typeReference);

		System.out.println(shop);
		assertTrue(true);
	}

}
