

checkpermisPost
===
```sql
SELECT P.ID
FROM SYS_POST P
LEFT JOIN SYS_POST_PERMIS PP ON PP.POST_ID=P.ID 
WHERE P.STATUS=1 AND P.ID IN (#{join(params.ids)}) AND PP.RES_ID=#{params.targetId}
```

loadPostList
===
```sql
SELECT ID,PARENT_ID AS PID,NAME AS TITLE,TYPES as NTYPE,SN,DESCS
FROM SYS_POST 
WHERE STATUS=1 AND ID IN (#{join(query.ids)})
```