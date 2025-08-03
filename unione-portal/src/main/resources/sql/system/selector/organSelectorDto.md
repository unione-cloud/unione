

checkpermisOrgan
===
```sql
SELECT O.ID
FROM SYS_ORGAN O
LEFT JOIN SYS_ORGAN_PERMIS OP ON OP.ORG_ID=O.ID 
WHERE O.STATUS=1 AND O.ID IN (#{join(params.ids)}) AND OP.RES_ID=#{params.targetId}
```

loadOrganList
===
```sql
SELECT ID,PARENT_ID AS PID,NAME AS TITLE,TYPES as OTYPE,SN,DESCS
FROM SYS_ORGAN 
WHERE STATUS=1 AND ID IN (#{join(query.ids)})
```