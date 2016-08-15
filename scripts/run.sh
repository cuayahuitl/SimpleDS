#!/bin/bash

#################################################################################### 
# This script runs the SimpleDS system for training dialogue agents 
# <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
####################################################################################

stty cols 120

# Parent/Root directory relative to this script.
# Allows this to be called from any working directoy and still function.
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/..
pushd $DIR > /dev/null

if [ "$1" = "train" ] ; then
   xterm -geometry 120x50 -T "SimpleDS.Server" -hold -e "ant SimpleDS" &
   sleep 2
   pushd $DIR/web/main/ > /dev/null
   xterm -geometry 80x20 -T "SimpleDS.Client" -hold -e "node runclient.js train"
   popd > /dev/null

elif [ "$1" = "test" ] ; then 
   xterm -geometry 120x50 -T "SimpleDS.Client" -hold -e "ant SimpleDS" &
   sleep 2
   pushd $DIR/web/main/ > /dev/null
   xterm -geometry 80x20 -T "SimpleDS.Client" -hold -e "node runclient.js test"
   popd > /dev/null

else 
   echo "usage: run.sh (train | test)" 
fi

popd
