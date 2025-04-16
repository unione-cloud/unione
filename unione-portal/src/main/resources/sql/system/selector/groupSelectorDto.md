

checkpermisGroup
===
```sql
SELECT G.ID
FROM SYS_GROUP G
LEFT JOIN SYS_GROUP_PERMIS GP ON GP.GROUP_ID=G.ID 
WHERE G.STATUS=1 AND G.ID IN (#{join(params.ids)}) AND GP.RES_ID=#{params.targetId}
```