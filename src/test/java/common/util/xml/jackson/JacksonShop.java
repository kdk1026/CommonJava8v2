package common.util.xml.jackson;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "shop")
public class JacksonShop {

	@JacksonXmlProperty(isAttribute = true)
	private String city;

	@JacksonXmlProperty(isAttribute = true)
	private String type;

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "food")
	private List<JacksonFood> food;

}
