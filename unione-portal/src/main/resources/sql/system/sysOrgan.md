
tableColumn
===
```
ID,TENANT_ID,PARENT_ID,AREA_CODE,AREA_LABEL,NAME,ALIAS,CODES,TYPES,BUSI_MAIN,BUSI_SCOP,ADDR,TEL,LEVELS,IS_LEAF,ORDERED,STATUS,DESCS,CREATED,CREATED_BY,LAST_UPDATED,LAST_UPDATED_BY
```

updateCloume
===
```
-- @if(!isNotEmpty(fields.id)){
   ID=#{params.id},
-- @}
-- @if(!isNotEmpty(fields.tenantId)){
   TENANT_ID=#{params.tenantId},
-- @}
-- @if(!isNotEmpty(fields.parentId)){
   PARENT_ID=#{params.parentId},
-- @}
-- @if(!isNotEmpty(fields.areaCode)){
   AREA_CODE=#{params.areaCode},
-- @}
-- @if(!isNotEmpty(fields.areaLabel)){
   AREA_LABEL=#{params.areaLabel},
-- @}
-- @if(!isNotEmpty(fields.name)){
   NAME=#{params.name},
-- @}
-- @if(!isNotEmpty(fields.alias)){
   ALIAS=#{params.alias},
-- @}
-- @if(!isNotEmpty(fields.codes)){
   CODES=#{params.codes},
-- @}
-- @if(!isNotEmpty(fields.types)){
   TYPES=#{params.types},
-- @}
-- @if(!isNotEmpty(fields.busiMain)){
   BUSI_MAIN=#{params.busiMain},
-- @}
-- @if(!isNotEmpty(fields.busiScop)){
   BUSI_SCOP=#{params.busiScop},
-- @}
-- @if(!isNotEmpty(fields.addr)){
   ADDR=#{params.addr},
-- @}
-- @if(!isNotEmpty(fields.tel)){
   TEL=#{params.tel},
-- @}
-- @if(!isNotEmpty(fields.levels)){
   LEVELS=#{params.levels},
-- @}
-- @if(!isNotEmpty(fields.isLeaf)){
   IS_LEAF=#{params.isLeaf},
-- @}
-- @if(!isNotEmpty(fields.ordered)){
   ORDERED=#{params.ordered},
-- @}
-- @if(!isNotEmpty(fields.status)){
   STATUS=#{params.status},
-- @}
-- @if(!isNotEmpty(fields.descs)){
   DESCS=#{params.descs},
-- @}
-- @if(!isNotEmpty(fields.created)){
   CREATED=#{params.created},
-- @}
-- @if(!isNotEmpty(fields.createdBy)){
   CREATED_BY=#{params.createdBy},
-- @}
-- @if(!isNotEmpty(fields.lastUpdated)){
   LAST_UPDATED=#{params.lastUpdated},
-- @}
-- @if(!isNotEmpty(fields.lastUpdatedBy)){
   LAST_UPDATED_BY=#{params.lastUpdatedBy}
-- @}
```


