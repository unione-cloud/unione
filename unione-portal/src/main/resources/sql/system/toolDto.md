
loadUserToolsByPermis
===
```
SELECT t.* FROM SYS_TOOL t
left join SYS_RESOURCE res on t.RES_ID = res.ID
WHERE (res.STATUS = 1 OR t.RES_ID IS NULL) AND t.DEL_FLAG = 0 AND t.STATUS = 1 AND t.SYS_ID = #{params.sysId}
-- @if(isNotEmpty(params.gname)){
AND t.GNAME = #{params.gname}
-- @}   
-- @if(isNotEmpty(params.types)){
AND t.TYPES = #{params.types}
-- @}   
-- @if(params.isAdmin==false){
 AND (
    res.CREATED_BY=#{pricipal.id}
    OR
    EXISTS (SELECT 1 FROM SYS_USER_PERMIS WHERE RES_ID = res.ID AND USER_ID=#{pricipal.id})
    -- @if(isNotEmpty(pricipal.userRoles)){
    OR EXISTS (SELECT 1 FROM SYS_ROLE_PERMIS srp LEFT JOIN SYS_ROLE SR ON srp.ROLE_ID = sr.ID WHERE RES_ID = res.ID AND sr.SN IN (#{join(pricipal.userRoles)}))
    -- @}
    OR EXISTS (SELECT 1 FROM SYS_GROUP_PERMIS sgp LEFT JOIN SYS_GROUP_MEMBER sgm ON sgm.GROUP_ID=sgp.ID  WHERE RES_ID = res.ID and sgm.USER_ID = #{pricipal.id})
)
-- @}
ORDER BY t.ORDERED
```