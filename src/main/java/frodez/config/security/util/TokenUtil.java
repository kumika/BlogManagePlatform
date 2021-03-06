package frodez.config.security.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import frodez.config.security.settings.SecurityProperties;
import frodez.util.spring.ContextUtil;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * token工具类
 * @author Frodez
 * @date 2018-11-14
 */
@Component
@DependsOn("contextUtil")
public class TokenUtil {

	/**
	 * 算法
	 */
	private static Algorithm algorithm;

	/**
	 * 签发者
	 */
	private static String issuer;

	/**
	 * 是否过期
	 */
	private static boolean expired = false;

	/**
	 * 过期时长(毫秒)
	 */
	private static Long expiration;

	/**
	 * 声明
	 */
	private static String claim;

	/**
	 * HttpHeader名称
	 */
	private static String header;

	/**
	 * token前缀
	 */
	private static String tokenPrefix;

	/**
	 * token前缀长度
	 */
	private static int tokenPrefixLength;

	@PostConstruct
	private void init() {
		SecurityProperties properties = ContextUtil.get(SecurityProperties.class);
		algorithm = Algorithm.HMAC256(properties.getJwt().getSecret());
		issuer = properties.getJwt().getIssuer();
		expiration = properties.getJwt().getExpiration() * 1000;
		expired = expiration > 0;
		claim = properties.getJwt().getAuthorityClaim();
		header = properties.getJwt().getHeader();
		tokenPrefix = properties.getJwt().getTokenPrefix();
		tokenPrefixLength = tokenPrefix.length();
		Assert.notNull(algorithm, "algorithm must not be null");
		Assert.notNull(issuer, "issuer must not be null");
		Assert.notNull(expiration, "expiration must not be null");
		Assert.notNull(claim, "claim must not be null");
		Assert.notNull(header, "header must not be null");
		Assert.notNull(tokenPrefix, "tokenPrefix must not be null");
	}

	/**
	 * 生成token
	 * @author Frodez
	 * @param UserDetails 用户信息
	 * @date 2018-11-21
	 */
	public static String generate(UserDetails user) {
		long now = System.currentTimeMillis();
		if (expired) {
			return JWT.create().withIssuer(issuer).withIssuedAt(new Date(now)).withExpiresAt(new Date(now + expiration))
				.withSubject(user.getUsername()).withArrayClaim(claim, AuthorityUtil.get(user)).sign(algorithm);
		} else {
			return JWT.create().withIssuer(issuer).withIssuedAt(new Date(now)).withSubject(user.getUsername())
				.withArrayClaim(claim, AuthorityUtil.get(user)).sign(algorithm);
		}
	}

	/**
	 * 生成token
	 * @author Frodez
	 * @param UserDetails 用户信息
	 * @date 2018-11-21
	 */
	public static String generate(String username, List<String> authorities) {
		long now = System.currentTimeMillis();
		if (expired) {
			return JWT.create().withIssuer(issuer).withIssuedAt(new Date(now)).withExpiresAt(new Date(now + expiration))
				.withSubject(username).withArrayClaim(claim, authorities.toArray(String[]::new)).sign(algorithm);
		} else {
			return JWT.create().withIssuer(issuer).withIssuedAt(new Date(now)).withSubject(username).withArrayClaim(
				claim, authorities.toArray(String[]::new)).sign(algorithm);
		}
	}

	/**
	 * 验证token
	 * @author Frodez
	 * @param token
	 * @date 2018-11-21
	 */
	public static UserDetails verify(String token) {
		DecodedJWT jwt = null;
		if (expired) {
			//前面已经将exp置为合适的过期时间了,这里只需要判断其是否超过当前时间即可.
			jwt = JWT.require(algorithm).acceptExpiresAt(0).withIssuer(issuer).build().verify(token);
		} else {
			jwt = JWT.require(algorithm).withIssuer(issuer).build().verify(token);
		}
		return new User(jwt.getSubject(), "N/A", AuthorityUtil.make(jwt.getClaim(claim).asArray(String.class)));
	}

	/**
	 * 验证token,且不考虑过期
	 * @author Frodez
	 * @param token
	 * @date 2018-11-21
	 */
	public static UserDetails verifyWithNoExpired(String token) {
		DecodedJWT jwt = JWT.require(algorithm).withIssuer(issuer).build().verify(token);
		return new User(jwt.getSubject(), "N/A", AuthorityUtil.make(jwt.getClaim(claim).asArray(String.class)));
	}

	/**
	 * 获取request中的token,如果为空或者前缀不符合设置,均返回null.
	 * @see frodez.config.security.util.TokenUtil#getFullToken(HttpServletRequest)
	 * @author Frodez
	 * @date 2019-01-13
	 */
	public static String getRealToken(HttpServletRequest request) {
		String token = request.getHeader(header);
		if (token == null || !token.startsWith(tokenPrefix)) {
			return null;
		}
		return token.substring(tokenPrefixLength);
	}

	/**
	 * 获取request中的token,不做任何处理.
	 * @see frodez.config.security.util.TokenUtil#getRealToken(HttpServletRequest)
	 * @author Frodez
	 * @date 2019-01-13
	 */
	public static String getFullToken(HttpServletRequest request) {
		return request.getHeader(header);
	}

}
