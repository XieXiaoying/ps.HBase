#!/bin/sh
# check param
if [ ! $# -ge 1 ]; then
echo "error param"
return 1
fi

SQL_FILE="./Create.sql"
CONNECT_MYSQL="mysql -h localhost -u root -proot"
TMP_SQL="./tmp.sql"


for arg in $*
do
echo "drop database if exists $arg;create database $arg; use $arg;" > $TMP_SQL
cat $SQL_FILE >> $TMP_SQL
$CONNECT_MYSQL -e "source $TMP_SQL"
done


rm -f $TMP_SQL