whereCondition
===
```
-- @if(!isEmpty(params.id)){
  AND ID=#{params.id}
-- @}
-- @if(!isEmpty(params.tenantId)){
  AND TENANT_ID=#{params.tenantId}
-- @}
-- @if(!isEmpty(params.parentId)){
  AND PARENT_ID=#{params.parentId}
-- @}
-- @if(!isEmpty(params.areaCode)){
  AND AREA_CODE=#{params.areaCode}
-- @}
-- @if(!isEmpty(params.areaLabel)){
  AND AREA_LABEL=#{params.areaLabel}
-- @}
-- @if(!isEmpty(params.name)){
  AND NAME=#{params.name}
-- @}
-- @if(!isEmpty(params.alias)){
  AND ALIAS=#{params.alias}
-- @}
-- @if(!isEmpty(params.codes)){
  AND CODES=#{params.codes}
-- @}
-- @if(!isEmpty(params.types)){
  AND TYPES=#{params.types}
-- @}
-- @if(!isEmpty(params.busiMain)){
  AND BUSI_MAIN=#{params.busiMain}
-- @}
-- @if(!isEmpty(params.busiScop)){
  AND BUSI_SCOP=#{params.busiScop}
-- @}
-- @if(!isEmpty(params.addr)){
  AND ADDR=#{params.addr}
-- @}
-- @if(!isEmpty(params.tel)){
  AND TEL=#{params.tel}
-- @}
-- @if(!isEmpty(params.levels)){
  AND LEVELS=#{params.levels}
-- @}
-- @if(!isEmpty(params.isLeaf)){
  AND IS_LEAF=#{params.isLeaf}
-- @}
-- @if(!isEmpty(params.ordered)){
  AND ORDERED=#{params.ordered}
-- @}
-- @if(!isEmpty(params.status)){
  AND STATUS=#{params.status}
-- @}
-- @if(!isEmpty(params.descs)){
  AND DESCS=#{params.descs}
-- @}
-- @if(!isEmpty(params.created)){
  AND CREATED=#{params.created}
-- @}
-- @if(!isEmpty(params.createdBy)){
  AND CREATED_BY=#{params.createdBy}
-- @}
-- @if(!isEmpty(params.lastUpdated)){
  AND LAST_UPDATED=#{params.lastUpdated}
-- @}
-- @if(!isEmpty(params.lastUpdatedBy)){
  AND LAST_UPDATED_BY=#{params.lastUpdatedBy}
-- @}

*关键字搜索，自行根据需要开启
*-- @if(!isEmpty(query.keywords)){
* AND (FIELD1 LIKE #{'%'+query.keywords+'%'} OR FIELD2 LIKE #{'%'+query.keywords+'%'})
*-- @}
```


byIdCondition
===
```
-- @if(isEmpty(query.id) && isEmpty(query.ids)){
  AND 1=2
-- @}
-- @if(isNotEmpty(query.id)){
  AND ID = query.id;
-- @}
-- @if(isNotEmpty(query.ids)){
  AND ID IN ( #{join(query.ids)} )
-- @}
-- @if(isNotEmpty(params.tenantId)){
  AND TENANT_ID = params.tenantId
-- @}
-- @if(isNotEmpty(params.orgId)){
  AND ORG_ID = params.orgId
-- @}
```


update
===
```sql
UPDATE sys_organ SET #{use("updateCloume")} WHERE 1=1 #{use("whereCondition")}
```

updateById
===
```sql
UPDATE sys_organ SET #{use("updateCloume")} WHERE 1=1 #{use("byIdCondition")}
```

delete
===
```sql
DELETE FROM sys_organ WHERE 1=1 #{use("whereCondition")}
```

deleteById
===
```sql
DELETE FROM sys_organ WHERE 1=1 #{use("byIdCondition")}
```

deleteLogic
===
```sql
UPDATE sys_organ
SET DEL_FLAG=1,LAST_UPDATED = #{params.lastUpdated},LAST_UPDATED_BY = #{params.lastUpdatedBy}
WHERE 1=1 #{use("whereCondition")}
```

deleteLogicById
===
```sql
UPDATE sys_organ
SET DEL_FLAG=1,LAST_UPDATED = #{params.lastUpdated},LAST_UPDATED_BY = #{params.lastUpdatedBy}
WHERE 1=1 #{use("byIdCondition")}
```


count
===
```sql
SELECT COUNT(*) FROM sys_organ WHERE 1=1 #{use("whereCondition")}
```

findUnique
===
```sql
SELECT #{use("tableColumn")} FROM sys_organ WHERE 1=1 #{use("whereCondition")}
```

findOne
===
```sql
SELECT #{use("tableColumn")} FROM sys_organ WHERE 1=1 #{use("whereCondition")}
```

findById
===
```sql
SELECT #{use("tableColumn")} FROM sys_organ WHERE 1=1 #{use("byIdCondition")}
```

findList
===
```sql
SELECT #{use("tableColumn")} FROM sys_organ WHERE 1=1 #{use("whereCondition")}
```

findPages
===
```sql
SELECT #{page('*')} FROM sys_organ WHERE 1=1 #{use("whereCondition")}
```



