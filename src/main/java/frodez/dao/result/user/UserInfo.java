package frodez.dao.result.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息
 * @author Frodez
 * @date 2018-11-14
 */
@Data
@NoArgsConstructor
@ApiModel(value = "用户信息返回数据")
public class UserInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	@ApiModelProperty(value = "用户ID")
	private Long id;

	/**
	 * 用户名
	 */
	@ApiModelProperty(value = "用户名")
	private String name;

	/**
	 * 密码
	 */
	@ApiModelProperty(value = "密码")
	private String password;

	/**
	 * 昵称
	 */
	@ApiModelProperty(value = "昵称")
	private String nickname;

	/**
	 * 邮箱地址
	 */
	@ApiModelProperty(value = "邮箱地址")
	private String email;

	/**
	 * 电话号码
	 */
	@ApiModelProperty(value = "电话号码")
	private String phone;

	/**
	 * 用户状态 0:禁用 1:正常
	 */
	@ApiModelProperty(value = "用户状态 0:禁用 1:正常")
	private Byte status;

	/**
	 * 角色ID
	 */
	@ApiModelProperty(value = "角色ID")
	private Long roleId;

	/**
	 * 角色名称
	 */
	@ApiModelProperty(value = "角色名称")
	private String roleName;

	/**
	 * 角色等级 0-9 0最高,9最低
	 */
	@ApiModelProperty(value = "角色等级 0-9 0最高,9最低")
	private Byte roleLevel;

	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述")
	private String roleDescription;

	/**
	 * 权限列表
	 */
	@ApiModelProperty(value = "权限列表")
	private List<PermissionInfo> permissionList;

}
