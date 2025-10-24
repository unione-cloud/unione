package com.unione.cloud.system.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.system.model.SysConfigDefine;
import com.unione.cloud.system.model.SysConfigValue;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统配置服务
 */
@Slf4j
@Service
public class SysConfigService {

    @Autowired
    private DataBaseDao dataBaseDao;

    @Autowired
    private SessionService sessionservice;


    /**
     * 加载配置列表
     * @param name
     * @param type
     * @return
     */
    public List<SysConfigDefine> loadList(String name,Integer type){
        log.info("进入：加载配置列表方法,参数name:{},type:{}",name,type);

        SysConfigDefine configDefine=new SysConfigDefine();
        if(!Objects.equals("root", name)){
			configDefine.setSn(name+"%");
		}
		if(!Objects.equals(-1, type)){
			configDefine.setTypes(type);
		}

        List<SysConfigDefine> list = dataBaseDao.findList(SqlBuilder.build(configDefine)
			.where("delFlag=0 and sn like ? and types=?")
			.sort(Sort.build("sn")));

        // 根据类型分组
        Map<Integer,List<Long>> tmap=new HashMap<>();
        list.stream().filter(r->!ObjectUtil.equal(r.getTypes(), 0)).forEach(r->{
            List<Long> ids=tmap.get(r.getTypes());
            if(Objects.isNull(ids)){
            	ids=new ArrayList<>();
            	tmap.put(r.getTypes(), ids);	
            }
            ids.add(r.getId());	
        });

        // 根据类型加载value
        Map<Long,SysConfigValue> vmap=new HashMap<>();
        tmap.forEach((k,v)->{
            //1租户级，2机构级，3用户级
            if(ObjectUtil.equal(k, 1) &&!ObjectUtil.isEmpty(v)){
            	List<SysConfigValue> values = dataBaseDao.findList(SqlBuilder.build(SysConfigValue.class,v)
                    .where("delFlag=0 and status=1 and configId in [ids] and types=? and tenantId=?")
                    .where("types", k)
                    .where("tenantId", sessionservice.getTenantId()));
            	values.forEach(r->{
            		vmap.put(r.getConfigId(), r);
            	});
            }
            if(ObjectUtil.equal(k, 2) &&!ObjectUtil.isEmpty(v)){
            	List<SysConfigValue> values = dataBaseDao.findList(SqlBuilder.build(SysConfigValue.class,v)
                   .where("delFlag=0 and status=1 and configId in [ids] and types=? and orgId=?")
                   .where("types", k)
                   .where("orgId", sessionservice.getOrgId()));
            	values.forEach(r->{
            		vmap.put(r.getConfigId(), r);
            	});	
            }
            if(ObjectUtil.equal(k, 3) &&!ObjectUtil.isEmpty(v)){
            	List<SysConfigValue> values = dataBaseDao.findList(SqlBuilder.build(SysConfigValue.class,v)
                  .where("delFlag=0 and status=1 and configId in [ids] and types=? and userId=?")
                  .where("types", k)
                  .where("userId", sessionservice.getUserId()));
            	values.forEach(r->{
            		vmap.put(r.getConfigId(), r);
            	});
            }
        });

        // 组装
        list.stream().forEach(r->{
            SysConfigValue value=vmap.get(r.getId());
            if(Objects.nonNull(value)){
            	r.setValueUsed(value.getValueUsed());
            }
        });

        log.info("退出：加载配置列表方法,name:{},type:{},size:{}",name,type,list.size());
        return list;
    }




