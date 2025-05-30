package common.util.push.apns;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.jsonwebtoken.Jwts;

/**
 * <pre>
 * -----------------------------------
 * 개정이력
 * -----------------------------------
 * 2025. 5. 26. kdk	최초작성
 * </pre>
 *
 * httpclient5, jackson-databind, jjwt-api 기반
 *
 * <pre>
 * "토픽 구독" 및 "토픽 기반 대량 발송" 기능 없음
 *  - FCM 연동 시, 토픽 발송 가능
 * </pre>
 *
 * <pre>
 * 제미나이 이용해서 구현은 했으나 개복잡... FCM 연동해서 편하게 쓰는게 좋을 듯
 * </pre>
 *
 * @author kdk
 */
public class ApnsSenderUtil {

	private static final Logger logger = LoggerFactory.getLogger(ApnsSenderUtil.class);

	private static ApnsSenderUtil instance;

	private ApnsSenderUtil() {
		super();
	}

	private static final String APNS_DEVELOPMENT_URL = "https://api.development.push.apple.com";
    private static final String APNS_PRODUCTION_URL = "https://api.push.apple.com";

    private String p8FilePath;
    private String keyId;
    private String teamId;
    private String bundleId;

    private CloseableHttpAsyncClient httpClient;
    private PrivateKey privateKey;
    private String jwtToken;
    private long lastTokenGeneratedTime;
    private ObjectMapper objectMapper;

    // 병렬 처리를 위한 스레드 풀
    private ExecutorService executorService;
    // APNS 요청은 네트워크 I/O가 많으므로 CPU 코어 수보다 많은 스레드가 유리할 수 있습니다.
    // 여기서는 고정된 스레드 풀 크기를 설정합니다. 실제 환경에서는 적절히 조정하세요.
    private static final int THREAD_POOL_SIZE = 10; // 예시: 10개 스레드

	private static synchronized ApnsSenderUtil getInstance(String p8FilePath, String keyId, String teamId, String bundleId) throws IOException {
		Objects.requireNonNull(p8FilePath.trim(), "p8FilePath cannot be null");
		Objects.requireNonNull(keyId.trim(), "keyId cannot be null");
		Objects.requireNonNull(teamId.trim(), "teamId cannot be null");
		Objects.requireNonNull(bundleId.trim(), "bundleId cannot be null");

        if (instance == null) {
			instance = new ApnsSenderUtil();
			instance.p8FilePath = p8FilePath;
			instance.keyId = keyId;
			instance.teamId = teamId;
			instance.bundleId = bundleId;
			instance.initialize();
        } else {
        	if (!instance.p8FilePath.equals(p8FilePath) || !instance.keyId.equals(keyId) ||
                !instance.teamId.equals(teamId) || !instance.bundleId.equals(bundleId)) {
                logger.warn("Attempted to initialize ApnsSenderUtil with different settings. Using existing instance's settings.");
            }
        }

        return instance;
    }

	private void initialize() throws IOException {
		IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(10))
                .build();

		PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnTotal(THREAD_POOL_SIZE * 2) // 전체 연결 수
                .setMaxConnPerRoute(THREAD_POOL_SIZE) // 경로(APNS 엔드포인트)당 최대 연결 수
                .build();

