chcp 65001
REM 编译源码
mvn clean package -Dspring.boot.skip=false && deploy.bat
REM 程序更新完成
pause