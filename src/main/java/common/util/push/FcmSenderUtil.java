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
 * 2025. 5. 17. kdk	최초작성
 * </pre>
 *
 *
 * @author kdk
 */
public class FcmSenderUtil {

	private static final Logger logger = LoggerFactory.getLogger(FcmSenderUtil.class);

	private static FcmSenderUtil instance;
    private FirebaseMessaging firebaseMessaging;

	private FcmSenderUtil() {
		super();
	}

	private static synchronized FcmSenderUtil getInstance(String serviceAccountKeyJsonPath) throws IOException {
        if (instance == null) {
			instance = new FcmSenderUtil();
			instance.initialize(serviceAccountKeyJsonPath);
        } else if (instance.firebaseMessaging == null) {
        	instance.initialize(serviceAccountKeyJsonPath);
        }

        return instance;
    }

    private void initialize(String serviceAccountKeyJsonPath) throws IOException {
		if ( StringUtils.isBlank(serviceAccountKeyJsonPath) ) {
			throw new IllegalArgumentException("serviceAccountKeyJsonPath is null");
		}

		if (FirebaseApp.getApps().isEmpty()) {
			try (FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyJsonPath)) {
				FirebaseOptions options = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(serviceAccount))
						.build();

				FirebaseApp app = FirebaseApp.initializeApp(options);
				firebaseMessaging = FirebaseMessaging.getInstance(app);
			}
		} else {
			firebaseMessaging = FirebaseMessaging.getInstance();
		}
    }

	private boolean sendPush(String token, String title, String body, String imageUrl, Map<String, String> data) {
		if ( StringUtils.isBlank(token) ) {
			throw new IllegalArgumentException("token is null");
		}

		if ( StringUtils.isBlank(title) ) {
			throw new IllegalArgumentException("title is null");
		}

		if ( StringUtils.isBlank(body) ) {
			throw new IllegalArgumentException("body is null");
		}

		Message message = Message.builder()
				.setToken(token)
				.setNotification(Notification.builder()
						.setTitle(title)
						.setBody(body)
						.setImage(StringUtils.isBlank(imageUrl) ? null : imageUrl)
						.build())
				.putAllData(data == null || data.isEmpty() ? new HashMap<>() : data)
				.build();

		try {
			String response = firebaseMessaging.send(message);
			logger.debug("푸시 알림 전송 성공: {}", response);
			return true;
		} catch (FirebaseMessagingException e) {
			logger.error("푸시 알림 전송 실패", e);
			return false;
		}
	}

	private boolean sendPushEach(List<String> tokens, String title, String body, String imageUrl, Map<String, String> data) {
		if ( tokens == null || tokens.isEmpty() ) {
			throw new IllegalArgumentException("tokens is null");
		}

		if ( tokens.size() > 500 ) {
			throw new IllegalArgumentException("tokens size is over 500");
		}

		if ( StringUtils.isBlank(title) ) {
			throw new IllegalArgumentException("title is null");
		}

		if ( StringUtils.isBlank(body) ) {
			throw new IllegalArgumentException("body is null");
		}

		List<Message> messages = new ArrayList<>();

		for (String token : tokens) {
			Message message = Message.builder()
					.setToken(token)
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
			BatchResponse response = firebaseMessaging.sendEach(messages);
			logger.debug("푸시 알림 전송 성공: {}", response.getSuccessCount());
			return true;
		} catch (FirebaseMessagingException e) {
			logger.error("푸시 알림 전송 실패", e);
			return false;
		}
	}

	private boolean sendTopicMessage(String topic, String title, String body, String imageUrl, Map<String, String> data) {
		if ( StringUtils.isBlank(topic) ) {
			throw new IllegalArgumentException("topic is null");
		}

		if ( StringUtils.isBlank(title) ) {
			throw new IllegalArgumentException("title is null");
		}

		if ( StringUtils.isBlank(body) ) {
			throw new IllegalArgumentException("body is null");
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
            String response = firebaseMessaging.send(message);
            logger.info("토픽 메시지 전송 성공: {}", response);
            return true;
        } catch (FirebaseMessagingException e) {
            logger.error("토픽 메시지 전송 실패", e);
            return false;
        }
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

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, String token, String title, String body) throws IOException {
			getInstance(serviceAccountKeyJsonPath);
			return instance.sendPush(token, title, body, null, null);
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, String token, String title, String body, Map<String, String> data) throws IOException {
			getInstance(serviceAccountKeyJsonPath);
			return instance.sendPush(token, title, body, null, data);
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, String token, String title, String body, String imageUrl) throws IOException {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException("imageUrl is null");
			}

			getInstance(serviceAccountKeyJsonPath);
			return instance.sendPush(token, title, body, imageUrl, null);
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, String token, String title, String body, String imageUrl, Map<String, String> data) throws IOException {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException("imageUrl is null");
			}

			getInstance(serviceAccountKeyJsonPath);
			return instance.sendPush(token, title, body, imageUrl, data);
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

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, List<String> tokens, String title, String body) throws IOException {
			getInstance(serviceAccountKeyJsonPath);
			return instance.sendPushEach(tokens, title, body, null, null);
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, List<String> tokens, String title, String body, Map<String, String> data) throws IOException {
			getInstance(serviceAccountKeyJsonPath);
			return instance.sendPushEach(tokens, title, body, null, data);
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, List<String> tokens, String title, String body, String imageUrl) throws IOException {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException("imageUrl is null");
			}

			getInstance(serviceAccountKeyJsonPath);
			return instance.sendPushEach(tokens, title, body, imageUrl, null);
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, List<String> tokens, String title, String body, String imageUrl, Map<String, String> data) throws IOException {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException("imageUrl is null");
			}

			getInstance(serviceAccountKeyJsonPath);
			return instance.sendPushEach(tokens, title, body, imageUrl, data);
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

		public static boolean subscribeToTopic(String serviceAccountKeyJsonPath, List<String> tokens, String topic) {
			if ( tokens == null || tokens.isEmpty() ) {
				throw new IllegalArgumentException("tokens is null");
			}

			if ( StringUtils.isBlank(topic) ) {
				throw new IllegalArgumentException("topic is null");
			}

	        try {
	        	getInstance(serviceAccountKeyJsonPath);
	        	instance.firebaseMessaging.subscribeToTopic(tokens, topic);
	            logger.debug("토픽 구독 성공: {}", topic);
	            return true;
	        } catch (FirebaseMessagingException | IOException e) {
	            logger.error("토픽 구독 실패: {}", topic, e);
	            return false;
	        }
	    }

		public static boolean unsubscribeFromTopic(String serviceAccountKeyJsonPath, List<String> tokens, String topic) {
			if ( tokens == null || tokens.isEmpty() ) {
				throw new IllegalArgumentException("tokens is null");
			}

			if ( StringUtils.isBlank(topic) ) {
				throw new IllegalArgumentException("topic is null");
			}

		    try {
		    	getInstance(serviceAccountKeyJsonPath);
		    	instance.firebaseMessaging.unsubscribeFromTopic(tokens, topic);
		        logger.debug("토픽 구독 취소 성공: {}", topic);
		        return true;
		    } catch (FirebaseMessagingException | IOException e) {
		        logger.error("토픽 구독 취소 실패: {}", topic, e);
		        return false;
		    }
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, String topic, String title, String body) throws IOException {
			getInstance(serviceAccountKeyJsonPath);
			return instance.sendTopicMessage(topic, title, body, null, null);
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, String topic, String title, String body, Map<String, String> data) throws IOException {
			getInstance(serviceAccountKeyJsonPath);
			return instance.sendTopicMessage(topic, title, body, null, data);
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, String topic, String title, String body, String imageUrl) throws IOException {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException("imageUrl is null");
			}

			getInstance(serviceAccountKeyJsonPath);
			return instance.sendTopicMessage(topic, title, body, imageUrl, null);
		}

		public static boolean sendPushNotification(String serviceAccountKeyJsonPath, String topic, String title, String body, String imageUrl, Map<String, String> data) throws IOException {
			if ( StringUtils.isBlank(imageUrl) ) {
				throw new IllegalArgumentException("imageUrl is null");
			}

			getInstance(serviceAccountKeyJsonPath);
			return instance.sendTopicMessage(topic, title, body, imageUrl, data);
		}
	}

}
