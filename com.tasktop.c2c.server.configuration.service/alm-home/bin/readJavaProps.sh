#!/bin/sh

# Reads a java properties file into shell vars. Replace . with _

TEMPFILE=`mktemp java-prop-XXXXXX`
cat $1 | awk 'BEGIN { FS="="; } \
/^\#/ { print; } \
!/^\#/ { if (NF == 2) { n = $1; gsub(/[^A-Za-z0-9_]/,"_",n); print n "=\"" $2 "\""; } else { print; } }' \
 >$TEMPFILE
source $TEMPFILE
rm $TEMPFILE