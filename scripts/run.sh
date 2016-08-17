#!/bin/sh

#################################################################################### 
# This script runs the SimpleDS system for training dialogue agents 
# <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
####################################################################################

stty cols 120

if [ $1 = "train" ] ; then
   xterm -geometry 120x50 -T "SimpleDS.Server" -hold -e "ant SimpleDS" &
   sleep 7
   cd web/main
   xterm -geometry 80x20 -T "SimpleDS.Client" -hold -e "nodejs runclient.js train"
   #node runclient.js train
   cd ../../

elif [ $1 = "test" ] ; then 
   xterm -geometry 120x50 -T "SimpleDS.Client" -hold -e "ant SimpleDS" &
   sleep 7
   cd web/main
   xterm -geometry 80x20 -T "SimpleDS.Client" -hold -e "nodejs runclient.js test"
   #node runclient.js test
   cd ../../

else 
   echo "usage: run.sh (train | test)" 
fi 

