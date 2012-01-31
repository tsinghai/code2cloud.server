#!/bin/sh

# Assumes disk is at /dev/sdb
# Assumes partition will end up at /dev/sdb1
# Assumes mount point of /home/cloudalm
ls /dev/sd*

if [ -b /dev/sdb ]
then
   echo "Hard drive exists"
else
    echo "Missing hard drive"
exit 0
fi

sudo fdisk /dev/sdb << EOF
n
p
1
1

w
EOF

sudo mkfs -t ext3 /dev/sdb1

sudo tune2fs -m 1 /dev/sdb1

sudo mkdir /home/cloudalm

sudo chown vcloud:vcloud /home/cloudalm

sudo chmod o+w /etc/fstab

sudo cat >> /etc/fstab << EOF
/dev/sdb1	/home/cloudalm	ext3	defaults		 0	 0
EOF

sudo chmod o-w /etc/fstab

sudo mount -a

sudo chown vcloud:vcloud /home/cloudalm
