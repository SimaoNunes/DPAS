#!/bin/bash
while true
do
	sleep $1
	kill -INT $!
	mvn exec:java
done
