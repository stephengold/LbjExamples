#!/bin/bash

set -e

GitDir=~/NetBeansProjects
#GitDir="/c/Users/sgold/My Documents/NetBeansProjects"

S1="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/lbjexamples/apps"
D1="$GitDir/Minie/TutorialApps/src/main/java/jme3utilities/tutorial"

S2="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/lbjexamples"
D2="$GitDir/sport/apps/src/main/java/com/github/stephengold/sport/demo"

S3="$GitDir/LbjExamples/docs/en/modules/ROOT"
D3="$GitDir/Minie/MinieLibrary/src/site/antora/tutorials/modules/minie-library-tutorials"

S4="$GitDir/LbjExamples/docs/en/modules/ROOT"
D4="$GitDir/Minie/src/site/antora/minie-project/modules/ROOT"

S5="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/lbjexamples"
D5="$GitDir/V-Sport/apps/src/main/java/com/github/stephengold/vsport/demo"

S6="$GitDir/LbjExamples/apps/src/main/java/com/github/stephengold/lbjexamples/apps"
D6="$GitDir/V-Sport/apps/src/main/java/com/github/stephengold/vsport/tutorial"

S7="$GitDir/LbjExamples/apps/src/main/java"
D7="$GitDir/Minie/MinieExamples/src/main/java"

Meld="/usr/bin/meld"
#Meld="/c/Program Files/Meld/meld"

"$Meld" --diff "$S1" "$D1" --diff "$S2" "$D2" --diff "$S3" "$D3" --diff "$S4" "$D4" \
        --diff "$S5" "$D5" --diff "$S6" "$D6" --diff "$S7" "$D7"
