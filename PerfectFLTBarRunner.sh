#!/bin/bash

bugDataPath=$1
bugID=$2
defects4jHome=$3
isTestFixPatterns=$4
mode=$5

java -Xmx8g -Xss8m -cp "./target/dependency/*" edu.lu.uni.serval.tbar.main.MainPerfectFL $bugDataPath $bugID $defects4jHome $isTestFixPatterns $mode