package common.util.push.apns;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <pre>
 * -----------------------------------
 * 개정이력
 * -----------------------------------
 * 2025. 5. 26. kdk	최초작성
 * </pre>
 *
 *
 * @author kdk
 */
@Getter
@Setter
@ToString
public class ApnsSettingVo {

	private ApnsSettingVo() {
		super();
	}

	private String p8FilePath;
	private String keyId;
	private String teamId;
	private String bundleId;

}
