package common.util.xml;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * <pre>
 * -----------------------------------
 * 개정이력
 * -----------------------------------
 * 2025. 5. 19. kdk	 최초작성
 * 2025. 5. 27. 김대광 제미나이에 의한 코드 개선
 * </pre>
 *
 *
 * @author kdk
 */
public class JaxbXmlUtil {

	private static final Logger logger = LoggerFactory.getLogger(JaxbXmlUtil.class);

	private JaxbXmlUtil() {
		super();
	}

	private static final Map<Class<?>, JAXBContext> contextCache = new ConcurrentHashMap<>();

    /**
     * 지정된 클래스에 대한 JAXBContext를 가져오거나 생성하여 캐싱합니다.
     * @param clazz
     * @param <T>
     * @return
     */
	private static <T> JAXBContext getCachedJAXBContext(Class<T> clazz) {
        // 먼저 캐시에서 JAXBContext를 찾아봅니다.
        JAXBContext context = contextCache.get(clazz);

        // 캐시에 없으면 새로 생성하여 캐시에 넣습니다.
        if (context == null) {
            // 여러 스레드가 동시에 접근할 경우를 대비하여 동기화 블록 사용
            // 이중 체크 잠금(Double-Checked Locking) 패턴을 사용하여 성능 최적화
            synchronized (contextCache) { // contextCache 객체를 락으로 사용
                context = contextCache.get(clazz); // 다시 한번 캐시를 확인 (첫 번째 체크 후 다른 스레드가 이미 추가했을 수 있음)
                if (context == null) {
                    try {
                        context = JAXBContext.newInstance(clazz);
                        contextCache.put(clazz, context); // 생성된 JAXBContext를 캐시에 추가
                    } catch (JAXBException e) {
                        logger.error("Error creating JAXBContext for class: " + clazz.getName(), e);
                        return null;
                    }
                }
            }
        }
        return context;
    }

	public static class ToXml {
		private ToXml() {
			super();
		}

		public static String converterObjToXmlStr(Object obj, boolean isPretty) {
			if ( obj == null ) {
				throw new IllegalArgumentException("obj is null");
			}

			try {
				JAXBContext jaxbContext = getCachedJAXBContext(obj.getClass());
				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, isPretty);

				StringWriter writer = new StringWriter();
				marshaller.marshal(obj, writer);
				return writer.toString();
			} catch (JAXBException e) {
				logger.error("", e);
			}

			return null;
		}
	}

	public static class FromXml {
		private FromXml() {
			super();
		}

		@SuppressWarnings("unchecked")
		public static <T> T converterXmlStreamToClass(InputStream is, Class<T> clazz) {
			if ( is == null ) {
				throw new IllegalArgumentException("is is null");
			}

			if ( clazz == null ) {
				throw new IllegalArgumentException("clazz is null");
			}

			try {
				JAXBContext jaxbContext = getCachedJAXBContext(clazz);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				return (T) unmarshaller.unmarshal(is);
			} catch (JAXBException e) {
				logger.error("", e);
			}

			return null;
		}
	}

	public static class ReadXmlFile {
		private ReadXmlFile() {
			super();
		}

		@SuppressWarnings("unchecked")
		public static <T> T convertXmlFileToObject(File file, Class<T> clazz) {
			if ( file == null ) {
				throw new IllegalArgumentException("file is null");
			}

			if ( clazz == null ) {
				throw new IllegalArgumentException("clazz is null");
			}

			try {
				JAXBContext jaxbContext = getCachedJAXBContext(clazz);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				return (T) unmarshaller.unmarshal(file);
			} catch (JAXBException e) {
				logger.error("", e);
			}

			return null;
		}

		@SuppressWarnings("unchecked")
		public static <T> T convertXmlFileToObject(String fileName, Class<T> clazz) {
			if ( StringUtils.isBlank(fileName) ) {
				throw new IllegalArgumentException("fileName is null");
			}

			if ( clazz == null ) {
				throw new IllegalArgumentException("clazz is null");
			}

			try {
				JAXBContext jaxbContext = getCachedJAXBContext(clazz);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				return (T) unmarshaller.unmarshal(new File(fileName));
			} catch (JAXBException e) {
				logger.error("", e);
			}

			return null;
		}
	}

}