		this.httpClient = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .setConnectionManager(connectionManager)
                .build();
        this.httpClient.start();

        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try {
            this.privateKey = loadPrivateKey(instance.p8FilePath);
            generateAndSetJwtToken();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        	throw new IOException("Failed to initialize APNS sender due to key loading or JWT generation.", e);
        }
	}

    private PrivateKey loadPrivateKey(String p8FilePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String keyContent = new String(Files.readAllBytes(Paths.get(p8FilePath)))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(keySpec);
    }

    private synchronized void generateAndSetJwtToken() {
    	// JWT 유효기간은 1시간을 넘을 수 없습니다. 보통 50분 정도로 설정합니다.
    	Instant now = Instant.now();
    	long secondsToAdd = 60L * 50;
        Instant expiry = now.plusSeconds(secondsToAdd); // 50분 후 만료

        this.jwtToken = Jwts.builder()
        		.header()
        			.keyId(instance.keyId)
        		.and()
        		.claims()
        			.issuer(instance.teamId)
        			.issuedAt(Date.from(now))
        			.expiration(Date.from(expiry))
        		.and()
        		.signWith(privateKey, Jwts.SIG.ES256)
                .compact();
        this.lastTokenGeneratedTime = System.currentTimeMillis();
        logger.debug("JWT Token generated: {}", this.jwtToken);
    }

    private void ensureJwtTokenIsValid() {
        if (System.currentTimeMillis() - lastTokenGeneratedTime > TimeUnit.MINUTES.toMillis(50)) {
        	logger.debug("Refreshing JWT token...");
            this.generateAndSetJwtToken();
        }
    }

    private CompletableFuture<ApnsPushResultVo> sendPushAsync(String deviceToken, String title, String body, boolean isProduction, Map<String, String> data) {
    	if (StringUtils.isBlank(deviceToken)) {
            return CompletableFuture.completedFuture(new ApnsPushResultVo(null, false, -1, "Device token is null or empty"));
        }
        if (StringUtils.isBlank(title) && StringUtils.isBlank(body)) {
             return CompletableFuture.completedFuture(new ApnsPushResultVo(deviceToken, false, -1, "Title and Body cannot both be null or empty"));
        }

		// 토큰 유효성 검사는 여러 스레드에서 동시에 접근할 수 있으므로 synchronized 블록으로 보호
        synchronized (this) {
            ensureJwtTokenIsValid();
        }

    	String apnsUrl = isProduction ? APNS_PRODUCTION_URL : APNS_DEVELOPMENT_URL;
        String pushUrl = String.format("%s/3/device/%s", apnsUrl, deviceToken);

        ObjectNode aps = objectMapper.createObjectNode();
        ObjectNode alert = objectMapper.createObjectNode();
        alert.put("title", title);
        alert.put("body", body);
        aps.set("alert", alert);
        aps.put("sound", "default");

        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("aps", aps);

        Iterator<Entry<String, String>> it = data.entrySet().iterator();
        while (it.hasNext()) {
        	Map.Entry<String, String> entry = it.next();
        	payload.put(entry.getKey(), entry.getValue());
        }

        String jsonPayload = null;
        try {
        	jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (IOException e) {
			logger.debug("Payload JSON creation failed: {}", e.getMessage(), e);
			return CompletableFuture.completedFuture(new ApnsPushResultVo(deviceToken, false, -1, "Payload JSON creation failed: " + e.getMessage()));
		}

        AsyncRequestProducer requestProducer = AsyncRequestBuilder.post(pushUrl)
                .addHeader(HttpHeaders.AUTHORIZATION, "bearer " + jwtToken)
                .addHeader("apns-topic", this.bundleId)
                .addHeader("apns-priority", "10")
                .addHeader("apns-push-type", "alert")
                .setEntity(jsonPayload, ContentType.APPLICATION_JSON)
                .build();

        AsyncResponseConsumer<SimpleHttpResponse> responseConsumer = SimpleResponseConsumer.create();
        CompletableFuture<ApnsPushResultVo> resultFuture = new CompletableFuture<>();

        httpClient.execute(
            requestProducer,
            responseConsumer,
            null,
            new FutureCallback<SimpleHttpResponse>() {
                @Override
                public void completed(SimpleHttpResponse response) {
                    int statusCode = response.getCode();
                    String responseBody = response.getBodyText();
                    logger.debug("APNS Response for {}: {} - {}", deviceToken, statusCode, responseBody);

                    boolean success = statusCode == 200;
                    String reason = success ? "Success" : "Unknown Error";

                    if (!success) {
                        try {
                            ObjectNode errorJson = (ObjectNode) objectMapper.readTree(responseBody);
                            reason = errorJson.has("reason") ? errorJson.get("reason").asText() : reason;
                        } catch (Exception e) {
                            reason = "Failed to parse error response: " + e.getMessage();
                        }
                    }
                    resultFuture.complete(new ApnsPushResultVo(deviceToken, success, statusCode, reason));
                }

                @Override
                public void failed(Exception ex) {
                    logger.debug("APNS Request failed for {} : {}", deviceToken, ex.getMessage());
                    resultFuture.completeExceptionally(ex);
                }

                @Override
                public void cancelled() {
                    resultFuture.complete(new ApnsPushResultVo(deviceToken, false, -1, "APNS Request cancelled"));
                }
            }
        );

        return resultFuture;
    }

    private List<CompletableFuture<ApnsPushResultVo>> sendMultiPushAsync(List<String> deviceTokens, String title, String body, boolean isProduction, Map<String, String> data) {
        List<CompletableFuture<ApnsPushResultVo>> futures = new ArrayList<>();
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            return futures; // 빈 리스트 반환
        }

        for (String token : deviceTokens) {
            futures.add(sendPushAsync(token, title, body, isProduction, data));
        }
        return futures;
    }

    /**
     * 중요: 애플리케이션 종료 시 한 번만 shutdown()을 호출
     */
    public static void shutdown() {
    	logger.info("Shutting down ApnsSenderUtil resources...");

    	if ( instance != null ) {
    		instance.executorService.shutdown();
    		try {
    			if (!instance.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
    				logger.warn("Executor service did not terminate in 60 seconds, forcing shutdown.");
    				instance.executorService.shutdownNow();
    			}
    		} catch (InterruptedException e) {
    			logger.error("Executor service termination interrupted.", e);
    			instance.executorService.shutdownNow();
    			Thread.currentThread().interrupt();
    		}

    		if ( instance.httpClient != null ) {
    			try {
    				instance.httpClient.close();
    				logger.info("Apache HttpClient closed.");
    			} catch (IOException e) {
    				logger.error("Error closing Apache HttpClient: {}", e.getMessage(), e);
    			}
    		}
    	}

        instance = null;
        logger.info("ApnsSenderUtil shutdown complete.");
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

		public static ApnsPushResultVo sendPushNotification(ApnsSettingVo apnsSettingVo,
	    		String deviceToken, String title, String body, boolean isProduction) throws IOException, InterruptedException, ExecutionException, TimeoutException {
			getInstance(apnsSettingVo.getP8FilePath(), apnsSettingVo.getKeyId(), apnsSettingVo.getTeamId(), apnsSettingVo.getBundleId());
			return instance.sendPushAsync(deviceToken, title, body, isProduction, null).get(30, TimeUnit.SECONDS);
		}

		public static ApnsPushResultVo sendPushNotification(ApnsSettingVo apnsSettingVo,
	    		String deviceToken, String title, String body, boolean isProduction, Map<String, String> data) throws IOException, InterruptedException, ExecutionException, TimeoutException {
			getInstance(apnsSettingVo.getP8FilePath(), apnsSettingVo.getKeyId(), apnsSettingVo.getTeamId(), apnsSettingVo.getBundleId());
			return instance.sendPushAsync(deviceToken, title, body, isProduction, data).get(30, TimeUnit.SECONDS);
		}
	}

	/**
	 * <pre>
	 * 푸시 알림 전송 (다수)
	 * </pre>
	 */
	public static class PushEch {
		private PushEch() {
			super();
		}

		public static List<ApnsPushResultVo> sendPushNotification(ApnsSettingVo apnsSettingVo,
                List<String> deviceTokens, String title, String body, boolean isProduction) throws IOException {
            ApnsSenderUtil util = getInstance(apnsSettingVo.getP8FilePath(), apnsSettingVo.getKeyId(), apnsSettingVo.getTeamId(), apnsSettingVo.getBundleId());

            List<ApnsPushResultVo> results = new ArrayList<>();

            // 각 푸시 요청은 비동기적으로 스케줄링됨
            List<CompletableFuture<ApnsPushResultVo>> futures = util.sendMultiPushAsync(deviceTokens, title, body, isProduction, null);

            // 모든 비동기 작업이 완료될 때까지 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 모든 결과 수집
            for (CompletableFuture<ApnsPushResultVo> future : futures) {
                try {
                    results.add(future.get()); // 결과를 동기적으로 얻습니다.
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Failed to get result for a push notification: {}", e.getMessage(), e);
                    results.add(new ApnsPushResultVo("UNKNOWN_TOKEN", false, -1, "Error retrieving result: " + e.getMessage()));
                    Thread.currentThread().interrupt();
                }
            }

            return results;
        }

        public static List<ApnsPushResultVo> sendPushNotification(ApnsSettingVo apnsSettingVo,
                List<String> deviceTokens, String title, String body, boolean isProduction, Map<String, String> data) throws IOException {
            ApnsSenderUtil util = getInstance(apnsSettingVo.getP8FilePath(), apnsSettingVo.getKeyId(), apnsSettingVo.getTeamId(), apnsSettingVo.getBundleId());

            List<ApnsPushResultVo> results = new ArrayList<>();

            List<CompletableFuture<ApnsPushResultVo>> futures = util.sendMultiPushAsync(deviceTokens, title, body, isProduction, data);

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<ApnsPushResultVo> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException | InterruptedException e) {
                    logger.error("Failed to get result for a push notification: {}", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
            return results;
        }
	}

}
