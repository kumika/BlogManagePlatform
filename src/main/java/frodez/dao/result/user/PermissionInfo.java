package frodez.dao.result.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限信息
 * @author Frodez
 * @date 2018-11-14
 */
@Data
@NoArgsConstructor
@ApiModel(value = "权限信息返回数据")
public class PermissionInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 权限ID
	 */
	@ApiModelProperty(value = "权限ID")
	private Long id;

	/**
	 * 类型 1:GET 2:POST 3:DELETE 4:PUT
	 */
	@ApiModelProperty(value = "类型 1:GET 2:POST 3:DELETE 4:PUT")
	private Byte type;

	/**
	 * 权限名称
	 */
	@ApiModelProperty(value = "权限名称")
	private String name;

	/**
	 * 地址
	 */
	@ApiModelProperty(value = "地址")
	private String url;

	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述")
	private String description;

}
