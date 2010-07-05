#!/bin/bash

pushd $(dirname "$0")

# Starts a passive replicated servers
java -cp ../bin:../conf:../libs/zookeeper-3.3.1/lib/log4j-1.2.15.jar:../libs/zookeeper-3.3.1/zookeeper-3.3.1.jar -Dlog4j.configuration=file:../conf/basicserver_log4j.properties br.unicamp.ic.zooexp.server.passive.ServerContext

popd
