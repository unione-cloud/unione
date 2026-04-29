package com.unione.cloud.security.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.core.annotation.Action;
import com.unione.cloud.core.annotation.ActionType;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.redis.RedisService;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.token.TokenService;
import com.unione.cloud.core.token.TokenService.TcmEntry;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.security.dto.PrincipalSession;
import com.unione.cloud.system.model.SysOrgan;
import com.unione.cloud.system.model.SysTenant;
import com.unione.cloud.system.model.SysUser;
import com.unione.cloud.system.service.OrganService;
import com.unione.cloud.system.service.TenantService;
import com.unione.cloud.system.service.UserService;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/session")
@Tag(name="安全服务：会话管理")
public class SessionController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private OrganService organService;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TokenService tokenService;

    /**
	 * Token Center Manage 令牌中心化管理，redis 数据库，默认：10
	 */
	@Value("${security.tcm.db:10}")
	private int tcmDb;
	/**
	 * Token Center Manage 令牌中心化管理，key前缀
	 */
	@Value("${security.tcm.key:TOKEN}")
	private String tcmKey;

    @PostMapping("/list")
    @Operation(summary = "查询会话列表")
    @Action(title="查询会话列表",type=ActionType.Query,roles={UserRoles.TENANTADMIN})
    public Results<List<PrincipalSession>> list(@RequestBody Params<PrincipalSession> params) {

        String pattern = String.format("%s:%s:*", tcmKey, sessionService.getTenantId());
        if(sessionService.isAdmin() || sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
            pattern = tcmKey + ":*";
        }

        // 获取会话列表
        List<PrincipalSession> sessionList = new ArrayList<>();
        Cursor<String> cursor = redisService.template(tcmDb).scan(ScanOptions.scanOptions().match(pattern).build());
        while (cursor.hasNext()) {
            String key = cursor.next();
            TcmEntry tcm = redisService.getObj(tcmDb, key);
            if(!ObjectUtil.isEmpty(tcm)){
                PrincipalSession session=BeanUtils.copyProperties(tcm, PrincipalSession.class, "times");
                session.setTimes(DateUtil.date(tcm.getTimes()));
                sessionList.add(session);
            }
        }
        if(sessionList.isEmpty() || sessionList.size() < (params.getPage()-1) * params.getPageSize()){
            return Results.success();
        }

        // 排序并分页，获取会话列表
        sessionList.sort((a1, a2) -> a2.getTimes().compareTo(a1.getTimes()));
        sessionList=ListUtil.page(params.getPage()-1, params.getPageSize(), sessionList);

        // 获取租户，机构和用户信息
        Set<Long> tids=new HashSet<>();
        Set<Long> oids=new HashSet<>();
        Set<Long> uids=new HashSet<>();
        sessionList.forEach(item -> {
            tids.add(item.getTenantId());
            oids.add(item.getOrgId());
            uids.add(item.getUserId());
        });
        Map<Long,SysTenant> tmap = tenantService.loadTenant(tids);
        Map<Long,SysOrgan> omap = organService.loadOrgan(oids);
        Map<Long,SysUser> umap = userService.loadUser(uids);

        // 渲染会话列表
        sessionList.forEach(item -> {
            SysTenant tenant=tmap.get(item.getTenantId());
            if(tenant!=null){
                item.setTenantName(tenant.getName());
            }
            SysOrgan organ=omap.get(item.getOrgId());
            if(organ!=null){
                item.setOrgName(organ.getName());
            }
            SysUser user=umap.get(item.getUserId());
            if(user!=null){
                item.setUserName(user.getUsername());
                item.setRealName(user.getRealName());
            }
        });

        return Results.success(sessionList);
    }


    @PostMapping("/kick")
    @Operation(summary = "踢出会话")
    @Action(title="踢出会话",type=ActionType.Delete,roles={UserRoles.TENANTADMIN})
    public Results<Void> kick(@RequestBody PrincipalSession session){
        AssertUtil.service().notNull(session,new String[]{"id","tenantId","userName"}, "属性%s不能为空");
        if(!sessionService.isAdmin() && !sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN)){
            AssertUtil.service().notEq(session.getTenantId(), sessionService.getTenantId(), "无该会话操作权限");
        }
        tokenService.clean4auth(String.format("%s@%s@%s", session.getTenantId(),session.getUserName(),session.getId()));
        return Results.success();
    }


}
