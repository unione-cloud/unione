

deleteSystemPage
===
```sql
DELETE FROM SYS_PAGE_DEFINE WHERE VERS=1 AND ID IN (#{join(query.ids)})
```

deleteSystemPageLogic
===
```sql
UPDATE SYS_PAGE_DEFINE SET DEL_FLAG=1 WHERE VERS>1 AND ID IN (#{join(query.ids)})
```