

checkpermisOrgan
===
```sql
SELECT O.ID
FROM SYS_ORGAN O
LEFT JOIN SYS_ORGAN_PERMIS OP ON OP.ORG_ID=O.ID 
WHERE O.STATUS=1 AND O.ID IN (#{join(params.ids)}) AND OP.RES_ID=#{params.targetId}
```