
statByDay
===
```sql
SELECT count(*) AS VISIT_COUNT,
APP_ID,TENANT_ID,ORG_ID,USER_ID,TARGET_ID,EXPLORER,OSNAME,VISIT_YEAR,VISIT_QUAR,VISIT_MONTH,VISIT_WEEK,VISIT_DAY,COUNTRY,PROVINCE,CITY 
FROM COMM_VISIT_ITEM WHERE VISIT_TIME>=#{params.timeBegin} AND VISIT_TIME<=#{params.timeEnd}
GROUP BY APP_ID,TENANT_ID,ORG_ID,USER_ID,TARGET_ID,EXPLORER,OSNAME,VISIT_YEAR,VISIT_QUAR,VISIT_MONTH,VISIT_WEEK,VISIT_DAY,COUNTRY,PROVINCE,CITY
```


updateStatByDay
===
```sql
UPDATE COMM_VISIT_STAT SET VISIT_COUNT=#{data.visitCount},LAST_UPDATED=#{data.time} 
WHERE APP_ID = #{params.appId} AND TENANT_ID = #{params.tenantId} AND ORG_ID=#{params.orgId} AND USER_ID=#{params.userId} AND TARGET_ID=#{params.targetId} AND EXPLORER=#{params.explorer} AND OSNAME=#{params.osname} AND VISIT_YEAR=#{params.visitYear} AND VISIT_QUAR=#{params.visitQuar} AND VISIT_MONTH=#{params.visitMonth} AND VISIT_WEEK=#{params.visitWeek} AND VISIT_DAY=#{params.visitDay} AND COUNTRY=#{params.country} AND PROVINCE=#{params.province} AND CITY=#{params.city}
```


loadStat
===
```sql
SELECT sum(VISIT_COUNT) AS VISIT_COUNT 
-- @if(notNull(params.dimensions)){
    -- @if(contains(params.dimensions,"app")){
        ,APP_ID
    -- @}
    -- @if(contains(params.dimensions,"tenant")){
        ,TENANT_ID
    -- @}
    -- @if(contains(params.dimensions,"organ")){
        ,ORG_ID
    -- @}
    -- @if(contains(params.dimensions,"user")){
        ,USER_ID
    -- @}
    -- @if(contains(params.dimensions,"target")){
        ,TARGET_ID
    -- @}
    -- @if(contains(params.dimensions,"explorer")){
        ,EXPLORER
    -- @}
    -- @if(contains(params.dimensions,"osname")){
        ,OSNAME
    -- @}
    -- @if(contains(params.dimensions,"year")){
        ,VISIT_YEAR
    -- @}
    -- @if(contains(params.dimensions,"quar")){
        ,VISIT_QUAR
    -- @}
    -- @if(contains(params.dimensions,"month")){
        ,VISIT_MONTH
    -- @}
    -- @if(contains(params.dimensions,"week")){
        ,VISIT_WEEK
    -- @}
    -- @if(contains(params.dimensions,"day")){
        ,VISIT_DAY
    -- @}
    -- @if(contains(params.dimensions,"country")){
        ,COUNTRY
    -- @}
    -- @if(contains(params.dimensions,"province")){
        ,PROVINCE
    -- @}
    -- @if(contains(params.dimensions,"city")){
        ,CITY
    -- @}    
-- @}
FROM COMM_VISIT_STAT WHERE VISIT_DATE>=#{params.timeBegin} AND VISIT_DATE<=#{params.timeEnd}
-- @if(notNull(params.dimensions)){
GROUP BY 
-- @sqlTrim(){
 -- @if(contains(params.dimensions,"app")){
        ,APP_ID
    -- @}
    -- @if(contains(params.dimensions,"tenant")){
        ,TENANT_ID
    -- @}
    -- @if(contains(params.dimensions,"organ")){
        ,ORG_ID
    -- @}
    -- @if(contains(params.dimensions,"user")){
        ,USER_ID
    -- @}
    -- @if(contains(params.dimensions,"target")){
        ,TARGET_ID
    -- @}
    -- @if(contains(params.dimensions,"explorer")){
        ,EXPLORER
    -- @}
    -- @if(contains(params.dimensions,"osname")){
        ,OSNAME
    -- @}
    -- @if(contains(params.dimensions,"year")){
        ,VISIT_YEAR
    -- @}
    -- @if(contains(params.dimensions,"quar")){
        ,VISIT_QUAR
    -- @}
    -- @if(contains(params.dimensions,"month")){
        ,VISIT_MONTH
    -- @}
    -- @if(contains(params.dimensions,"week")){
        ,VISIT_WEEK
    -- @}
    -- @if(contains(params.dimensions,"day")){
        ,VISIT_DAY
    -- @}
    -- @if(contains(params.dimensions,"country")){
        ,COUNTRY
    -- @}
    -- @if(contains(params.dimensions,"province")){
        ,PROVINCE
    -- @}
    -- @if(contains(params.dimensions,"city")){
        ,CITY
    -- @}
-- @}        
-- @}
```
