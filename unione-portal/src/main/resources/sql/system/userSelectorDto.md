
countorganUser
===
```sql
SELECT COUNT(U.ID) 
FROM SYS_USER U
LEFT JOIN SYS_USER_ORGAN UO ON UO.USER_ID=U.ID 
WHERE U.STATUS=1 AND (U.ORG_ID = #{params.pid} OR (UO.ORG_ID = #{params.pid} AND UO.STATUS=1))
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
 AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @} 
```

findorganUserList
===
```sql
SELECT #{page('U.ID,U.USERNAME,U.REAL_NAME AS TITLE,U.SEX,U.AVATAR,U.TEL,U.EMAIL,U.ORG_ID,O.NAME AS ORG_NAME')} 
FROM SYS_USER U
LEFT JOIN SYS_USER_ORGAN UO ON UO.USER_ID=U.ID 
LEFT JOIN SYS_ORGAN O ON U.ORG_ID=O.ID
WHERE U.STATUS=1 AND (U.ORG_ID = #{params.pid} OR (UO.ORG_ID = #{params.pid} AND UO.STATUS=1))
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
 AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @} 
```

checkorganUser
===
```sql
SELECT U.ID
FROM SYS_USER U
LEFT JOIN SYS_USER_ORGAN UO ON UO.USER_ID=U.ID 
WHERE U.STATUS=1 AND U.ID IN (#{join(params.ids)}) AND (U.ORG_ID=#{params.targetId} OR (UO.ORG_ID=#{params.targetId} AND UO.STATUS=1))
```




countroleUser
===
```sql
SELECT COUNT(U.ID) 
FROM SYS_USER U
LEFT JOIN SYS_USER_ROLE UR ON UR.USER_ID=U.ID 
WHERE U.STATUS=1 AND UR.ROLE_ID = #{params.pid}
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
 AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @} 
```

findroleUserList
===
```sql
SELECT #{page('U.ID,U.USERNAME,U.REAL_NAME AS TITLE,U.SEX,U.AVATAR,U.TEL,U.EMAIL,U.ORG_ID,O.NAME AS ORG_NAME')} 
FROM SYS_USER U
LEFT JOIN SYS_USER_ROLE UR ON UR.USER_ID=U.ID 
LEFT JOIN SYS_ORGAN O ON U.ORG_ID=O.ID
WHERE U.STATUS=1 AND UR.ROLE_ID = #{params.pid}
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
 AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @} 
```

checkroleUser
===
```sql
SELECT U.ID
FROM SYS_USER U
LEFT JOIN SYS_USER_ROLE UR ON UR.USER_ID=U.ID 
WHERE U.STATUS=1 AND U.ID IN (#{join(params.ids)}) AND UR.ROLE_ID = #{params.targetId}
```



countgroupUser
===
```sql
SELECT COUNT(U.ID) 
FROM SYS_USER U
LEFT JOIN SYS_GROUP_MEMBER MB ON MB.USER_ID=U.ID 
WHERE U.STATUS=1 AND MB.GROUP_ID = #{params.pid} AND MB.STATUS=1
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
 AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @} 
```

findgroupUserList
===
```sql
SELECT #{page('U.ID,U.USERNAME,U.REAL_NAME AS TITLE,U.SEX,U.AVATAR,U.TEL,U.EMAIL,U.ORG_ID,O.NAME AS ORG_NAME')} 
FROM SYS_USER U
LEFT JOIN SYS_GROUP_MEMBER MB ON MB.USER_ID=U.ID 
LEFT JOIN SYS_ORGAN O ON U.ORG_ID=O.ID
WHERE U.STATUS=1 AND MB.GROUP_ID = #{params.pid} AND MB.STATUS=1
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
 AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @} 
```

checkgroupUser
===
```sql
SELECT U.ID
FROM SYS_USER U
LEFT JOIN SYS_GROUP_MEMBER MB ON MB.USER_ID=U.ID 
WHERE U.STATUS=1 AND U.ID IN (#{join(params.ids)}) AND (MB.GROUP_ID=#{params.targetId} AND MB.STATUS=1)
```




countpostUser
===
```sql
SELECT COUNT(U.ID) 
FROM SYS_USER U
LEFT JOIN SYS_USER_POST UP ON UP.USER_ID=U.ID 
WHERE U.STATUS=1 AND UP.POST_ID = #{params.pid} AND UP.STATUS=1
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
 AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @} 
```

findpostUserList
===
```sql
SELECT #{page('U.ID,U.USERNAME,U.REAL_NAME AS TITLE,U.SEX,U.AVATAR,U.TEL,U.EMAIL,U.ORG_ID,O.NAME AS ORG_NAME')} 
FROM SYS_USER U
LEFT JOIN SYS_USER_POST UP ON UP.USER_ID=U.ID 
LEFT JOIN SYS_ORGAN O ON U.ORG_ID=O.ID
WHERE U.STATUS=1 AND UP.POST_ID = #{params.pid} AND UP.STATUS=1
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
 AND (U.USERNAME LIKE #{'%'+keywords+'%'} OR U.REAL_NAME LIKE #{'%'+keywords+'%'})
-- @} 
```

checkpostUser
===
```sql
SELECT U.ID
LEFT JOIN SYS_USER_POST UP ON UP.USER_ID=U.ID 
WHERE U.STATUS=1 AND U.ID IN (#{join(params.ids)}) AND (UP.POST_ID=#{params.targetId} AND UP.STATUS=1)
```
