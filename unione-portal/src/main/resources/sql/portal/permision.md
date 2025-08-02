
loadAppPermisForUser
===
```
SELECT app.* FROM SYS_APP_INFO APP WHERE STATUS in (2,3) AND TYPES = #{params.type}
-- @if(params.isAdmin==false){
 AND (
    EXISTS (select 1 from sys_user_permis where APP_ID = app.ID AND RES_TYPE='app' and USER_ID=#{params.user.id})
    -- @if(isNotEmpty(params.user.userRoles)){
    OR EXISTS (SELECT 1 FROM SYS_ROLE_PERMIS srp LEFT JOIN SYS_ROLE SR ON srp.ROLE_ID = sr.ID WHERE APP_ID = app.ID AND RES_TYPE='app' AND sr.SN in (#{join(params.user.userRoles)}))
    -- @}
    OR EXISTS (SELECT 1 FROM SYS_GROUP_PERMIS sgp LEFT JOIN SYS_GROUP_MEMBER sgm on sgm.GROUP_ID=sgp.ID  WHERE APP_ID = app.ID AND sgm.USER_ID = #{params.user.id})
)
-- @}
ORDER BY app.ORDERED
```

loadResorucePermisForUser
===
```
SELECT res.* FROM SYS_RESOURCE res WHERE res.STATUS = 1 AND APP_ID IN (#{join(params.appIds)})
-- @if(params.isAdmin==false){
 AND (
    EXISTS (SELECT 1 FROM SYS_USER_PERMIS WHERE RES_ID = res.ID AND USER_ID=#{params.user.id})
    -- @if(isNotEmpty(params.user.userRoles)){
    OR EXISTS (SELECT 1 FROM SYS_ROLE_PERMIS srp LEFT JOIN SYS_ROLE SR ON srp.ROLE_ID = sr.ID WHERE RES_ID = res.ID AND sr.SN IN (#{join(params.user.userRoles)}))
    -- @}
    OR EXISTS (SELECT 1 FROM SYS_GROUP_PERMIS sgp LEFT JOIN SYS_GROUP_MEMBER sgm ON sgm.GROUP_ID=sgp.ID  WHERE RES_ID = res.ID and sgm.USER_ID = #{params.user.id})
)
-- @}
ORDER BY res.ORDERED
```