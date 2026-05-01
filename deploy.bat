chcp 65001

REM 获取脚本参数
SET HOST=%1
SET APP=%2
if "%APP%"=="" (
 SET APP=portal
)
if "%HOST%"=="gateway" (
 SET APP=gateway
 SET HOST=CLOUD
)
if "%HOST%"=="" (
 SET HOST=CLOUD
)

REM 拼接变量
call set UNIONE_PASSWD=%%UNIONE_%HOST%_PASSWD%%
call set UNIONE_HOST=%%UNIONE_%HOST%_HOST%%

if "%UNIONE_HOST%"=="" (
 REM 退出脚本
 echo "请指定主机名"
 exit
)

REM 输出HOST
echo HOST: %UNIONE_HOST% APP: %APP%

call mvn clean package -Dspring.boot.skip=false 

REM 删除旧的jar包
plink -pw %UNIONE_PASSWD% root@%UNIONE_HOST% "cd /opt/builder/jars ; rm -f %APP%.jar"

REM 使用 pscp 上传jar包
if %APP%==portal (
    pscp -pw %UNIONE_PASSWD% unione-starter/target/%APP%.jar root@%UNIONE_HOST%:/opt/builder/jars
) else (
    pscp -pw %UNIONE_PASSWD% unione-%APP%/target/%APP%.jar root@%UNIONE_HOST%:/opt/builder/jars
)

REM 使用 plink 执行build脚本
plink -pw %UNIONE_PASSWD% root@%UNIONE_HOST% "cd /opt/builder; sh build %APP%"
REM 程序更新完成
pause