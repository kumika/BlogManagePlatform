package frodez.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json工具类
 * @author Frodez
 * @date 2018-11-27
 */
public class JSONUtil {

	/**
	 * 整个系统中所有的objectMapper均由此处提供,一是减少无用对象,<br>
	 * 二是保证系统中所有使用objectMapper的方法均保持一致的行为,<br>
	 * 而不必担心不同处objectMapper配置不一致导致行为不一致.
	 */
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
		SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true).configure(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
				JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true).configure(
					JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

	private static final TypeFactory TYPE_FACTORY = OBJECT_MAPPER.getTypeFactory();

	private JSONUtil() {
	}

	/**
	 * 获取jackson对象
	 * @author Frodez
	 * @date 2018-12-02
	 */
	public static ObjectMapper getInstance() {
		return OBJECT_MAPPER;
	}

	/**
	 * 将对象转换成json字符串
	 * @author Frodez
	 * @param object 对象
	 * @date 2018-12-02
	 */
	public static String toJSONString(Object object) {
		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json字符串转换成对象
	 * @author Frodez
	 * @param json json字符串
	 * @param klass 对象类型
	 * @date 2018-12-02
	 */
	public static <T> T toObject(String json, Class<T> klass) {
		try {
			return OBJECT_MAPPER.readValue(json, klass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json字符串转换成Map
	 * @author Frodez
	 * @param <K>
	 * @param <V>
	 * @param json json字符串
	 * @date 2018-12-02
	 */
	public static Map<String, Object> toMap(String json) {
		try {
			return OBJECT_MAPPER.readValue(json, TYPE_FACTORY.constructParametricType(HashMap.class, String.class,
				Object.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json字符串转换成Map
	 * @author Frodez
	 * @param <K>
	 * @param <V>
	 * @param json json字符串
	 * @date 2018-12-02
	 */
	public static <K, V> Map<K, V> toMap(String json, Class<K> k, Class<V> v) {
		try {
			return OBJECT_MAPPER.readValue(json, TYPE_FACTORY.constructParametricType(HashMap.class, k, v));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json字符串转换成List,发生异常返回null
	 * @author Frodez
	 * @param <T>
	 * @param json json字符串
	 * @date 2018-12-02
	 */
	public static <T> List<T> toList(String json, Class<T> klass) {
		try {
			return OBJECT_MAPPER.readValue(json, TYPE_FACTORY.constructParametricType(ArrayList.class, klass));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}