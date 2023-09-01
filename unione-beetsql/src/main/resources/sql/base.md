update
===
```
UPDATE #{unioneTable(updater)} SET #{unioneUpdateField(updater)} #{unioneWhere(updater)}
```

updateById
===
```
UPDATE #{unioneTable(updater)} SET #{unioneUpdateField(updater)} #{unioneWhere(updater,"byId")}
```

delete
===
```sql
DELETE FROM #{unioneTable(deleter)} #{unioneWhere(deleter)}
```

deleteById
===
```sql
DELETE FROM #{unioneTable(deleter)} #{unioneWhere(deleter,"byId")}
```

deleteLogic
===
```sql
UPDATE #{unioneTable(deleter)} 
SET DEL_FLAG=1,LAST_UPDATED = #{deleter.params.lastUpdated},LAST_UPDATED_BY = #{deleter.params.lastUpdatedBy}
#{unioneWhere(deleter)}
```

deleteLogicById
===
```sql
UPDATE #{unioneTable(deleter)} 
SET DEL_FLAG=1,LAST_UPDATED = #{deleter.params.lastUpdated},LAST_UPDATED_BY = #{deleter.params.lastUpdatedBy}
#{unioneWhere(deleter,"byId")}
```

count
===
```sql
SELECT COUNT(*) FROM #{unioneTable(finder)} #{unioneWhere(finder)}
```

findUnique
===
```sql
SELECT #{unioneField(finder)} FROM #{unioneTable(finder)} #{unioneWhere(finder)}
```


findList
===
```
SELECT #{unioneField(finder)} FROM #{unioneTable(finder)} #{unioneWhere(finder)}
```

findById
===
```
SELECT #{unioneField(finder)} FROM #{unioneTable(finder)} #{unioneWhere(finder,"byId")}
```

findByIds
===
```
SELECT #{unioneField(finder)} FROM #{unioneTable(finder)} #{unioneWhere(finder,"byIds")}
```


orderby
===
```
-- @if(!isEmpty(sorts)){
   ORDER BY #{text(sorts)}
-- @}
```
