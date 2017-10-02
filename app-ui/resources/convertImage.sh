#!/usr/bin/env bash
convert $1 -resize 1024x1024 icon.png

## ANDROID ##
v=android/icon/;
convert $1 -resize 57x57 $v/drawable-hdpi-icon.png
convert $1 -resize 36x36 $v/drawable-ldpi-icon.png
convert $1 -resize 48x48 $v/drawable-mdpi-icon.png
convert $1 -resize 96x96 $v/drawable-xhdpi-icon.png
convert $1 -resize 144x144 $v/drawable-xxhdpi-icon.png
convert $1 -resize 192x192 $v/drawable-xxxhdpi-icon.png

## IOS ##
v=ios/icon/;
convert $1 -resize 57x57 $v/icon.png
convert $1 -resize 114x114 $v/icon@2x.png
convert $1 -resize 40x40 $v/icon-40.png
convert $1 -resize 80x80 $v/icon-40@2x.png
convert $1 -resize 120x120 $v/icon-40@3x.png
convert $1 -resize 50x50 $v/icon-50.png
convert $1 -resize 100x100 $v/icon-50@2x.png
convert $1 -resize 60x60 $v/icon-60.png
convert $1 -resize 120x120 $v/icon-60@2x.png
convert $1 -resize 180x180 $v/icon-60@3x.png
convert $1 -resize 72x72 $v/icon-72.png
convert $1 -resize 144x144 $v/icon-72@2x.png
convert $1 -resize 76x76 $v/icon-76.png
convert $1 -resize 152x152 $v/icon-76@2x.png
convert $1 -resize 167x167 $v/icon-83.5@2x.png
convert $1 -resize 29x29 $v/icon-small.png
convert $1 -resize 58x58 $v/icon-small@2x.png
convert $1 -resize 87x87 $v/icon-small@3x.png

## WEB ##
v=../src/assets/icon/;
convert $1 -resize 64x64 $v/favicon.ico

