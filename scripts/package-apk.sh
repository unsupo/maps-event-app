#!/usr/bin/env bash
dir=`pwd`;
cd ../app-ui
app=/Users/jarndt/code_projects/kevinapp/app-ui/platforms/android/build/outputs/apk/android-release-unsigned.apk;
apk=ItsUpToYou.apk
rm -f $apk;
ionic cordova build android --release --minifycss --minifyjs;
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore howyoumove.keystore $app howyoumove -storepass password;
~/Library/Android/sdk/build-tools/26.0.1/zipalign -v 4 $app $apk;
