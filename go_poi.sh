#!/bin/sh

CLASS_DIR=classes
SRC_DIR=src
LIB_DIR=lib
TEST_DIR=test
RESOUCE_DIR=resource
clean()
{
   echo "clean..."
   rm -rf $CLASS_DIR
   echo "clean done"
}

build_src()
{
   clean;
   mkdir $CLASS_DIR
   echo "compiling..."
   javac -d $CLASS_DIR -classpath $CLASSPATH:$LIB_DIR/*  -Xlint:unchecked $SRC_DIR/**/*.java
   #cd $SRC_DIR
   #cp --parents ./**/*.class ../$CLASS_DIR
   # delete CLASS file
   #rm ./**/*.class
   #cd ../
   cp $SRC_DIR/log4j.properties $CLASS_DIR/.
#   cp -r $RESOURCE_DIR/ $CLASS_DIR/.
   cp -r resource/ classes/ 
   echo "compile done"
}

build_test(){
#   $CLASSPATH=$CLASSPATH:$CLASS_DIR
#   echo $CLASSPATH
   javac -d $CLASS_DIR -classpath $CLASSPATH:$CLASS_DIR -Xlint:unchecked $TEST_DIR/**/*.java

}

if [ x$1 != x ]
then
    #...有参�?
#    echo $1
    case $1 in
    build)
#clean
	build_src
	;;
    run)
	build_src
	echo "run..."		
	cd $CLASS_DIR
#	java -classpath $CLASSPATH:../$LIB_DIR/* RMI.QueueServer
#	java -classpath $CLASSPATH:../$LIB_DIR/* RMI.QueueClient
#	java -Xmx8g  -Xms8g -classpath $CLASSPATH:../$LIB_DIR/* collection.ImageTableTest /home/ps/leeying/pic
#	java -classpath $CLASSPATH:../$LIB_DIR/* collection.MultimediaTable
#	java -classpath $CLASSPATH:../$LIB_DIR/* collection.CollectionTable
#	java -classpath $CLASSPATH:../$LIB_DIR/* psensing.TrajPre
#	java -classpath $CLASSPATH:../$LIB_DIR/* Web.WebView
#	java -classpath $CLASSPATH:../$LIB_DIR/* Web.POITable
#	java -classpath $CLASSPATH:../$LIB_DIR/* Trajectory.TrajTable
#	java -classpath $CLASSPATH:../$LIB_DIR/* Trajectory.GridTable
#	java -classpath $CLASSPATH:../$LIB_DIR/* collection.CollectionPicTable
#	java -classpath $CLASSPATH:../$LIB_DIR/* Pic.CRFTable
	java -classpath $CLASSPATH:../$LIB_DIR/* Web.POITable 
#	java -classpath $CLASSPATH:../$LIB_DIR/* Path.PathTable
#	java -classpath $CLASSPATH:../$LIB_DIR/* Fusion.FusionView
#	java -classpath $CLASSPATH:../$LIB_DIR/* Web.testjava

        cd ../
        ;;
    test)
        echo "test..."
        build_src
	build_test
	cd $CLASS_DIR
#	java org.junit.runner.JUnitCore Pic.MonitorSiteTest
#	java org.junit.runner.JUnitCore Pic.WeatherTest
#	java org.junit.runner.JUnitCore Pic.CRFTest
#	java org.junit.runner.JUnitCore Incentive.UserTest
#	java  -classpath $CLASSPATH:../$LIB_DIR/* org.junit.runner.JUnitCore Incentive.IncentiveJobTest
#	java org.junit.runner.JUnitCore Collection.CollectionPicTest
#	java org.junit.runner.JUnitCore Fusion.FusionTest
#	java org.junit.runner.JUnitCore Fusion.GridTest
#	java org.junit.runner.JUnitCore Web.POITest
#	java org.junit.runner.JUnitCore Web.WebViewTest
java org.junit.runner.JUnitCore Parse.ExifParse
        cd ../
        ;;
    *)
        echo "The valid task is build/run/test"
        ;;
esac
else
    #...没有参数
    echo "Input task:build/run/test"
fi
