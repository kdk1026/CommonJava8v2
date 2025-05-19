package common.util.xml.jackson;

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
public class JacksonFood {

	private String name;
	private String sort;
	private int cost;

}
