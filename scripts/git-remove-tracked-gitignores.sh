#!/usr/bin/env bash
curDir=`pwd`;
rootDir="`pwd`/../";
cd $rootDir;
while IFS= read -r var; do
#    echo $var
    for i in `find . -name "$var"`; do
#        echo "  $i";
        git rm -rf --cached $i 2>/dev/null;
    done;
done < ".gitignore"
cd $curDir;