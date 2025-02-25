
whereField
===
```
-- @if(isNotEmpty(params.userId)){
   AND U.ID = #{params.userId}
-- @}
-- @if(isNotEmpty(params.status)){
   AND U.STATUS = #{params.status}
-- @}
-- @if(isNotEmpty(params.roleId)){
   AND R.ID = #{params.roleId}
-- @}
-- @if(isNotEmpty(params.roleStatus)){
   AND R.STATUS = #{params.roleStatus}
-- @}
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
   AND (R.NAME LIKE #{'%'+keywords+'%'} OR R.DESCS LIKE #{'%'+keywords+'%'} OR U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'} OR O.NAME LIKE #{'%'+keywords+'%'})
-- @}
```


count
===
```sqlUserRoleDto
SELECT COUNT(UR.ID) 
FROM SYS_USER_ROLE UR 
LEFT JOIN SYS_USER U ON UR.USER_ID=U.ID 
LEFT JOIN SYS_ROLE R ON R.ID=UR.ROLE_ID 
LEFT JOIN SYS_ORGAN O ON U.ORG_ID=O.ID 
WHERE 1=1 #{use("whereField")}
```

findPages
===
```sql
SELECT #{page('UR.ID,UR.CREATED,UR.LAST_UPDATED,U.USERNAME,U.REAL_NAME,U.SEX,U.AVATAR,O.NAME AS ORG_NAME,U.AVATAR,U.SEX,U.STATUS,R.NAME AS ROLE_NAME,R.SN AS ROLE_SN,R.DESCS AS ROLE_DESCS,R.STATUS AS ROLE_STATUS')} 
FROM SYS_USER_ROLE UR 
LEFT JOIN SYS_USER U ON UR.USER_ID=U.ID 
LEFT JOIN SYS_ROLE R ON R.ID=UR.ROLE_ID 
LEFT JOIN SYS_ORGAN O ON U.ORG_ID=O.ID 
WHERE 1=1 #{use("whereField")}
```
