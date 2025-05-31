package common.util.push;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

/**
 * <pre>
 * -----------------------------------
 * 개정이력
 * -----------------------------------
 * 2025. 5. 17. kdk	 최초작성
 * 2025. 5. 27. 김대광 제미나이에 의한 코드 개선
 * </pre>
 *
 * firebase-admin 기반
 *
 * @author kdk
 */
public class FcmSenderUtil {

	private static final Logger logger = LoggerFactory.getLogger(FcmSenderUtil.class);

	private FcmSenderUtil() {
		super();
	}

	private static final String TITLE_NULL_ERROR = "title는 null일 수 없습니다.";
	private static final String BODY_NULL_ERROR = "body는 null일 수 없습니다.";
	private static final String TOPIC_NULL_ERROR = "topic는 null일 수 없습니다.";
	private static final String IMAGE_URL_NULL_ERROR = "imageUrl는 null일 수 없습니다.";

    private static FirebaseMessaging firebaseMessaging;

    public static synchronized void initialize(String serviceAccountKeyJsonPath) throws IOException {
		if ( StringUtils.isBlank(serviceAccountKeyJsonPath) ) {
			throw new IllegalArgumentException("serviceAccountKeyJsonPath는 null일 수 없습니다.");
		}

    	if (firebaseMessaging != null) {
            logger.warn("Firebase Messaging이 이미 초기화되었습니다. 추가 초기화 시도는 무시됩니다.");
            return;
        }

    	if (FirebaseApp.getApps().isEmpty()) {
            try (FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyJsonPath)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                // 'default' 앱이 이미 초기화된 경우를 대비하여 이름을 부여하거나 기본 앱을 사용
                FirebaseApp app;
                try {
                    app = FirebaseApp.initializeApp(options);
                } catch (IllegalStateException e) {
                    // "Default FirebaseApp is already initialized" 예외 처리
                    logger.warn("기본 Firebase 앱이 이미 초기화되어 있습니다. 기존 앱 인스턴스를 사용합니다.");
                    app = FirebaseApp.getInstance();
                }
                firebaseMessaging = FirebaseMessaging.getInstance(app);
            }
    	} else {
            // 이미 다른 곳에서 FirebaseApp이 초기화된 경우, 해당 인스턴스를 사용
            firebaseMessaging = FirebaseMessaging.getInstance();
        }
        logger.info("Firebase Messaging이 성공적으로 초기화되었습니다.");
    }

    // 초기화 여부를 확인하고 firebaseMessaging 인스턴스를 반환하는 private static 메서드
    private static FirebaseMessaging getFirebaseMessagingInstance() {
        if (firebaseMessaging == null) {
            throw new IllegalStateException("FcmSenderUtil이 초기화되지 않았습니다. 사용 전에 FcmSenderUtil.initialize()를 호출해주세요.");
        }
        return firebaseMessaging;
    }

	/**
	 * <pre>
	 * 푸시 알림 전송 (단일)
	 * </pre>
	 */
	public static class PushSingle {
		private PushSingle() {
			super();
		}

		private static boolean sendPush(String deviceToken, String title, String body, String imageUrl, Map<String, String> data) {
			if ( StringUtils.isBlank(deviceToken) ) {
				throw new IllegalArgumentException("deviceToken는 null일 수 없습니다.");
			}

			if ( StringUtils.isBlank(title) ) {
				throw new IllegalArgumentException(TITLE_NULL_ERROR);
			}

			if ( StringUtils.isBlank(body) ) {
				throw new IllegalArgumentException(BODY_NULL_ERROR);
			}

			Message message = Message.builder()
					.setToken(deviceToken)
					.setNotification(Notification.builder()
							.setTitle(title)
							.setBody(body)
							.setImage(StringUtils.isBlank(imageUrl) ? null : imageUrl)
							.build())
					.putAllData(data == null || data.isEmpty() ? new HashMap<>() : data)
					.build();

			try {
				String response = getFirebaseMessagingInstance().send(message);
				logger.debug("푸시 알림 전송 성공: {}", response);
				return true;
			} catch (FirebaseMessagingException e) {
				logger.error("푸시 알림 전송 실패", e);
				return false;
			}
		}

		public static boolean sendPushNotification(String deviceToken, String title, String body) {
			return sendPush(deviceToken, title, body, null, null);
		}

		public static boolean sendPushNotification(String deviceToken, String title, String body, Map<String, String> data) {
			return sendPush(deviceToken, title, body, null, data);
		}

		public static boolean sendPushNotification(String deviceToken, String title, String body, String imageUrl) {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException(IMAGE_URL_NULL_ERROR);
			}

			return sendPush(deviceToken, title, body, imageUrl, null);
		}

		public static boolean sendPushNotification(String deviceToken, String title, String body, String imageUrl, Map<String, String> data) {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException(IMAGE_URL_NULL_ERROR);
			}

			return sendPush(deviceToken, title, body, imageUrl, data);
		}
	}

	/**
	 * <pre>
	 * 푸시 알림 전송 (다수)
	 *  - 500개 이상의 토큰을 전송할 경우, 500개씩 나누어 전송해야 함
	 *  - 성능을 위해 PushTopic 사용 권장
	 * </pre>
	 */
	public static class PushEch {
		private PushEch() {
			super();
		}

		private static boolean sendPushEach(List<String> deviceTokens, String title, String body, String imageUrl, Map<String, String> data) {
			if ( deviceTokens == null || deviceTokens.isEmpty() ) {
				throw new IllegalArgumentException("deviceTokens is null");
			}

			if ( deviceTokens.size() > 500 ) {
				throw new IllegalArgumentException("deviceTokens size is over 500");
			}

			if ( StringUtils.isBlank(title) ) {
				throw new IllegalArgumentException(TITLE_NULL_ERROR);
			}

			if ( StringUtils.isBlank(body) ) {
				throw new IllegalArgumentException(BODY_NULL_ERROR);
			}

			List<Message> messages = new ArrayList<>();

			for (String deviceToken : deviceTokens) {
				Message message = Message.builder()
						.setToken(deviceToken)
						.setNotification(Notification.builder()
								.setTitle(title)
								.setBody(body)
								.setImage(StringUtils.isBlank(imageUrl) ? null : imageUrl)
								.build())
						.putAllData(data == null || data.isEmpty() ? new HashMap<>() : data)
						.build();

				messages.add(message);
			}

			try {
				BatchResponse response = getFirebaseMessagingInstance().sendEach(messages);
				logger.debug("푸시 알림 전송 성공 개수: {}", response.getSuccessCount());
				logger.debug("푸시 알림 전송 실패 개수: {}", response.getFailureCount());
				return true;
			} catch (FirebaseMessagingException e) {
				logger.error("푸시 알림 전송 실패", e);
				return false;
			}
		}

		public static boolean sendPushNotification(List<String> deviceTokens, String title, String body) {
			return sendPushEach(deviceTokens, title, body, null, null);
		}

		public static boolean sendPushNotification(List<String> deviceTokens, String title, String body, Map<String, String> data) {
			return sendPushEach(deviceTokens, title, body, null, data);
		}

		public static boolean sendPushNotification(List<String> deviceTokens, String title, String body, String imageUrl) {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException(IMAGE_URL_NULL_ERROR);
			}

			return sendPushEach(deviceTokens, title, body, imageUrl, null);
		}

		public static boolean sendPushNotification(List<String> deviceTokens, String title, String body, String imageUrl, Map<String, String> data) {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException(IMAGE_URL_NULL_ERROR);
			}

			return sendPushEach(deviceTokens, title, body, imageUrl, data);
		}
	}

	/**
	 * <pre>
	 * 토픽 구독 및 푸시 알림 전송
	 *  - 내용이 다른 푸시 알림을 연달아 보낼 수도 있으므로 다음 순서대로 호출하여 사용
	 *  	1. 토픽 구독
	 *  	2. 푸시 전송
	 *  	3. 토픽 구독 취소
	 * </pre>
	 */
	public static class PushTopic {
		private PushTopic() {
			super();
		}

		public static boolean subscribeToTopic(List<String> deviceTokens, String topic) {
			if ( deviceTokens == null || deviceTokens.isEmpty() ) {
				throw new IllegalArgumentException("deviceTokens is null");
			}

			if ( StringUtils.isBlank(topic) ) {
				throw new IllegalArgumentException(TOPIC_NULL_ERROR);
			}

	        try {
	        	getFirebaseMessagingInstance().subscribeToTopic(deviceTokens, topic);
	            logger.debug("토픽 구독 성공: {}", topic);
	            return true;
	        } catch (FirebaseMessagingException e) {
	            logger.error("토픽 구독 실패: {}", topic, e);
	            return false;
	        }
	    }

		public static boolean unsubscribeFromTopic(List<String> deviceTokens, String topic) {
			if ( deviceTokens == null || deviceTokens.isEmpty() ) {
				throw new IllegalArgumentException("deviceTokens is null");
			}

			if ( StringUtils.isBlank(topic) ) {
				throw new IllegalArgumentException(TOPIC_NULL_ERROR);
			}

		    try {
		    	getFirebaseMessagingInstance().unsubscribeFromTopic(deviceTokens, topic);
		        logger.debug("토픽 구독 취소 성공: {}", topic);
		        return true;
		    } catch (FirebaseMessagingException  e) {
		        logger.error("토픽 구독 취소 실패: {}", topic, e);
		        return false;
		    }
		}

		private static boolean sendTopicMessage(String topic, String title, String body, String imageUrl, Map<String, String> data) {
			if ( StringUtils.isBlank(topic) ) {
				throw new IllegalArgumentException(TOPIC_NULL_ERROR);
			}

			if ( StringUtils.isBlank(title) ) {
				throw new IllegalArgumentException(TITLE_NULL_ERROR);
			}

			if ( StringUtils.isBlank(body) ) {
				throw new IllegalArgumentException(BODY_NULL_ERROR);
			}

	        Message message = Message.builder()
	                .setTopic(topic)
	                .setNotification(Notification.builder()
	                		.setTitle(title)
							.setBody(body)
							.setImage(StringUtils.isBlank(imageUrl) ? null : imageUrl)
	                        .build())
	                .putAllData(data == null || data.isEmpty() ? new HashMap<>() : data)
	                .build();

	        try {
	            String response = getFirebaseMessagingInstance().send(message);
	            logger.info("토픽 메시지 전송 성공: {}", response);
	            return true;
	        } catch (FirebaseMessagingException e) {
	            logger.error("토픽 메시지 전송 실패", e);
	            return false;
	        }
		}

		public static boolean sendPushNotification( String topic, String title, String body) {
			return sendTopicMessage(topic, title, body, null, null);
		}

		public static boolean sendPushNotification(String topic, String title, String body, Map<String, String> data) {
			return sendTopicMessage(topic, title, body, null, data);
		}

		public static boolean sendPushNotification(String topic, String title, String body, String imageUrl) {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException(IMAGE_URL_NULL_ERROR);
			}

			return sendTopicMessage(topic, title, body, imageUrl, null);
		}

		public static boolean sendPushNotification(String topic, String title, String body, String imageUrl, Map<String, String> data) {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException(IMAGE_URL_NULL_ERROR);
			}

			return sendTopicMessage(topic, title, body, imageUrl, data);
		}
	}

}
