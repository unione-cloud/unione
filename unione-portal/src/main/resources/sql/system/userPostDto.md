
whereField
===
```
-- @if(isNotEmpty(params.userId)){
   AND MB.USER_ID = #{params.userId}
-- @}
-- @if(isNotEmpty(params.userSts)){
   AND U.STATUS = #{params.userSts}
-- @}
-- @if(isNotEmpty(params.status)){
   AND MB.STATUS = #{params.status}
-- @}
-- @if(isNotEmpty(params.postId)){
   AND MB.POST_ID = #{params.postId}
-- @}
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
   AND (MB.NAME LIKE #{'%'+keywords+'%'} OR MB.ORG_NAME LIKE #{'%'+keywords+'%'} OR U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @}
```


count
===
```sqlUserRoleDto
SELECT COUNT(MB.ID) 
FROM SYS_USER_POST MB 
LEFT JOIN SYS_USER U ON MB.USER_ID=U.ID 
WHERE 1=1 #{use("whereField")}
```

findPages
===
```sql
SELECT #{page('MB.*,U.USERNAME,U.REAL_NAME,U.SEX,U.AVATAR,U.STATUS AS USER_STS')} 
FROM SYS_USER_POST MB
LEFT JOIN SYS_USER U ON MB.USER_ID=U.ID 
WHERE 1=1 #{use("whereField")}
```
