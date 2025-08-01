
statByDay
===
```sql
SELECT count(*) AS VISIT_COUNT,
APP_ID,TENANT_ID,ORG_ID,USER_ID,TARGET_ID,EXPLORER,OSNAME,VISIT_YEAR,VISIT_QUAR,VISIT_MONTH,VISIT_WEEK,VISIT_DAY,COUNTRY,PROVINCE,CITY 
FROM COMM_VISIT_ITEM WHERE VISIT_TIME>#{params.timeBegin} AND VISIT_TIME<#{params.timeEnd}
GROUP BY APP_ID,TENANT_ID,ORG_ID,USER_ID,TARGET_ID,EXPLORER,OSNAME,VISIT_YEAR,VISIT_QUAR,VISIT_MONTH,VISIT_WEEK,VISIT_DAY,COUNTRY,PROVINCE,CITY
```


updateStatByDay
===
```sql
UPDATE COMM_VISIT_STAT SET VISIT_COUNT=#{data.visitCount},LAST_UPDATED=#{data.time} 
WHERE APP_ID = #{params.appId} AND TENANT_ID = #{params.tenantId} AND ORG_ID=#{params.orgId} AND USER_ID=#{params.userId} AND TARGET_ID=#{params.targetId} AND EXPLORER=#{params.explorer} AND OSNAME=#{params.osname} AND VISIT_YEAR=#{params.visitYear} AND VISIT_QUAR=#{params.visitQuar} AND VISIT_MONTH=#{params.visitMonth} AND VISIT_WEEK=#{params.visitWeek} AND VISIT_DAY=#{params.visitDay} AND COUNTRY=#{params.country} AND PROVINCE=#{params.province} AND CITY=#{params.city}
```