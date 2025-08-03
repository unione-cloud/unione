

checkpermisGroup
===
```sql
SELECT G.ID
FROM SYS_GROUP G
LEFT JOIN SYS_GROUP_PERMIS GP ON GP.GROUP_ID=G.ID 
WHERE G.STATUS=1 AND G.ID IN (#{join(params.ids)}) AND GP.RES_ID=#{params.targetId}
```

loadGroupList
===
```sql
SELECT ID,PARENT_ID AS PID,NAME AS TITLE,TYPES as NTYPE,SN,DESCS
FROM SYS_GROUP 
WHERE STATUS=1 AND ID IN (#{join(query.ids)})
```