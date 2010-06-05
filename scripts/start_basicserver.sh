#!/bin/bash

# Starts the Basic Server that can accept multiple clients
java -cp ../bin:../conf:../libs/zookeeper-3.3.1/lib/log4j-1.2.15.jar -Dlog4j.configuration=file:../conf/basicserver_log4j.properties br.unicamp.ic.zooexp.server.basic.BasicServer
