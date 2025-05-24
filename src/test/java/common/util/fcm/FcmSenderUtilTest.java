package common.util.fcm;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import common.util.push.FcmSenderUtil;

/**
 * <pre>
 * -----------------------------------
 * 개정이력
 * -----------------------------------
 * 2025. 5. 17. kdk	최초작성
 * </pre>
 *
 *
 * @author kdk
 */
class FcmSenderUtilTest {

//	@Test
	void 싱글_테스트() throws IOException {
		String serviceAccountKeyJsonPath = "d:/test/webpushtest-6bb7d-firebase-adminsdk-6qup9-58a2645a0a.json";
		String token = "dHkWFfuRRCxg0tsbgg3RqR:APA91bGkuTJEGcq7BcVC5OEt_A7VbY9g_yFf_UUjRCBwHnVPdaartFVegwKMLqX8H_7P94ItEAqzeR9AcvV_6tQh7RuqtcjKHCkI-wAS1Yk1lfoB9cigU4U";

		boolean result = FcmSenderUtil.PushSingle.sendPushNotification(serviceAccountKeyJsonPath, token, "테스트", "푸시 테스트 입니다");
		System.out.println( result );
	}

//	@Test
	void 멀티_테스트() throws IOException {
		String serviceAccountKeyJsonPath = "d:/test/webpushtest-6bb7d-firebase-adminsdk-6qup9-58a2645a0a.json";
		String token = "dHkWFfuRRCxg0tsbgg3RqR:APA91bGkuTJEGcq7BcVC5OEt_A7VbY9g_yFf_UUjRCBwHnVPdaartFVegwKMLqX8H_7P94ItEAqzeR9AcvV_6tQh7RuqtcjKHCkI-wAS1Yk1lfoB9cigU4U";

		List<String> tokkens = List.of(token);

		boolean result = FcmSenderUtil.PushEch.sendPushNotification(serviceAccountKeyJsonPath, tokkens, "테스트", "푸시 테스트 입니다");
		System.out.println( result );
	}

	@Test
	void 토픽_테스트() throws IOException {
		String serviceAccountKeyJsonPath = "d:/test/webpushtest-6bb7d-firebase-adminsdk-6qup9-58a2645a0a.json";
		String token = "dHkWFfuRRCxg0tsbgg3RqR:APA91bGkuTJEGcq7BcVC5OEt_A7VbY9g_yFf_UUjRCBwHnVPdaartFVegwKMLqX8H_7P94ItEAqzeR9AcvV_6tQh7RuqtcjKHCkI-wAS1Yk1lfoB9cigU4U";
		String topic = "test_topic";

		List<String> tokkens = List.of(token);

		boolean subscribeResult = FcmSenderUtil.PushTopic.subscribeToTopic(serviceAccountKeyJsonPath, tokkens, topic);
		System.out.println("토픽 구독 : " + subscribeResult);

		boolean result = FcmSenderUtil.PushTopic.sendPushNotification(serviceAccountKeyJsonPath, topic, "테스트", "푸시 테스트 입니다");
		System.out.println("토픽 발송 : " + result);

		boolean unsubscribeResult = FcmSenderUtil.PushTopic.unsubscribeFromTopic(serviceAccountKeyJsonPath, tokkens, topic);
		System.out.println("토픽 구독 취소 : " + unsubscribeResult);
	}

}
