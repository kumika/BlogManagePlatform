package frodez.config.aop.request.checker.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import frodez.config.aop.request.checker.facade.AutoChecker;
import frodez.config.cache.CacheProperties;
import frodez.constant.settings.DefTime;
import frodez.util.spring.ContextUtil;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 自动超时型重复请求检查GUAVACACHE实现
 * @author Frodez
 * @date 2019-01-21
 */
@Component("timeoutGuavaChecker")
@DependsOn("contextUtil")
public class AutoGuavaChecker implements AutoChecker {

	private Cache<String, Long> cache;

	@PostConstruct
	private void init() {
		cache = CacheBuilder.newBuilder().expireAfterAccess(ContextUtil.get(CacheProperties.class).getAutoGuavaChecker()
			.getTimeout(), DefTime.UNIT).build();
		Assert.notNull(cache, "cache must not be null");
	}

	@Override
	public boolean check(String key) {
		Long timestamp = cache.getIfPresent(key);
		long now = System.currentTimeMillis();
		if (timestamp == null) {
			return false;
		} else if (timestamp < now) {
			cache.invalidate(key);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void lock(String key, long timeout) {
		cache.put(key, timeout + System.currentTimeMillis());
	}

}
