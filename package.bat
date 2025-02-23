chcp 65001
REM 编译源码
mvn clean package -Dspring.boot.skip=false
pause
REM 使用 pscp 上传jar包
pscp -pw %UNIONE_PASSWD% unione-start/target/unione-start-1.0.0-SNAPSHOT.jar root@%UNIONE_HOST%:/opt/unione/portal
REM 使用 plink 执行build脚本
plink -pw %UNIONE_PASSWD% root@%UNIONE_HOST% "cd /opt/unione/portal ; sh build"
REM 程序更新完成
pause