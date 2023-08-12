#!/bin/bash

set -e

GitDir=~/NetBeansProjects

S1="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/lbjexamples/apps"
D1="$GitDir/Minie/TutorialApps/src/main/java/jme3utilities/tutorial"

S2="$GitDir/LbjExamples/common/src/main/java/com/github/stephengold/sport"
D2="$GitDir/V-Sport/lib/src/main/java/com/github/stephengold/vsport"

S3="$GitDir/LbjExamples/"
D3="$GitDir/V-Sport/"

S4="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/sport/test"
D4="$GitDir/V-Sport/apps/src/main/java/com/github/stephengold/vsport/test"

/usr/bin/meld --diff "$S1" "$D1" --diff "$S2" "$D2" --diff "$S3" "$D3" --diff "$S4" "$D4"
