package common.util.fcm;

import java.io.FileInputStream;

import org.junit.jupiter.api.Test;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
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
public class FcmPushTest {

	@Test
	public void test() {
        try {
            // Firebase 초기화
            FileInputStream serviceAccount = new FileInputStream("d:/test/webpushtest-6bb7d-firebase-adminsdk-6qup9-58a2645a0a.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

            // 푸시 알림 전송
            this.sendPushNotification("dHkWFfuRRCxg0tsbgg3RqR:APA91bGkuTJEGcq7BcVC5OEt_A7VbY9g_yFf_UUjRCBwHnVPdaartFVegwKMLqX8H_7P94ItEAqzeR9AcvV_6tQh7RuqtcjKHCkI-wAS1Yk1lfoB9cigU4U");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void sendPushNotification(String token) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle("테스트 알림")
                        .setBody("FCM 푸시 알림을 main 메소드에서 실행합니다.")
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("푸시 알림 전송 성공: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
