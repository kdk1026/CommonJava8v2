package common.util.xml.jaxb;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
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
@XmlType(propOrder = {"name", "sort", "cost"})
public class JaxbFood {

	private String name;
	private String sort;
	private int cost;

	@XmlElement
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	/**
	 * @return the sort
	 */
	public String getSort() {
		return sort;
	}

	/**
	 * @param sort the sort to set
	 */
	public void setSort(String sort) {
		this.sort = sort;
	}

	@XmlElement
	/**
	 * @return the cost
	 */
	public int getCost() {
		return cost;
	}

	/**
	 * @param cost the cost to set
	 */
	public void setCost(int cost) {
		this.cost = cost;
	}

}
