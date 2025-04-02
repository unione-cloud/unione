
whereField
===
```
-- @if(isNotEmpty(params.orgId)){
   AND G.ORG_ID = #{params.orgId}
-- @}
-- @if(isNotEmpty(params.status)){
   AND G.STATUS = #{params.status}
-- @}
-- @if(isNotEmpty(params.groupId)){
   AND MB.GROUP_ID = #{params.groupId}
-- @}
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
   AND (G.NAME LIKE #{'%'+keywords+'%'})
-- @}
```


count
===
```sql
SELECT COUNT(GP.ID) 
FROM SYS_GROUP_PERMIS GP 
LEFT JOIN SYS_GROUP G ON GP.GROUP_ID=G.ID 
LEFT JOIN SYS_ORGAN O ON G.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```

findPages
===
```sql
SELECT #{page('GP.*,G.NAME,G.SN,G.STATUS,G.TYPES,O.NAME AS ORG_NAME')} 
FROM SYS_GROUP_PERMIS GP 
LEFT JOIN SYS_GROUP G ON GP.GROUP_ID=G.ID 
LEFT JOIN SYS_ORGAN O ON G.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```
