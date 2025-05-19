package common.util.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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
public class JacksonXmlUtil {

	private JacksonXmlUtil() {
		super();
	}

	private static JacksonXmlUtil instance;
    private XmlMapper xmlMapper;

	private static final Logger logger = LoggerFactory.getLogger(JacksonXmlUtil.class);

	private static synchronized JacksonXmlUtil getInstance() {
        if (instance == null) {
			instance = new JacksonXmlUtil();
			instance.xmlMapper = new XmlMapper();
        }

        return instance;
    }

	public static class ToXml {
		public static String converterObjToXmlStr(Object obj, boolean isPretty) {
			if ( obj == null ) {
				throw new IllegalArgumentException("obj is null");
			}

			String xmlStr = "";

			try {
				getInstance();
				if (!isPretty) {
					xmlStr = instance.xmlMapper.writeValueAsString(obj);
				} else {
					xmlStr = instance.xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
				}
			} catch (Exception e) {
				logger.error("", e);
			}

			return xmlStr;
		}
	}

	public static class FromXml {
		public static <T> T converterXmlStrToClass(String xmlStr, Class<T> clazz) {
			if ( StringUtils.isBlank(xmlStr) ) {
				throw new IllegalArgumentException("xmlStr is null");
			}

			if ( clazz == null ) {
				throw new IllegalArgumentException("clazz is null");
			}

			try {
				getInstance();
				Object result = instance.xmlMapper.readValue(xmlStr, clazz);
				return clazz.cast(result);
			} catch (Exception e) {
				logger.error("", e);
			}

			return null;
		}

		public static <T> T converterXmlStreamToClass(InputStream is, Class<T> clazz) {
			if ( is == null ) {
				throw new IllegalArgumentException("is is null");
			}

			if ( clazz == null ) {
				throw new IllegalArgumentException("clazz is null");
			}

			try {
				getInstance();
				Object result = instance.xmlMapper.readValue(is, clazz);
				return clazz.cast(result);
			} catch (Exception e) {
				logger.error("", e);
			}

			return null;
		}
	}

	public static class ReadXmlFile {
		public static Object convertXmlFileToObject(File file, TypeReference<?> typeReference) {
			if ( file == null ) {
				throw new IllegalArgumentException("file is null");
			}

		    if (typeReference == null) {
		        throw new IllegalArgumentException("typeReference is null");
		    }

		    Object obj = null;

		    try {
		    	getInstance();
		        obj = instance.xmlMapper.readValue(file, typeReference);
		    } catch (IOException e) {
		        logger.error("", e);
		    }

		    return obj;
		}

		public static Object convertXmlFileToObject(String fileName, TypeReference<?> typeReference) {
			if ( StringUtils.isBlank(fileName) ) {
				throw new IllegalArgumentException("fileName is null");
			}

			if (typeReference == null) {
				throw new IllegalArgumentException("typeReference is null");
			}

			Object obj = null;

			try {
				getInstance();
				obj = instance.xmlMapper.readValue(new File(fileName), typeReference);
			} catch (IOException e) {
				logger.error("", e);
			}

			return obj;
		}
	}


}
