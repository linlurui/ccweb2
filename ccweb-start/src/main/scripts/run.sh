#!/bin/sh
nohup java -Xms255m -Xmx255m -Xss255k -Duser.timezone=GMT+08 -jar ccweb-start-2.0.0-SNAPSHOT >ccweb.log 2>&1 &
echo $! > tpid
echo Start ccweb-start-2.0.0-SNAPSHOT.jar Success!
exit
