#!/bin/sh

# Note, in vegas this outputs "sed: can't read '': No such file or directory" but it still works...

HOMES_DIR=/home/code2cloud/hudson-homes/*
SED_EXPR="sed -i '' -e s_https://code2cloud\.tasktop\.com_https://qcode\.cloudfoundry\.com_g"

for home in $HOMES_DIR
do
    echo updateing protocol in $home
    $SED_EXPR $home/config.xml 
    $SED_EXPR $home/hudson.tasks.Mailer.xml 

    for job in $home/jobs/*
    do
	echo ' updating job' $job
	$SED_EXPR $job/config.xml
    done
done
