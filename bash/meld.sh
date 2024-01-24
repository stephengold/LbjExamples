#!/bin/bash

set -e

GitDir=~/NetBeansProjects
#GitDir="/c/Users/sgold/My Documents/NetBeansProjects"

S1="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/lbjexamples/apps"
D1="$GitDir/Minie/TutorialApps/src/main/java/jme3utilities/tutorial"

S2="$GitDir/LbjExamples/common/src/main/java/com/github/stephengold/sport"
D2="$GitDir/V-Sport/lib/src/main/java/com/github/stephengold/vsport"

S3="$GitDir/LbjExamples/"
D3="$GitDir/V-Sport/"

S4="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/sport/test"
D4="$GitDir/V-Sport/apps/src/main/java/com/github/stephengold/vsport/test"

S5="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/lbjexamples"
D5="$GitDir/V-Sport/apps/src/main/java/com/github/stephengold/vsport/demo"

S6="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/lbjexamples/apps"
D6="$GitDir/V-Sport/apps/src/main/java/com/github/stephengold/vsport/tutorial"

S7="$GitDir/LbjExamples/apps/src/main/java"
D7="$GitDir/Minie/MinieExamples/src/main/java"

Meld="/usr/bin/meld"
#Meld="/c/Program Files/Meld/meld"

"$Meld" --diff "$S1" "$D1" --diff "$S2" "$D2" --diff "$S3" "$D3" --diff "$S4" "$D4" --diff "$S5" "$D5" --diff "$S6" "$D6" --diff "$S7" "$D7"
