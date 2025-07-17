whereField
===
```
((stats.ID IS NULL AND
 EXISTS (
 SELECT 1 FROM UMS_MESSAGE_TARGET WHERE MESSAGE_ID=msg.ID AND (
	TARGET_TYPE=1 
    OR (TARGET_TYPE=2 AND TARGET_ID=#{principal.tenantId})
-- @if(isNotEmpty(principal.orgIds)){    
    OR (TARGET_TYPE=3 AND TARGET_ID IN (#{join(principal.orgIds)})) 
-- @}  
    OR (TARGET_TYPE=4 AND TARGET_ID=#{principal.id})    
-- @if(isNotEmpty(principal.userRoles)){        
    OR (TARGET_TYPE=5 AND TARGET_ID IN (SELECT ID FROM SYS_ROLE WHERE SN IN (#{join(principal.userRoles)})))
-- @} 
 ))
) OR stats.DEL_FLAG=0 AND stats.USER_ID=#{principal.id})

-- @if(isNotEmpty(query.id)){
AND msg.ID=#{query.id}
-- @}  
-- @if(isNotEmpty(params.fromId)){
AND msg.FROM_ID=#{params.fromId}
-- @}    
-- @if(isNotEmpty(params.categoryId)){
AND msg.CATEGORY_ID=#{params.categoryId}
-- @}   
-- @if(isNotEmpty(params.types)){
AND msg.TYPES=#{params.types}
-- @}   
-- @if(isNotEmpty(params.timeBegine)){
AND msg.PUBLIC_DATE >= #{params.timeBegine}
-- @} 
-- @if(isNotEmpty(params.timeEnd)){
AND msg.PUBLIC_DATE <= #{params.timeEnd}
-- @} 
-- @if(isNotEmpty(params.viewSts) && params.viewSts==1){
AND stats.VIEW_STS=1
-- @}   
-- @if(isNotEmpty(params.viewSts) && params.viewSts==0){
AND (stats.VIEW_STS=0 OR stats.CONFIRM_STATUS=0)
-- @}   
-- @if(isNotEmpty(keywords)){
AND (msg.TITLE LIKE #{'%'+keywords+'%'} OR msg.BODY_HTML like #{'%'+keywords+'%'})
-- @}  

```

countMine
===
```sql
SELECT count(*)
FROM UMS_MESSAGE msg 
left JOIN (SELECT * from UMS_MESSAGE_STATUS WHERE USER_ID=#{principal.id}) stats ON msg.ID=stats.MESSAGE_ID
WHERE msg.DEL_FLAG=0 AND #{use("whereField")}
```

loadMine
===
```sql
SELECT msg.*, 
stats.id as MINE_ID,stats.VIEW_STS,stats.VIEW_TIME,stats.CONFIRM_STATUS,stats.CONFIRM_DATE,stats.CONFIRM_RESULT,stats.REPLY_INFO
FROM UMS_MESSAGE msg 
left JOIN (SELECT * from UMS_MESSAGE_STATUS WHERE USER_ID=#{principal.id}) stats ON msg.ID=stats.MESSAGE_ID
WHERE msg.DEL_FLAG=0 AND #{use("whereField")}
ORDER BY msg.ID DESC
```

