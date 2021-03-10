#!/bin/sh
# nohup java -jar ccweb-start-2.0.0-SNAPSHOT.jar -Dwatchdocker=true >ccweb.log 2>&1 &
nohup java -jar -Dserver.port=9090 -Dserver.ssl.port=9091 ccweb-start-2.0.0-SNAPSHOT.jar -Xms1024m -Xmx1024m -Xss256k -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 -Duser.timezone=GMT+08 >ccweb.log 2>&1 &
echo $! > tpid
echo Start ccweb-start-2.0.0-SNAPSHOT.jar Success!
exit
