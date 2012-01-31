#!/bin/sh

stop() {
    echo 'doing nothiing for stop'
}

status() {
if [ -b /dev/sdb1 ]
then
    echo 'Disk already apears to be setup'
else 
    echo 'Still need to setup disk'
fi
}

start() {
if [ -b /dev/sdb1 ]
then
    echo 'File already exists, aborting configuration' >> /opt/cloudalm/logs/configure.log 
    exit 0;
fi

echo 'Mysql stop'  >> /opt/cloudalm/logs/configure.log  2>&1
/etc/init.d/mysql stop  >> /opt/cloudalm/logs/configure.log 2>&1

/opt/cloudalm/bin/addDisk.sh  >> /opt/cloudalm/logs/configure.log 2>&1

/opt/cloudalm/bin/initMysql.sh  >> /opt/cloudalm/logs/configure.log 2>&1

echo 'configure done' >> /opt/cloudalm/logs/configure.log 2>&1
date >> /opt/cloudalm/logs/configure.log

# TODO uninstall self from init.d
}

case "$1" in
 start)
        start
        ;;
 stop)
        stop
        ;;
 restart)
        stop
        start
        ;;
 status)
        status
        ;;
 *)
        echo $"Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
