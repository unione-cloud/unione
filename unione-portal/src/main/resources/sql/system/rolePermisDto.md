
whereField
===
```
-- @if(isNotEmpty(params.orgId)){
   AND O.ORG_ID = #{params.orgId}
-- @}
-- @if(isNotEmpty(params.status)){
   AND R.STATUS = #{params.status}
-- @}
-- @if(isNotEmpty(params.roleId)){
   AND RP.ROLE_ID = #{params.roleId}
-- @}
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
   AND (R.NAME LIKE #{'%'+keywords+'%'})
-- @}
```


count
===
```sql
SELECT COUNT(RP.ID) 
FROM SYS_ROLE_PERMIS RP 
LEFT JOIN SYS_ROLE R ON RP.ROLE_ID=R.ID
LEFT JOIN SYS_ORGAN O ON R.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```

findPages
===
```sql
SELECT #{page('RP.*,R.NAME,R.SN,R.TYPES,R.STATUS,O.NAME AS ORG_NAME')} 
FROM SYS_ROLE_PERMIS RP 
LEFT JOIN SYS_ROLE R ON RP.ROLE_ID=R.ID
LEFT JOIN SYS_ORGAN O ON R.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```
