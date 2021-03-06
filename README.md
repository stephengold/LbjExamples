<img height="150" src="https://i.imgur.com/YEPFEcx.png" alt="Libbulletjme Project logo">

[The LbjExamples Project][project] provides
documentation and example applications
for the [Libbulletjme 3-D physics library][libbulletjme].

It contains 3 sub-projects:

1. apps: demos, tutorial examples, and non-automated test software
2. common: the SPORT graphics engine
3. docs: documentation for Libbulletjme, including the tutorial

Complete source code (in [Java]) is provided under
[a BSD 3-Clause license][license].


## How to build and run LbjExamples

### Initial build

1. Install a [Java Development Kit (JDK)][adoptium],
   if you don't already have one.
2. Point the `JAVA_HOME` environment variable to your JDK installation:
   (The path might be something like "C:\Program Files\Java\jre1.8.0_301"
   or "/usr/lib/jvm/java-8-openjdk-amd64/" or
   "/Library/Java/JavaVirtualMachines/liberica-jdk-17-full.jdk/Contents/Home" .)
  + using Bash or Zsh: `export JAVA_HOME="` *path to installation* `"`
  + using Windows Command Prompt: `set JAVA_HOME="` *path to installation* `"`
  + using PowerShell: `$env:JAVA_HOME = '` *path to installation* `'`
3. Download and extract the LbjExamples source code from GitHub:
  + using Git:
    + `git clone https://github.com/stephengold/LbjExamples.git`
    + `cd Heart`
4. Run the [Gradle] wrapper:
  + using Bash or PowerShell or Zsh: `./gradlew build`
  + using Windows Command Prompt: `.\gradlew build`

### Tutorials

The tutorial apps all have names starting with "Hello".
For instance, the first tutorial app is named "HelloLibbulletjme".

To execute "HelloLibbulletjme":
+ using Bash or PowerShell or Zsh: `./gradlew HelloLibbulletjme`
+ using Windows Command Prompt: `.\gradlew HelloLibbulletjme`

### Demos

Six demo applications are included:
+ ConveyorDemo
+ NewtonsCradle
+ Pachinko
+ TestGearJoint
+ ThousandCubes
+ Windlass

Documentation for the demo apps is at
https://stephengold.github.io/Libbulletjme/lbj-en/demos.html

### Chooser

A Swing-based chooser application is included.
However, it includes only the graphical apps and doesn't work yet on macOS.

To run the chooser:
+ using Bash or PowerShell or Zsh: `./gradlew AppChooser`
+ using Windows Command Prompt: `.\gradlew AppChooser`

### Cleanup

You can restore the project to a pristine state:
+ using Bash or PowerShell or Zsh: `./gradlew clean`
+ using Windows Command Prompt: `.\gradlew clean`


## About SPORT

SPORT is a Simple Phyisics-ORienTed graphics engine written in Java 1.8.
In addition to Libbulletjme, it uses [LWJGL], [GLFW], [OpenGL], and [JOML].
It has been tested on Windows, Linux, and macOS.


[adoptium]: https://adoptium.net/releases.html "Adoptium Project"
[gradle]: https://gradle.org "Gradle Project"
[glfw]: https://www.glfw.org "GLFW Library"
[java]: https://en.wikipedia.org/wiki/Java_(programming_language) "Java programming language"
[joml]: https://joml-ci.github.io/JOML "Java OpenGL Math Library"
[libbulletjme]: https://stephengold.github.io/Libbulletjme/lbj-en/overview.html "Libbulletjme Project"
[license]: https://github.com/stephengold/LbjExamples/blob/master/LICENSE "LbjExamples license"
[lwjgl]: https://www.lwjgl.org "Lightweight Java Game Library"
[opengl]: https://www.khronos.org/opengl "OpenGL API"
[project]: https://github.com/stephengold/LbjExamples "LbjExamples Project"
