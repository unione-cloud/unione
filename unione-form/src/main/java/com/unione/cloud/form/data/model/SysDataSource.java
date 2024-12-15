package com.unione.cloud.form.data.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.UniQueryIgnore;
import com.unione.cloud.beetsql.annotation.UniQueryIgnore.QueryType;
import com.unione.cloud.beetsql.annotation.UniQueryKeyWord;
import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysDataSource Entity
 * @描述	系统管理：数据源
 * @作者	Unione Cloud CodeGen
 * @日期	2024-09-02 22:16:23
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataSource")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_source")
public class SysDataSource extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5944695038555069391L;
	/**
	* 标题
	*/
	@UniQueryKeyWord
	@ApiModelProperty(value="标题",notes="长度为：100")
	private String title;
	/**
	* 名称，eg：数据库名称
	*/
	@UniQueryKeyWord
	@ApiModelProperty(value="名称，eg：数据库名称",notes="长度为：50")
	private String name;
	/**
	* 类别，sql：关系型存储，nosql：非关系型存储，api：接口存储
	*/
	@ApiModelProperty(value="类别，sql：关系型存储，nosql：非关系型存储，api：接口存储",notes="长度为：10")
	private String category;
	/**
	* 类型 字典 DMSDSTYPE ，1mysql，2oracle，3SqlServer，4SqlServer2005，5达梦，6postgreSql
            说明：1-20关系型数据源，21-40noSql数据源，41-50接口型数据源
	*/
	@ApiModelProperty(value="类型 字典 DMSDSTYPE ，1mysql，2oracle，3SqlServer，4SqlServer2005，5达梦，6postgreSql"+
            "说明：1-20关系型数据源，21-40noSql数据源，41-50接口型数据源",notes="长度为：10")
	private Integer dsType;
	/**
	* IP
	*/
	@ApiModelProperty(value="IP",notes="长度为：50")
	private String ip;
	/**
	* 端口
	*/
	@ApiModelProperty(value="端口",notes="长度为：10")
	private Integer port;
	/**
	* 链接URL
	*/
	@ApiModelProperty(value="链接URL",notes="长度为：250")
	private String url;
	/**
	* 认证类型，字典DMSDSAUTHTYPE  uname：帐号密码，token：令牌，md5:MD5，auth2:Auth2.0
	*/
	@ApiModelProperty(value="认证类型，字典DMSDSAUTHTYPE  uname：帐号密码，token：令牌，md5:MD5，auth2:Auth2.0",notes="长度为：20")
	private String authType;
	/**
	* 认证选项,json存储{}
	*/
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@ApiModelProperty(value="认证选项,json存储{}",notes="长度为：65535")
	private String authOpts;
	/**
	* 驱动名称
	*/
	@ApiModelProperty(value="驱动名称",notes="长度为：150")
	private String driverName;
	/**
	* 账号
	*/
	@ApiModelProperty(value="账号",notes="长度为：150")
	private String username;
	/**
	* 密码，加密存储
	*/
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@ApiModelProperty(value="密码，加密存储",notes="长度为：500")
	private String password;
	/**
	* 配置，json结构
	*/
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@ApiModelProperty(value="配置，json结构",notes="长度为：2147483647")
	private String configs;
	/**
	* 使用状态，字典USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="使用状态，字典USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer useSts;
	/**
	* 在线状态，字典MDPDCONLINE 1在线，2异常，3离线
	*/
	@ApiModelProperty(value="在线状态，字典MDPDCONLINE 1在线，2异常，3离线",notes="长度为：10")
	private Integer onlineSts;
	/**
	* 是否演练模式，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="是否演练模式，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer skitsFlag;
	/**
	* 说明
	*/
	@UniQueryKeyWord
	@ApiModelProperty(value="说明",notes="长度为：500")
	private String descs;

}
