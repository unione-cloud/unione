loadSysAppList
===
```
SELECT app.* FROM SYS_APP_INFO APP WHERE app.CATEGORY = 'app' AND STATUS in (2,3) AND (IS_PLATFORM=1 OR TENANT_ID=#{params.user.tenantId})
-- @if(!isEqules("view",params.type) && params.isAdmin==false){
 AND (
    EXISTS (select 1 from SYS_USER_PERMIS where APP_ID = app.ID AND RES_TYPE='app' and USER_ID=#{params.user.id}) OR
    EXISTS (SELECT 1 FROM SYS_ROLE_PERMIS srp WHERE APP_ID=app.ID AND EN_DILIVERY=1 AND 
        srp.ROLE_ID IN (SELECT ROLE_ID FROM SYS_USER_ROLE sur WHERE sur.USER_ID=#{params.user.id})
    ) OR
    EXISTS (SELECT 1 FROM SYS_GROUP_PERMIS sgp LEFT JOIN SYS_GROUP_MEMBER sgm on sgm.GROUP_ID=sgp.ID  WHERE APP_ID = app.ID AND sgm.USER_ID = #{params.user.id})
)
-- @}
ORDER BY app.ORDERED
```

loadSysResourceTree
===
```sql
SELECT * FROM SYS_RESOURCE res
WHERE STATUS = 1 AND (IS_PLATFORM=1 OR TENANT_ID=#{params.user.tenantId}) AND APP_ID IN (#{join(params.appIds)})
-- @if(!isEqules("view",params.type) && params.isAdmin==false){
   AND (
       EXISTS (SELECT 1 FROM SYS_USER_PERMIS sup WHERE sup.USER_ID=#{params.user.id} AND sup.RES_ID=res.ID AND EN_DILIVERY=1) OR
       EXISTS (SELECT 1 FROM SYS_ROLE_PERMIS srp WHERE srp.RES_ID=res.ID AND EN_DILIVERY=1 AND 
           srp.ROLE_ID IN (SELECT ROLE_ID FROM SYS_USER_ROLE sur WHERE sur.USER_ID=#{params.user.id})
       ) OR
       EXISTS (SELECT 1 FROM SYS_GROUP_PERMIS sgp LEFT JOIN SYS_GROUP_MEMBER sgm on sgm.GROUP_ID=sgp.ID  WHERE sgp.RES_ID = res.ID AND sgm.USER_ID = #{params.user.id})
   )
-- @}
ORDER BY res.ORDERED ASC
```