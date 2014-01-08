#!/bin/bash
# author: Daniel M. de Oliveira

bzr up
bzr revert
mvn clean

STATUS_UNIT_TESTS=0
mvn test
STATUS_UNIT_TESTS=$?
if [ "$STATUS_UNIT_TESTS" != "0" ]
then
    echo there were errors in the unit tests
    exit 1
fi

mvn deploy -Dmaven.test.skip=true
