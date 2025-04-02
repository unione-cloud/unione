
whereField
===
```
-- @if(isNotEmpty(params.orgId)){
   AND O.ORG_ID = #{params.orgId}
-- @}
-- @if(isNotEmpty(params.status)){
   AND U.STATUS = #{params.status}
-- @}
-- @if(isNotEmpty(params.userId)){
   AND UP.USER_ID = #{params.userId}
-- @}
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
   AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @}
```


count
===
```sql
SELECT COUNT(UP.ID) 
FROM SYS_USER_PERMIS UP 
LEFT JOIN SYS_USER U ON UP.USER_ID=U.ID
LEFT JOIN SYS_ORGAN O ON U.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```

findPages
===
```sql
SELECT #{page('UP.*,U.USERNAME,U.REAL_NAME,U.USER_TYPE,U.STATUS,O.NAME AS ORG_NAME')} 
FROM SYS_USER_PERMIS UP 
LEFT JOIN SYS_USER U ON UP.USER_ID=U.ID
LEFT JOIN SYS_ORGAN O ON U.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```