    /**
     * 设置配置value
     * @param sn
     * @param value
     */
    public void setValue(String sn,String value){
        SysConfigDefine configDefine=new SysConfigDefine();
        configDefine.setSn(sn);
        configDefine.setStatus(1);
        configDefine.setDelFlag(0);
        configDefine = dataBaseDao.findOne(SqlBuilder.build(configDefine));
        AssertUtil.service().notNull(configDefine, "配置["+sn+"]不存在");

        //全局
        if(ObjectUtil.equal(configDefine.getTypes(), 0)){ 
            AssertUtil.service().isTrue(sessionservice.getUserRoles().contains(UserRoles.SYS3PCONFIG)||
                sessionservice.getUserRoles().contains(UserRoles.FORMDEV) ||
                sessionservice.getUserRoles().contains(UserRoles.ONLINEDEV) ||
                sessionservice.getUserRoles().contains(UserRoles.SYSOPSUSER)||
                sessionservice.getUserRoles().contains(UserRoles.SUPPERADMIN), "用户权限不足");
            configDefine.setValueUsed(value);
            int len = dataBaseDao.updateById(configDefine);    
            AssertUtil.service().isTrue(len>0, "配置["+sn+"]设置失败");
        }

        //1租户级，2机构级，3用户级
        if(ObjectUtil.equal(configDefine.getTypes(), 1)){
            AssertUtil.service().isTrue(sessionservice.getUserRoles().contains(UserRoles.SYS3PCONFIG)||
                sessionservice.getUserRoles().contains(UserRoles.FORMDEV) ||
                sessionservice.getUserRoles().contains(UserRoles.ONLINEDEV) ||
                sessionservice.getUserRoles().contains(UserRoles.SYSOPSUSER) ||
                sessionservice.getUserRoles().contains(UserRoles.TENANTADMIN), "用户权限不足");
            SysConfigValue configValue=new SysConfigValue();
            configValue.setConfigId(configDefine.getId());
            configValue.setTenantId(sessionservice.getTenantId());
            configValue.setTypes(configDefine.getTypes());    
            SysConfigValue tempValue = dataBaseDao.findOne(SqlBuilder.build(configValue));
            if(Objects.isNull(tempValue)){
            	configValue.setSn(configDefine.getSn());
            	configValue.setValueUsed(value);
            	configValue.setStatus(1);
            	configValue.setDelFlag(0);
                int len = dataBaseDao.insert(configValue);
                AssertUtil.service().isTrue(len>0, "配置["+sn+"]设置失败");
            }else{
                tempValue.setValueUsed(value);
                int len = dataBaseDao.updateById(tempValue);
                AssertUtil.service().isTrue(len>0, "配置["+sn+"]设置失败");
            }
        }
        if(ObjectUtil.equal(configDefine.getTypes(), 2)){
            AssertUtil.service().isTrue(sessionservice.getUserRoles().contains(UserRoles.SYS3PCONFIG)||
                sessionservice.getUserRoles().contains(UserRoles.FORMDEV) ||
                sessionservice.getUserRoles().contains(UserRoles.ONLINEDEV) ||
                sessionservice.getUserRoles().contains(UserRoles.SYSOPSUSER) ||
                sessionservice.getUserRoles().contains(UserRoles.ORGANADMIN) ,"用户权限不足");
            SysConfigValue configValue=new SysConfigValue();
            configValue.setConfigId(configDefine.getId());
            configValue.setOrgId(sessionservice.getOrgId());
            configValue.setTypes(configDefine.getTypes());    
            SysConfigValue tempValue = dataBaseDao.findOne(SqlBuilder.build(configValue));
            if(Objects.isNull(tempValue)){
            	configValue.setSn(configDefine.getSn());
            	configValue.setValueUsed(value);
            	configValue.setStatus(1);
            	configValue.setDelFlag(0);
                configValue.setTenantId(sessionservice.getTenantId());
                int len = dataBaseDao.insert(configValue);
                AssertUtil.service().isTrue(len>0, "配置["+sn+"]设置失败");
            }else{
                tempValue.setValueUsed(value);
                int len = dataBaseDao.updateById(tempValue);
                AssertUtil.service().isTrue(len>0, "配置["+sn+"]设置失败");
            }    
        }
        if(ObjectUtil.equal(configDefine.getTypes(), 3)){
        	SysConfigValue configValue=new SysConfigValue();
            configValue.setConfigId(configDefine.getId());
            configValue.setUserId(sessionservice.getUserId());
            configValue.setTypes(configDefine.getTypes());    
            SysConfigValue tempValue = dataBaseDao.findOne(SqlBuilder.build(configValue));
            if(Objects.isNull(tempValue)){
            	configValue.setSn(configDefine.getSn());
            	configValue.setValueUsed(value);
            	configValue.setStatus(1);
            	configValue.setDelFlag(0);
                configValue.setTenantId(sessionservice.getTenantId());
                configValue.setOrgId(sessionservice.getOrgId());
                int len = dataBaseDao.insert(configValue);
                AssertUtil.service().isTrue(len>0, "配置["+sn+"]设置失败");
            }else{
                tempValue.setValueUsed(value);
                int len = dataBaseDao.updateById(tempValue);
                AssertUtil.service().isTrue(len>0, "配置["+sn+"]设置失败");
            }    
        }
    }

}
