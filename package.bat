chcp 65001
REM 编译源码
mvn clean package -Dspring.boot.skip=false
REM 程序更新完成
pause