

validPermis
===
```sql
SELECT COUNT(*)
FROM DOC_FILE f
LEFT JOIN DOC_PERMIS p ON f.ID=p.FILE_ID OR f.TYPE='dir' AND f.LV_SN LIKE p.FILE_LVSN
WHERE f.TENANT_ID=#{principal.tenantId} AND f.DEL_FLAG=0 AND (f.IS_SHARE=0 AND f.IS_PUBLIC=0 OR f.AUDIT_STATUS=2) 
-- @if(notNull(params.id)){
AND f.ID=#{params.id}
-- @} 
-- @if(notNull(params.ids)){
AND f.ID IN #{join(params.ids)}
-- @} 
-- @if(notNull(params.ownerId)){
AND f.OWNER_ID=#{params.ownerId}
-- @} 
AND (
    f.USER_ID=#{query.permisUser}
-- @if(notNull(params.permisOrg)){
    OR f.ORG_ID=#{params.permisOrg}
-- @}      
    OR 
    p.DEL_FLAG=0 AND p.AUDIT_RESULT=2 AND p.LIST IN #{join(#{params.permisTypes})} AND (
        p.OWNER_ID IN #{join(params.permisOwners)}
    -- @if(notNull(params.permisRoles)){
        OR f.OWNER_NAME IN #{join(params.permisRoles)}
    -- @}  
    )
)
```

fileFields
===
```sql
  f.ID,
  f.TENANT_ID,
  f.DIR_ID,
  f.ORG_ID,
  f.USER_ID,
  f.OWNER_ID,
  f.APP_CODE,
  f.TITLE,
  f.NAME,
  f.LV_SN,
  f.LV_NO,
  f.SIZE,
  f.TYPE,
  f.PATH,
  f.ORDERED,
  f.STATUS,
  f.IS_PUBLIC,
  f.IS_SHARE,
  f.AUDIT_STATUS,
  f.DESCS,
  f.DEL_FLAG,
  f.CREATED,
  f.CREATED_BY,
  f.LAST_UPDATED,
  f.LAST_UPDATED_BY
```


permisFilter
===
```sql
LEFT JOIN DOC_PERMIS p ON f.ID=p.FILE_ID OR f.TYPE='dir' AND f.LV_SN LIKE p.FILE_LVSN
WHERE f.TENANT_ID=#{params.tenantId} AND f.DEL_FLAG=0 AND (f.IS_SHARE=0 AND f.IS_PUBLIC=0 OR f.AUDIT_STATUS=2) 
-- @if(notNull(params.id)){
AND f.ID=#{params.id}
-- @} 
-- @if(notNull(params.ids)){
AND f.ID IN #{join(params.ids)}
-- @} 
-- @if(notNull(params.ownerId)){
AND f.OWNER_ID=#{params.ownerId}
-- @} 
-- @if(notNull(params.isPublic)){
AND f.IS_PUBLIC=#{params.isPublic}
-- @} 
-- @if(notNull(params.isShare)){
AND f.IS_SHARE=#{params.isShare}
-- @} 
-- @if(notNull(params.auditStatus)){
AND f.AUDIT_STATUS=#{params.auditStatus}
-- @} 
-- @if(notNull(params.name)){
AND f.NAME=#{params.name}
-- @} 
-- @if(notNull(params.dirId)){
AND f.DIR_ID=#{params.dirId}
-- @} 
-- @if(notNull(params.lvsn)){
AND f.LV_SN LIKE #{params.lvsn+'%'}
-- @} 
-- @if(notNull(params.incTypes)){
AND f.TYPE IN #{join(params.incTypes)}
-- @} 
-- @if(notNull(params.ninTypes)){
AND f.TYPE NOT IN #{join(params.ninTypes)}
-- @} 
-- @if(notNull(query.keywords)){
AND (f.TITLE LIKE #{'%'+query.keywords+'%'} OR f.DESCS LIKE #{'%'+query.keywords+'%'})
-- @} 
AND (
    f.USER_ID=#{query.permisUser}
-- @if(notNull(params.permisOrg)){
    OR f.ORG_ID=#{params.permisOrg}
-- @}      
    OR 
    p.DEL_FLAG=0 AND p.AUDIT_RESULT=2 AND p.LIST IN #{join(#{params.permisTypes})} AND (
        p.OWNER_ID IN #{join(params.permisOwners)}
    -- @if(notNull(params.permisRoles)){
        OR f.OWNER_NAME IN #{join(params.permisRoles)}
    -- @}  
    )
)
```


findDocList
===
```sql
SELECT #{page(use("fileFields"))}
FROM DOC_FILE f
#use("permisFilter")
```
countDocList
===
```sql
SELECT COUNT(*)
FROM DOC_FILE f
#use("permisFilter")
```


shareFilter
===
```sql
LEFT JOIN DOC_PERMIS p ON f.ID=p.FILE_ID OR f.TYPE='dir' AND f.LV_SN LIKE p.FILE_LVSN
WHERE f.TENANT_ID=#{params.tenantId} AND f.DEL_FLAG=0 AND (f.IS_SHARE=0 AND f.IS_PUBLIC=0 OR f.AUDIT_STATUS=2) 
-- @if(notNull(params.id)){
AND f.ID=#{params.id}
-- @} 
-- @if(notNull(params.ids)){
AND f.ID IN #{join(params.ids)}
-- @} 
-- @if(notNull(params.ownerId)){
AND f.OWNER_ID=#{params.ownerId}
-- @} 
-- @if(notNull(params.isPublic)){
AND f.IS_PUBLIC=#{params.isPublic}
-- @} 
-- @if(notNull(params.isShare)){
AND f.IS_SHARE=#{params.isShare}
-- @} 
-- @if(notNull(params.auditStatus)){
AND f.AUDIT_STATUS=#{params.auditStatus}
-- @} 
-- @if(notNull(params.name)){
AND f.NAME=#{params.name}
-- @} 
-- @if(notNull(params.dirId)){
AND f.DIR_ID=#{params.dirId}
-- @} 
-- @if(notNull(params.lvsn)){
AND f.LV_SN LIKE #{params.lvsn+'%'}
-- @} 
-- @if(notNull(params.incTypes)){
AND f.TYPE IN #{join(params.incTypes)}
-- @} 
-- @if(notNull(params.ninTypes)){
AND f.TYPE NOT IN #{join(params.ninTypes)}
-- @} 
-- @if(notNull(query.keywords)){
AND (f.TITLE LIKE #{'%'+query.keywords+'%'} OR f.DESCS LIKE #{'%'+query.keywords+'%'})
-- @} 
AND (
    f.USER_ID != #{principal.id}
-- @if(notNull(params.permisOrg)){
    AND f.ORG_ID != #{params.permisOrg}
-- @}      
    OR 
    p.DEL_FLAG=0 AND p.AUDIT_RESULT=2 AND p.LIST IN #{join(#{params.permisTypes})} AND (
        p.OWNER_ID IN #{join(params.permisOwners)}
    -- @if(notNull(params.permisRoles)){
        OR f.OWNER_NAME IN #{join(params.permisRoles)}
    -- @}  
    )
)
```


findShareMine
===
```sql
SELECT #{page(use("fileFields"))}
FROM DOC_FILE f
#use("shareFilter")
```
countShareMine
===
```sql
SELECT COUNT(*)
FROM DOC_FILE f
#use("shareFilter")
```