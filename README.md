# Acorus Project

[The Acorus Project][acorus] provides a user-interface library for
[the jMonkeyEngine (JME) game engine][jme].

It contains 2 sub-projects:

1. AcorusLibrary: the Acorus runtime library
2. AcorusExamples: demos, examples, and non-automated test software

Complete source code (in Java) is provided under
[a 3-clause BSD license][license].


<a name="toc"></a>

## Contents of this document

+ [Important features](#features)
+ [How to add Acorus to an existing project](#add)
+ [How to build Acorus from source](#build)
+ [Downloads](#downloads)
+ [Conventions](#conventions)
+ [An overview of the example applications](#examples)
+ [History](#history)
+ [Acknowledgments](#acks)


<a name="features"></a>

## Important features

Acorus is a user-interface library for JMonkeyEngine desktop applications
that don't require graphical widgets such as checkboxes, menus, and dialogs.

Acorus provides simple mechanisms to:
+ bind keyboard keys, mouse buttons, and joystick buttons (hotkeys)
  to names that are meaningful to the application (actions)
+ determine which actions are active (signals)
+ detect input combinations such as "Ctrl+C" (combos)
+ use different hotkey binding in different contexts (input modes)
+ display on-screen UI help for an input mode,
  localized for the system's keyboard layout (help nodes)

Input modes can be configured dynamically and/or loaded from files.

Acorus provides an `Application` implementation tailored for demos and
a default input mode to emulate a `SimpleApplication`.
It also provides simplified management of:
+ asset locators (for asset editors) and
+ display settings (for settings dialogs).

[Jump to table of contents](#toc)


<a name="add"></a>

## How to add Acorus to an existing project

Adding the Acorus Library to an existing [jMonkeyEngine][jme] project should be
a simple matter of adding it to the classpath.

Acorus comes pre-built as a single library
that depends on [the Heart Library][heart],
which in turn depends on
the standard "jme3-core" library from jMonkeyEngine.

For projects built using Maven or [Gradle], it is sufficient to specify the
dependency on the Acorus Library.  The build tools should automatically
resolve the remaining dependencies.

### Gradle-built projects

Add to the project’s "build.gradle" file:

    repositories {
        mavenCentral()
    }
    dependencies {
        implementation 'com.github.stephengold:Acorus:0.9.10'
    }

For some older versions of Gradle,
it's necessary to replace `implementation` with `compile`.

### Maven-built projects

Add to the project’s "pom.xml" file:

    <repositories>
      <repository>
        <id>mvnrepository</id>
        <url>https://repo1.maven.org/maven2/</url>
      </repository>
    </repositories>

    <dependency>
      <groupId>com.github.stephengold</groupId>
      <artifactId>Acorus</artifactId>
      <version>0.9.10</version>
    </dependency>

### Ant-built projects

For projects built using [Ant], download the Acorus and [Heart]
libraries from GitHub:

+ https://github.com/stephengold/Acorus/releases/tag/latest
+ https://github.com/stephengold/Heart/releases/tag/7.4.1

You'll want both class jars
and probably the `-sources` and `-javadoc` jars as well.

Open the project's properties in the IDE (JME 3.2 SDK or NetBeans 8.2):

1. Right-click on the project (not its assets) in the "Projects" window.
2. Select "Properties" to open the "Project Properties" dialog.
3. Under "Categories:" select "Libraries".
4. Click on the "Compile" tab.
5. Add the [Heart] class jar:
  + Click on the "Add JAR/Folder" button.
  + Navigate to the download folder.
  + Select the "Heart-7.4.1.jar" file.
  + Click on the "Open" button.
6. (optional) Add jars for javadoc and sources:
  + Click on the "Edit" button.
  + Click on the "Browse..." button to the right of "Javadoc:"
  + Select the "Heart-7.4.1-javadoc.jar" file.
  + Click on the "Open" button.
  + Click on the "Browse..." button to the right of "Sources:"
  + Select the "Heart-7.4.1-sources.jar" file.
  + Click on the "Open" button again.
  + Click on the "OK" button to close the "Edit Jar Reference" dialog.
7. Similarly, add the Acorus jar(s).
8. Click on the "OK" button to exit the "Project Properties" dialog.

[Jump to table of contents](#toc)


<a name="build"></a>

## How to build Acorus from source

1. Install a [Java Development Kit (JDK)][adoptium],
   if you don't already have one.
2. Point the `JAVA_HOME` environment variable to your JDK installation:
   (The path might be something like "C:\Program Files\Java\jre1.8.0_301"
   or "/usr/lib/jvm/java-8-openjdk-amd64/" or
   "/Library/Java/JavaVirtualMachines/liberica-jdk-17-full.jdk/Contents/Home" .)
  + using Bash or Zsh: `export JAVA_HOME="` *path to installation* `"`
  + using Windows Command Prompt: `set JAVA_HOME="` *path to installation* `"`
  + using PowerShell: `$env:JAVA_HOME = '` *path to installation* `'`
3. Download and extract the Acorus source code from GitHub:
  + using Git:
    + `git clone https://github.com/stephengold/Acorus.git`
    + `cd Acorus`
    + `git checkout -b latest 0.9.10`
  + using a web browser:
    + browse to [the latest release][latest]
    + follow the "Source code (zip)" link
    + save the ZIP file
    + extract the contents of the saved ZIP file
    + `cd` to the extracted directory/folder
4. Run the [Gradle] wrapper:
  + using Bash or PowerShell or Zsh: `./gradlew build`
  + using Windows Command Prompt: `.\gradlew build`

After a successful build,
Maven artifacts will be found in "AcorusLibrary/build/libs".

You can install the artifacts to your local Maven repository:
+ using Bash or PowerShell or Zsh: `./gradlew install`
+ using Windows Command Prompt: `.\gradlew install`

You can restore the project to a pristine state:
+ using Bash or PowerShell or Zsh: `./gradlew clean`
+ using Windows Command Prompt: `.\gradlew clean`

[Jump to table of contents](#toc)


<a name="downloads"></a>

## Downloads

Newer releases (since v0.9.7) can be downloaded from
[GitHub](https://github.com/stephengold/Acorus/releases).

Old releases (through v0.9.6) can be downloaded from
[the Jme3-utilities Project](https://github.com/stephengold/jme3-utilities/releases).

Newer Maven artifacts (since v0.9.7) are available from
[MavenCentral](https://search.maven.org/artifact/com.github.stephengold/Acorus).

Old Maven artifacts (v0.9.2 through v0.9.6) are available from
[MavenCentral](https://search.maven.org/artifact/com.github.stephengold/jme3-utilities-ui).

[Jump to table of contents](#toc)


<a name="conventions"></a>

## Conventions

Package names begin with `jme3utilities.ui`

The source code is compatible with JDK 7.
The pre-built libraries are compatible with JDK 8.

[Jump to table of contents](#toc)


<a name="examples"></a>

## An overview of the example applications

(under construction)

### TestBareBones

### TestHotkeys

### TestCombo

### TestFlyCam

### TestCaos

### TestToggleFly

### TestTwoModes

### TestCursors

### TestHeadless

[Jump to table of contents](#toc)


<a name="history"></a>

## History

The evolution of this project is chronicled in
[its release log][log].

Prior to January 2022, Acorus was a sub-project of
[the Jme3-utilities Project][utilities] named "jme3-utilities-ui".

Since January 2022, Acorus has been a separate project, hosted at
[GitHub][acorus].

[Jump to table of contents](#toc)


<a name="acks"></a>

## Acknowledgments

Like most projects, Acorus builds on the work of many who
have gone before.  I therefore acknowledge the creators of (and contributors to)
the following software:

+ the [Ant] and [Gradle] build tools
+ the [FindBugs] source-code analyzer
+ the [Firefox] and [Google Chrome][chrome] web browsers
+ the [Git] and Subversion revision-control systems
+ the [IntelliJ IDEA][idea] and [NetBeans] integrated development environments
+ the [Java] compiler, standard doclet, and virtual machine
+ [jMonkeyEngine][jme] and the jME3 Software Development Kit
+ the [Linux Mint][mint] operating system
+ LWJGL, the Lightweight Java Game Library
+ the [Markdown] document-conversion tool
+ the [Meld] visual merge tool
+ Microsoft Windows
+ the PMD source-code analyzer
+ the RealWorld Cursor Editor
+ the [WinMerge] differencing and merging tool

I am grateful to [GitHub], [Sonatype], and [JFrog]
for providing free hosting for this project
and many other open-source projects.

I'm also grateful to my dear Holly, for keeping me sane.

If I've misattributed anything or left anyone out, please let me know, so I can
correct the situation: sgold@sonic.net

[Jump to table of contents](#toc)


[acorus]: https://github.com/stephengold/Acorus "Acorus Project"
[adoptium]: https://adoptium.net/releases.html "Adoptium Project"
[ant]: https://ant.apache.org "Apache Ant Project"
[chrome]: https://www.google.com/chrome "Chrome"
[findbugs]: http://findbugs.sourceforge.net "FindBugs Project"
[firefox]: https://www.mozilla.org/en-US/firefox "Firefox"
[git]: https://git-scm.com "Git"
[github]: https://github.com "GitHub"
[gradle]: https://gradle.org "Gradle Project"
[heart]: https://github.com/stephengold/Heart "Heart Project"
[idea]: https://www.jetbrains.com/idea/ "IntelliJ IDEA"
[java]: https://java.com "Java"
[jfrog]: https://www.jfrog.com "JFrog"
[jme]: https://jmonkeyengine.org  "jMonkeyEngine Project"
[latest]: https://github.com/stephengold/Acorus/releases/tag/latest "latest release"
[license]: https://github.com/stephengold/Acorus/blob/master/LICENSE "Acorus license"
[log]: https://github.com/stephengold/Acorus/blob/master/AcorusLibrary/release-notes.md "release log"
[markdown]: https://daringfireball.net/projects/markdown "Markdown Project"
[meld]: https://meldmerge.org/ "Meld Project"
[mint]: https://linuxmint.com "Linux Mint Project"
[netbeans]: https://netbeans.org "NetBeans Project"
[sonatype]: https://www.sonatype.com "Sonatype"
[utilities]: https://github.com/stephengold/jme3-utilities "Jme3-utilities Project"
[winmerge]: https://winmerge.org "WinMerge Project"