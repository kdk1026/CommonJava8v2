package common.util.xml;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

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
 * 2025. 5. 19. kdk	최초작성
 * </pre>
 *
 *
 * @author kdk
 */
public class JaxbXmlUtil {

	private JaxbXmlUtil() {
		super();
	}

	private static JaxbXmlUtil instance;
	private JAXBContext jaxbContext;

	private static final Logger logger = LoggerFactory.getLogger(JaxbXmlUtil.class);

	private static synchronized <T> JaxbXmlUtil getInstance(Class<T> clazz) throws JAXBException {
        if (instance == null) {
			instance = new JaxbXmlUtil();
			instance.jaxbContext = JAXBContext.newInstance(clazz);
        } else {
        	instance.jaxbContext = JAXBContext.newInstance(clazz);
        }

        return instance;
    }

	public static class ToXml {
		public static String converterObjToXmlStr(Object obj, boolean isPretty) {
			if ( obj == null ) {
				throw new IllegalArgumentException("obj is null");
			}

			try {
				getInstance(obj.getClass());
				Marshaller marshaller = instance.jaxbContext.createMarshaller();
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
		@SuppressWarnings("unchecked")
		public static <T> T converterXmlStreamToClass(InputStream is, Class<T> clazz) {
			if ( is == null ) {
				throw new IllegalArgumentException("is is null");
			}

			if ( clazz == null ) {
				throw new IllegalArgumentException("clazz is null");
			}

			try {
				getInstance(clazz);
				Unmarshaller unmarshaller = instance.jaxbContext.createUnmarshaller();
				return (T) unmarshaller.unmarshal(is);
			} catch (JAXBException e) {
				logger.error("", e);
			}

			return null;
		}
	}

	public static class ReadXmlFile {
		@SuppressWarnings("unchecked")
		public static <T> T convertXmlFileToObject(File file, Class<T> clazz) {
			if ( file == null ) {
				throw new IllegalArgumentException("file is null");
			}

			if ( clazz == null ) {
				throw new IllegalArgumentException("clazz is null");
			}

			try {
				getInstance(clazz);
				Unmarshaller unmarshaller = instance.jaxbContext.createUnmarshaller();
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
				getInstance(clazz);
				Unmarshaller unmarshaller = instance.jaxbContext.createUnmarshaller();
				return (T) unmarshaller.unmarshal(new File(fileName));
			} catch (JAXBException e) {
				logger.error("", e);
			}

			return null;
		}
	}

}
