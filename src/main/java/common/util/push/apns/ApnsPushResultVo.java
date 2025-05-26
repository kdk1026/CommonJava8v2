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
public class ApnsPushResultVo {

	private String deviceToken;
	private boolean success;
	private int statusCode;
	private String reason;

    public ApnsPushResultVo(String deviceToken, boolean success, int statusCode, String reason) {
        this.deviceToken = deviceToken;
        this.success = success;
        this.statusCode = statusCode;
        this.reason = reason;
    }

}
