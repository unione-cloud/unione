loadSysResourceTree
===
```sql
SELECT * FROM SYS_RESOURCE res
WHERE STATUS = 1 AND (IS_PLATFORM=1 OR TENANT_ID=#{params.tenantId}) AND APP_ID IN (#{join(params.appIds)})
-- @if(!isEqules("view",params.type) && params.isAdmin==false){
   AND (
       EXISTS (SELECT 1 FROM SYS_USER_PERMIS sup WHERE sup.USER_ID=#{params.userId} AND sup.RES_ID=res.ID AND EN_DILIVERY=1) OR
       EXISTS (SELECT 1 FROM SYS_ROLE_PERMIS srp WHERE srp.RES_ID=res.ID AND EN_DILIVERY=1 AND 
           srp.ROLE_ID IN (SELECT ROLE_ID FROM SYS_USER_ROLE sur WHERE sur.USER_ID=#{params.userId})
       )
   )
-- @}
ORDER BY res.ORDERED ASC
```