package common.util.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * <pre>
 * -----------------------------------
 * 개정이력
 * -----------------------------------
 * 2025. 5. 19. kdk	 최초작성
 * 2025. 5. 27  김대광 제미나이에 의한 코드 개선
 * </pre>
 *
 *
 * @author kdk
 */
public class JacksonXmlUtil {

	private static final Logger logger = LoggerFactory.getLogger(JacksonXmlUtil.class);

	private JacksonXmlUtil() {
		super();
	}

	private static JacksonXmlUtil instance;
    private static final XmlMapper XML_MAPPER = new XmlMapper();

	private static synchronized JacksonXmlUtil getInstance() {
        if (instance == null) {
			instance = new JacksonXmlUtil();
        }

        return instance;
    }

	public static class ToXml {
		private ToXml() {
			super();
		}

		public static String converterObjToXmlStr(Object obj, boolean isPretty) {
			Objects.requireNonNull(obj, "obj is null");

			String xmlStr = "";

			try {
				getInstance();
				if (!isPretty) {
					xmlStr = XML_MAPPER.writeValueAsString(obj);
				} else {
					xmlStr = XML_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
				}
			} catch (JsonProcessingException e) {
				logger.error("", e);
			}

			return xmlStr;
		}
	}

	public static class FromXml {
		private FromXml() {
			super();
		}

		public static <T> T converterXmlStrToClass(String xmlStr, Class<T> clazz) {
			Objects.requireNonNull(xmlStr.trim(), "xmlStr is null");
			Objects.requireNonNull(clazz, "clazz is null");

			try {
				getInstance();
				Object result = XML_MAPPER.readValue(xmlStr, clazz);
				return clazz.cast(result);
			} catch (JsonProcessingException e) {
				logger.error("", e);
			}

			return null;
		}

		public static <T> T converterXmlStreamToClass(InputStream is, Class<T> clazz) {
			Objects.requireNonNull(is, "is is null");
			Objects.requireNonNull(clazz, "clazz is null");

			try {
				getInstance();
				Object result = XML_MAPPER.readValue(is, clazz);
				return clazz.cast(result);
			} catch (IOException e) {
				logger.error("", e);
			}

			return null;
		}
	}

	public static class ReadXmlFile {
		private ReadXmlFile() {
			super();
		}

		public static Object convertXmlFileToObject(File file, TypeReference<?> typeReference) {
			Objects.requireNonNull(file, "file is null");
			Objects.requireNonNull(typeReference, "typeReference is null");

		    Object obj = null;

		    try {
		    	getInstance();
		        obj = XML_MAPPER.readValue(file, typeReference);
		    } catch (IOException e) {
		        logger.error("", e);
		    }

		    return obj;
		}

		public static Object convertXmlFileToObject(String fileName, TypeReference<?> typeReference) {
			Objects.requireNonNull(fileName.trim(), "fileName is null");
			Objects.requireNonNull(typeReference, "typeReference is null");

			Object obj = null;

			try {
				getInstance();
				obj = XML_MAPPER.readValue(new File(fileName), typeReference);
			} catch (IOException e) {
				logger.error("", e);
			}

			return obj;
		}
	}

}
