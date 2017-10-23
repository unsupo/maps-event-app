#!/usr/bin/env bash
v=`pwd`;
st=$v/../app-server/src/main/resources/static;
ap=$v/../app-ui;

rm -rf $st 2>/dev/null;
mkdir -p $st;
cd $ap;
npm install;
ionic cordova build browser --release --minifycss --minifyjs #--optimizejs --release --prod #prod won't work with ng2-stomp
cp -R www/* $st/
cd $v;