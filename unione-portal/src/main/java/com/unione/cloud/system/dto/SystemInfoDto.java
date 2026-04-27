package com.unione.cloud.system.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.core.util.JsonUtil;
import com.unione.cloud.system.model.SysSystem;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SystemInfoDto implements Serializable {
    /**
	* 主键
	*/
	@Schema(title="主键",description="长度为：10")
    private Long id;
    /**
	* 系统名称
	*/
	@Schema(title="系统名称",description="长度为：100")
	private String name;
	/**
	* 系统简称
	*/
	@Schema(title="系统简称",description="长度为：50")
	private String alias;
	/**
	* 系统ctx
	*/
	@Schema(title="系统ctx",description="长度为：20")
	private String ctx;
	/**
	* 系统logo大
	*/
	@Schema(title="系统logo大",description="长度为：100")
	private String logoLarge;
	/**
	* 系统logo小
	*/
	@Schema(title="系统logo小",description="长度为：100")
	private String logoSmall;
	/**
	* 系统主题
	*/
	@Schema(title="系统主题",description="长度为：20")
	private String themeName;
	/**
	* 版本号
	*/
	@Schema(title="版本号",description="长度为：30")
	private String versNo;
	/**
	* 版本说明
	*/
	@Schema(title="版本说明",description="长度为：500")
	private String versDesc;
	/**
	* 系统秘钥，令牌加密秘钥，用于实现不同系统之间token隔离
	*/
	@Schema(title="系统秘钥，令牌加密秘钥，用于实现不同系统之间token隔离",description="长度为：50")
	private String secret;
	/**
	* 底部信息
	*/
	@Schema(title="底部信息",description="长度为：200")
	private String footer;

	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 系统状态，字典SYSSTATUS 1开发，2内测，3发布，4撤销
	*/
	@Schema(title="系统状态，字典SYSSTATUS 1开发，2内测，3发布，4撤销",description="长度为：10")
	private Integer status;

    /**
     * 应用列表
     */
    @Schema(title = "应用列表", description = "应用列表")
    private List<SystemAppDto> apps=new ArrayList<>();

    /**
     * 系统配置
     */
    @Schema(title = "系统配置", description = "系统配置")
    private SystemConfigDto configs=new SystemConfigDto();


    public static SystemInfoDto from(SysSystem system){
		SystemInfoDto info=BeanUtils.copyProperties(system, SystemInfoDto.class,"configs");
		 if(!ObjectUtil.isEmpty(system.getAppList())){
            List<SystemAppDto> apps=JsonUtil.toList(new TypeReference<List<SystemAppDto>>() {}, system.getAppList());
            info.setApps(apps);
        }
		if(!ObjectUtil.isEmpty(system.getConfigs())){
            SystemConfigDto config=JsonUtil.toBean(SystemConfigDto.class, system.getConfigs());
			info.setConfigs(config);
        }
        return info;
    }

}
