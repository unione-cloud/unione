query
===
```
SELECT join(finder) FROM unioneTable(finder) unioneWhere(finder)
```

query_byid
===
```
SELECT join(finder) FROM unioneTable(finder) unioneWhere(finder,"byId")
```

update
===
```
UPDATE unioneTable(updater) SET unioneUpdateField(updater) unioneWhere(updater)
```

update_byid
===
```
UPDATE unioneTable(updater) SET unioneUpdateField(updater) unioneWhere(updater,"byId")
```

orderby
===
```
-- @if(!isEmpty(sorts)){
   ORDER BY #{text(sorts)}
-- @}
```
