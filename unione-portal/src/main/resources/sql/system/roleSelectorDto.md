

selectRole4Auth
===
```sql
SELECT DISTINCT R.ID,R.NAME AS TITLE,R.TYPES AS NTYPE,R.SN,R.DESCS
FROM SYS_ROLE R
LEFT JOIN SYS_USER_ROLE UR ON UR.ROLE_ID=R.ID 
WHERE R.STATUS=1
-- @if(isNotEmpty(params.isTenantAdmin)){
  AND (R.TYPES=1 OR R.TENANT_ID = #{params.tenantId})
-- @}
-- @if(isNotEmpty(params.isOrganAdmin)){
  AND (
        R.TYPES=3 AND R.TENANT_ID = #{params.tenantId} AND R.ORG_ID = #{params.orgId} OR 
        UR.USER_ID=#{params.userId} AND UR.EN_DILIVERY=1 AND (R.TENANT_ID = #{params.tenantId} OR R.TYPE=1)
    )
-- @}
```

selectRole4Use
===
```sql
SELECT ID,NAME AS TITLE,TYPES AS NTYPE,SN,DESCS
FROM SYS_ROLE
WHERE STATUS=1 AND TENANT_ID = #{params.tenantId} AND (
  TYPES=2 
-- @if(isNotEmpty(params.orgId)){
  OR TYPES=3 AND ORG_ID = #{params.orgId}
-- @}
)
```

selectRolePage4Use
===
```sql
SELECT #{page("ID,NAME AS TITLE,TYPES AS NTYPE,SN,DESCS")}
FROM SYS_ROLE
WHERE STATUS=1 AND TENANT_ID = #{params.tenantId} AND (
  TYPES=2 
-- @if(isNotEmpty(params.orgId)){
  OR TYPES=3 AND ORG_ID = #{params.orgId}
-- @}
)
-- @if(isNotEmpty(params.keywords)){
  AND NAME LIKE #{'%'+params.keywords+'%'}
-- @}
-- @if(isNotEmpty(params.types)){
  AND TYPES = #{params.types}
-- @}
```

countRole4Use
===
```sql
SELECT count(*) FROM SYS_ROLE
WHERE STATUS=1 AND TENANT_ID = #{params.tenantId} AND (
  TYPES=2 
-- @if(isNotEmpty(params.orgId)){
  OR TYPES=3 AND ORG_ID = #{params.orgId}
-- @}
)
-- @if(isNotEmpty(params.keywords)){
  AND NAME LIKE #{'%'+params.keywords+'%'}
-- @}
-- @if(isNotEmpty(params.types)){
  AND TYPES = #{params.types}
-- @}
```