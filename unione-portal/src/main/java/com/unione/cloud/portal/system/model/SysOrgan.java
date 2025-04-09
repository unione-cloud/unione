package com.unione.cloud.portal.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.beetsql.annotation.QueryAction;
import com.unione.cloud.beetsql.builder.SqlAction;
import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysOrgan Entity
 * @描述	系统管理：机构信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
 * @版本	1.0.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_organ")
@SqlResource("system.SysOrgan")
// @UniDataPermis(DataPermis.ORGANCODE)
public class SysOrgan extends Pojo {
	/**
	* 上级ID
	*/
	@JsonProperty("pid")
	@Schema(title="上级ID",description="长度为：19")
	private Long parentId;
	/**
	* 企业/机构名称
	*/
	@KeyWords
	@Schema(title="企业/机构名称",description="长度为：250")
	private String name;
	/**
	* 企业/机构别名
	*/
	@KeyWords
	@Schema(title="企业/机构别名",description="长度为：250")
	private String alias;
	/**
	* 类型：字典ORGTYPES 1企业，2机构，3部门
	*/
	@Schema(title="类型：字典ORGTYPES 1企业，2机构，3部门",description="长度为：10")
	private Integer types;
	/**
	* 区域label，eg：广州市->天河区
	*/
	@Schema(title="区域label，eg：广州市->天河区",description="长度为：200")
	private String areaLabel;
	/**
	* 机构编码，不能为空，唯一
	*/
	@KeyWords
	@Schema(title="机构编码",description="机构编码，不能为空，唯一，长度为：100")
	private String sn;
	/**
	* 层级编码：自动生成
	*/
	@QueryAction(SqlAction.LIKER)
	@Schema(title="层级编码",description="层级编码：自动生成，长度为：100")
	private String lvSn;
	/**
	* 所在层级
	*/
	@Schema(title="所在层级",description="长度为：10")
	private Integer lvNo;
	/**
	* 主营业务
	*/
	@Schema(title="主营业务",description="长度为：500")
	private String busiMain;
	/**
	* 经营范围
	*/
	@Schema(title="经营范围",description="长度为：500")
	private String busiScop;
	/**
	* 企业/机构地址
	*/
	@Schema(title="企业/机构地址",description="长度为：250")
	private String addr;
	/**
	* 联系电话
	*/
	@KeyWords
	@Schema(title="联系电话",description="长度为：50")
	private String tel;
	/**
	* 级别
	*/
	@Schema(title="级别",description="长度为：10")
	private Integer levels;
	/**
	* 是否叶子节点 1：是叶子节点 0：非叶子节点
	*/
	@Schema(title="是否叶子节点 1：是叶子节点 0：非叶子节点",description="长度为：10")
	private Integer isLeaf;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="状态",description="长度为：10")
	private Integer status;
	/**
	* 说明
	*/
	@KeyWords
	@Schema(title="说明",description="长度为：500")
	private String descs;

}
