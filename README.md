<img height="150" src="https://i.imgur.com/YEPFEcx.png" alt="Libbulletjme Project logo">

[The LbjExamples Project][project] provides
documentation and example applications
for [the Libbulletjme 3-D physics library][libbulletjme].

It contains 3 subprojects:

1. docs: [Antora] documentation for Libbulletjme, including a tutorial
2. apps: sample applications referred to in the tutorial, written in [Java]
3. kotlin-apps: sample applications in [Kotlin]

The sample applications utilize [the SPORT graphics engine][sport],
which was formerly a subproject and is now a separate project at [GitHub].

Complete source code is provided under
[a 3-clause BSD license][license].


## How to build and run LbjExamples from source

### Initial build

1. Install a [Java Development Kit (JDK)][adoptium],
   version 17 or higher,
   if you don't already have one.
2. Point the `JAVA_HOME` environment variable to your JDK installation:
   (In other words, set it to the path of a directory/folder
   containing a "bin" that contains a Java executable.
   That path might look something like
   "C:\Program Files\Eclipse Adoptium\jdk-17.0.3.7-hotspot"
   or "/usr/lib/jvm/java-17-openjdk-amd64/" or
   "/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home" .)
  + using Bash or Zsh: `export JAVA_HOME="` *path to installation* `"`
  + using [Fish]: `set -g JAVA_HOME "` *path to installation* `"`
  + using Windows Command Prompt: `set JAVA_HOME="` *path to installation* `"`
  + using PowerShell: `$env:JAVA_HOME = '` *path to installation* `'`
3. Download and extract the LbjExamples source code from GitHub:
  + using [Git]:
    + `git clone https://github.com/stephengold/LbjExamples.git`
    + `cd LbjExamples`
4. Run the [Gradle] wrapper:
  + using Bash or Fish or PowerShell or Zsh: `./gradlew build`
  + using Windows Command Prompt: `.\gradlew build`

### Tutorials

The tutorial apps all have names starting with "Hello".
For instance, the first tutorial app is named "HelloLibbulletjme".

To execute "HelloLibbulletjme":
+ using Bash or Fish or PowerShell or Zsh: `./gradlew :apps:HelloLibbulletjme`
+ using Windows Command Prompt: `.\gradlew :apps:HelloLibbulletjme`

### Chooser

A [Swing]-based chooser application is provided.
However, it includes only the graphical apps and doesn't work on macOS yet.

To run the chooser:
+ using Bash or Fish or PowerShell or Zsh: `./gradlew AppChooser`
+ using Windows Command Prompt: `.\gradlew AppChooser`

### Cleanup

You can restore the project to a pristine state:
+ using Bash or Fish or PowerShell or Zsh: `./gradlew clean`
+ using Windows Command Prompt: `.\gradlew clean`


[adoptium]: https://adoptium.net/releases.html "Adoptium"
[antora]: https://antora.org/ "Antora site generator"
[fish]: https://fishshell.com/ "Fish command-line shell"
[git]: https://git-scm.com "Git version-control system"
[github]: https://en.wikipedia.org/wiki/GitHub "GitHub"
[gradle]: https://gradle.org "Gradle build tool"
[java]: https://en.wikipedia.org/wiki/Java_(programming_language) "Java programming language"
[kotlin]: https://en.wikipedia.org/wiki/Kotlin_(programming_language) "Kotlin programming language"
[libbulletjme]: https://stephengold.github.io/Libbulletjme/lbj-en/English/overview.html "Libbulletjme Project"
[license]: https://github.com/stephengold/LbjExamples/blob/master/LICENSE "LbjExamples license"
[project]: https://github.com/stephengold/LbjExamples "LbjExamples Project"
[sport]: https://github.com/stephengold/sport "SPORT graphics engine"
[swing]: https://en.wikipedia.org/wiki/Swing_(Java) "Swing toolkit"
