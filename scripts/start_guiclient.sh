#!/bin/bash

# Starts the GUI Client 
java -cp ../bin:../conf:../libs/zookeeper-3.3.1/lib/log4j-1.2.15.jar -Dlog4j.configuration=file:../conf/clientgui_log4j.properties br.unicamp.ic.zooexp.client.ClientGui 
