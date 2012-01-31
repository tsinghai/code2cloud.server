#!/bin/sh

mkdir /home/cloudalm/mysql
chown -R mysql /home/cloudalm/mysql
chgrp -R mysql /home/cloudalm/mysql
cp -Rp /opt/cloudalm/configuration/mysql-template/* /home/cloudalm/mysql

/etc/init.d/mysql start

