package frodez.util.json;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import frodez.util.spring.context.ContextUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * json工具类
 * @author Frodez
 * @date 2018-11-27
 */
@Component
public class JSONUtil {

	/**
	 * 整个系统中所有的objectMapper均由此处提供,一是减少无用对象,<br>
	 * 二是保证系统中所有使用objectMapper的方法均保持一致的行为,<br>
	 * 而不必担心不同处objectMapper配置不一致导致行为不一致.
	 */
	private static ObjectMapper OBJECT_MAPPER = ContextUtil.get(ObjectMapper.class);

	@SuppressWarnings("rawtypes")
	private static Class<HashMap> DEFAULT_MAP_CLASS = HashMap.class;

	@SuppressWarnings("rawtypes")
	private static Class<ArrayList> DEFAULT_LIST_CLASS = ArrayList.class;

	@SuppressWarnings("rawtypes")
	private static Class<HashSet> DEFAULT_SET_CLASS = HashSet.class;

	private static String DEFAULT_MAP_CLASS_NAME = DEFAULT_MAP_CLASS.getName();

	private static String DEFAULT_LIST_CLASS_NAME = DEFAULT_LIST_CLASS.getName();

	private static String DEFAULT_SET_CLASS_NAME = DEFAULT_SET_CLASS.getName();

	private static ObjectReader DEFAULT_MAP_READER;

	private static Map<Class<?>, ObjectReader> singleTypeReaderCache = new ConcurrentHashMap<>();

	private static Map<String, ObjectReader> multiTypeReaderCache = new ConcurrentHashMap<>();

	private static Map<Class<?>, ObjectWriter> writerCache = new ConcurrentHashMap<>();

	/**
	 * 增加危险字符的转义处理.由于统一使用json返回,因此可以视为所有的返回值中的危险字符均已处理.<br>
	 * 另外,由于mybatis中采取预编译的方式注入参数(使用#{}标识符而非${}),sql注入的风险也基本解除.<br>
	 */
	@PostConstruct
	private void init() {
		DEFAULT_MAP_READER = OBJECT_MAPPER.readerFor(OBJECT_MAPPER.getTypeFactory().constructParametricType(
			DEFAULT_MAP_CLASS, String.class, Object.class));
		OBJECT_MAPPER.getFactory().setCharacterEscapes(new CharacterEscapes() {

			private static final long serialVersionUID = 1L;

			private int[] asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();

			// Note: "&apos;" is not defined in HTML 4.01.
			private Escaper escaper = Escapers.builder().addEscape('"', "&quot;").addEscape('\'', "&#39;").addEscape(
				'&', "&amp;").addEscape('<', "&lt;").addEscape('>', "&gt;").build();

			{
				asciiEscapes['<'] = CharacterEscapes.ESCAPE_CUSTOM;
				asciiEscapes['>'] = CharacterEscapes.ESCAPE_CUSTOM;
				asciiEscapes['&'] = CharacterEscapes.ESCAPE_CUSTOM;
				asciiEscapes['"'] = CharacterEscapes.ESCAPE_CUSTOM;
				asciiEscapes['\''] = CharacterEscapes.ESCAPE_CUSTOM;
			}

			@Override
			public SerializableString getEscapeSequence(int ch) {
				return new SerializedString(escaper.escape(Character.toString(ch)));
			}

			@Override
			public int[] getEscapeCodesForAscii() {
				return asciiEscapes;
			}
		});
	}

	/**
	 * 获取jackson对象
	 * @author Frodez
	 * @date 2018-12-02
	 */
	public static ObjectMapper mapper() {
		return OBJECT_MAPPER;
	}

	/**
	 * 将对象转换成json字符串
	 * @author Frodez
	 * @param object 对象
	 * @date 2018-12-02
	 */
	public static String string(Object object) {
		Objects.requireNonNull(object);
		try {
			return writerCache.computeIfAbsent(object.getClass(), (o) -> OBJECT_MAPPER.writerWithView(object
				.getClass())).writeValueAsString(object);
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
	public static <T> T as(String json, Class<T> klass) {
		try {
			return singleTypeReaderCache.computeIfAbsent(klass, (k) -> OBJECT_MAPPER.readerFor(OBJECT_MAPPER
				.getTypeFactory().constructType(klass))).readValue(json);
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
	public static Map<String, Object> map(String json) {
		try {
			return DEFAULT_MAP_READER.readValue(json);
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
	public static <K, V> Map<K, V> map(String json, Class<K> k, Class<V> v) {
		Objects.requireNonNull(k);
		Objects.requireNonNull(v);
		try {
			return multiTypeReaderCache.computeIfAbsent(DEFAULT_MAP_CLASS_NAME.concat(k.getName()).concat(v.getName()),
				(i) -> OBJECT_MAPPER.readerFor(OBJECT_MAPPER.getTypeFactory().constructParametricType(DEFAULT_MAP_CLASS,
					k, v))).readValue(json);
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
	public static <T> List<T> list(String json, Class<T> klass) {
		Objects.requireNonNull(klass);
		try {
			return multiTypeReaderCache.computeIfAbsent(DEFAULT_LIST_CLASS_NAME.concat(klass.getName()), (
				i) -> OBJECT_MAPPER.readerFor(OBJECT_MAPPER.getTypeFactory().constructParametricType(DEFAULT_LIST_CLASS,
					klass))).readValue(json);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将json字符串转换成Set,发生异常返回null
	 * @author Frodez
	 * @param <T>
	 * @param json json字符串
	 * @date 2018-12-02
	 */
	public static <T> Set<T> set(String json, Class<T> klass) {
		Objects.requireNonNull(klass);
		try {
			return multiTypeReaderCache.computeIfAbsent(DEFAULT_SET_CLASS_NAME.concat(klass.getName()), (
				i) -> OBJECT_MAPPER.readerFor(OBJECT_MAPPER.getTypeFactory().constructParametricType(DEFAULT_SET_CLASS,
					klass))).readValue(json);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
