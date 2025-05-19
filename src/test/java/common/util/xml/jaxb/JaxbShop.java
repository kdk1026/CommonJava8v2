package common.util.xml.jaxb;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
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
@ToString
@XmlRootElement(name = "shop")
public class JaxbShop {

	private String city;
	private String type;
	private List<JaxbFood> food;

	@XmlAttribute
	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	@XmlAttribute
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name = "food")
	/**
	 * @return the food
	 */
	public List<JaxbFood> getFood() {
		return food;
	}

	/**
	 * @param food the food to set
	 */
	public void setFood(List<JaxbFood> food) {
		this.food = food;
	}

}
