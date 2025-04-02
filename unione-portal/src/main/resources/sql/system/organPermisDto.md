
whereField
===
```
-- @if(isNotEmpty(params.orgId)){
   AND O.ORG_ID = #{params.orgId}
-- @}
-- @if(isNotEmpty(params.status)){
   AND O.STATUS = #{params.status}
-- @}
-- @if(isNotEmpty(keywords) && !isBlank(keywords)){
   AND (O.NAME LIKE #{'%'+keywords+'%'})
-- @}
```


count
===
```sql
SELECT COUNT(OP.ID) 
FROM SYS_ORGAN_PERMIS OP 
LEFT JOIN SYS_ORGAN O ON OP.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```

findPages
===
```sql
SELECT #{page('OP.*,O.NAME,O.ALIAS,O.SN,O.TYPES,O.STATUS')} 
FROM SYS_ORGAN_PERMIS OP 
LEFT JOIN SYS_ORGAN O ON OP.ORG_ID=O.ID
WHERE 1=1 #{use("whereField")}
```
