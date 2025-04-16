

checkpermisPost
===
```sql
SELECT P.ID
FROM SYS_POST GP
LEFT JOIN SYS_POST_PERMIS PP ON PP.POST_ID=P.ID 
WHERE P.STATUS=1 AND P.ID IN (#{join(params.ids)}) AND PP.RES_ID=#{params.targetId}
```