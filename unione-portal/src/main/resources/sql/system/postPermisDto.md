
whereField
===
```
-- @if(isNotEmpty(params.orgId)){
   AND O.ORG_ID = #{params.orgId}
-- @}
-- @if(isNotEmpty(params.status)){
   AND P.STATUS = #{params.status}
-- @}
-- @if(isNotEmpty(params.postId)){
   AND PP.POST_ID = #{params.postId}
-- @}
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
   AND (P.NAME LIKE #{'%'+keywords+'%'})
-- @}
```


count
===
```sql
SELECT COUNT(PP.ID) 
FROM SYS_POST_PERMIS PP 
LEFT JOIN SYS_POST P ON PP.POST_ID=P.ID
LEFT JOIN SYS_ORGAN O ON P.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```

findPages
===
```sql
SELECT #{page('PP.*,P.NAME,P.SN,P.TYPES,P.STATUS,O.NAME AS ORG_NAME')} 
FROM SYS_POST_PERMIS PP 
LEFT JOIN SYS_POST P ON PP.POST_ID=P.ID
LEFT JOIN SYS_ORGAN O ON P.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```
