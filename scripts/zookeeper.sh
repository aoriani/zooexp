#!/bin/bash

pushd $(dirname "$0")

# redirect commands to zookeeper  
../libs/zookeeper-3.3.1/bin/zkServer.sh "$@"

popd
