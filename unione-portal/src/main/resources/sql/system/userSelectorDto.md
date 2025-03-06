
whereOrganUser
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
   AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @}
```


countOrganUser
===
```sql
SELECT COUNT(U.ID) 
FROM SYS_USER U
LEFT JOIN SYS_USER_ORGAN UO ON UO.USER_ID=U.ID 
WHERE 1=1 #{use("whereOrganUser")}
```

findOrganUserList
===
```sql
SELECT #{page('U.ID,U.USERNAME,U.REAL_NAME AS TITLE,U.SEX,U.AVATAR,U.TEL,U.EMAIL')} 
FROM SYS_USER U
LEFT JOIN SYS_USER_ORGAN UO ON UO.USER_ID=U.ID 
WHERE 1=1 #{use("whereOrganUser")}
```







