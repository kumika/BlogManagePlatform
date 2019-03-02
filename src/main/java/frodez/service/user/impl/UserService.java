package frodez.service.user.impl;

import frodez.config.error.exception.ServiceException;
import frodez.config.error.status.ErrorCode;
import frodez.config.security.util.TokenManager;
import frodez.constant.user.UserStatusEnum;
import frodez.dao.mapper.user.UserMapper;
import frodez.dao.model.user.User;
import frodez.dao.param.user.LoginDTO;
import frodez.dao.param.user.ReLoginDTO;
import frodez.dao.param.user.RegisterDTO;
import frodez.dao.result.user.PermissionInfo;
import frodez.dao.result.user.UserInfo;
import frodez.service.cache.vm.facade.TokenCache;
import frodez.service.user.facade.IAuthorityService;
import frodez.service.user.facade.IUserService;
import frodez.util.result.Result;
import frodez.util.spring.context.ContextUtil;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户信息服务
 * @author Frodez
 * @date 2018-11-14
 */
@Slf4j
@Service
public class UserService implements IUserService {

	/**
	 * spring security验证管理器
	 */
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private SecurityContextLogoutHandler logoutHandler;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TokenCache tokenCache;

	@Autowired
	private IAuthorityService authorityService;

	@Autowired
	private UserMapper userMapper;

	@Override
	public Result login(LoginDTO param) {
		try {
			Result result = authorityService.getUserInfo(param.getUsername());
			if (result.unable()) {
				return result;
			}
			UserInfo userInfo = result.as(UserInfo.class);
			if (!passwordEncoder.matches(param.getPassword(), userInfo.getPassword())) {
				return Result.fail("用户名或密码错误!");
			}
			if (tokenCache.existValue(userInfo)) {
				return Result.fail("用户已登录!");
			}
			List<String> authorities = userInfo.getPermissionList().stream().map(PermissionInfo::getName).collect(
				Collectors.toList());
			//realToken
			String token = TokenManager.generate(param.getUsername(), authorities);
			tokenCache.save(token, userInfo);
			SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(param.getUsername(), param.getPassword())));
			return Result.success(token);
		} catch (Exception e) {
			log.error("[login]", e);
			return Result.errorService();
		}
	}

	@Override
	public Result refresh(ReLoginDTO param) {
		try {
			Result result = authorityService.getUserInfo(param.getUsername());
			if (result.unable()) {
				return result;
			}
			UserInfo userInfo = result.as(UserInfo.class);
			UserDetails userDetails = TokenManager.verify(param.getOldToken(), false);
			//这里的userDetails.password已经加密了
			if (!userDetails.getPassword().equals(userInfo.getPassword())) {
				return Result.fail("用户名或密码错误!");
			}
			List<String> authorities = userInfo.getPermissionList().stream().map(PermissionInfo::getName).collect(
				Collectors.toList());
			//realToken
			String token = TokenManager.generate(param.getUsername(), authorities);
			tokenCache.remove(param.getOldToken());
			tokenCache.save(token, userInfo);
			logoutHandler.logout(ContextUtil.getRequest(), ContextUtil.getResponse(), SecurityContextHolder.getContext()
				.getAuthentication());
			SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(param.getUsername(), userInfo.getPassword())));
			return Result.success(token);
		} catch (Exception e) {
			log.error("[reLogin]", e);
			return Result.errorService();
		}
	}

	@Override
	public Result logout() {
		try {
			HttpServletRequest request = ContextUtil.getRequest();
			String token = TokenManager.getRealToken(request);
			if (!tokenCache.existKey(token)) {
				return Result.fail("用户已下线!");
			}
			tokenCache.remove(token);
			logoutHandler.logout(request, ContextUtil.getResponse(), SecurityContextHolder.getContext()
				.getAuthentication());
			return Result.success();
		} catch (Exception e) {
			log.error("[logout]", e);
			return Result.errorService();
		}
	}

	@Override
	@Transactional
	public Result register(RegisterDTO param) {
		try {
			User user = new User();
			user.setCreateTime(new Date());
			user.setName(param.getName());
			user.setPassword(passwordEncoder.encode(param.getPassword()));
			user.setNickname(param.getNickname());
			user.setEmail(param.getEmail());
			user.setPhone(param.getPhone());
			user.setStatus(UserStatusEnum.NORMAL.getVal());
			//暂时写死
			user.setRoleId(1L);
			userMapper.insert(user);
			return Result.success();
		} catch (Exception e) {
			log.error("[register]", e);
			throw new ServiceException(ErrorCode.USER_SERVICE_ERROR);
		}
	}

}