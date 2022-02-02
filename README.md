# Acorus Project

[The Acorus Project][acorus] provides a user-interface library for
[the jMonkeyEngine (JME) game engine][jme].

It contains 2 sub-projects:

 1. AcorusLibrary: the Acorus runtime library (in Java)
 2. AcorusExamples: demos, examples, and non-automated test software (in Java)

Complete source code is provided under
[a 3-clause BSD license][license].


<a name="toc"></a>

## Contents of this document

+ [Important features](#features)
+ [How to build Acorus from source](#build)
+ [Downloads](#downloads)
+ [Conventions](#conventions)
+ [History](#history)
+ [Acknowledgments](#acks)


<a name="features"></a>

## Important features

Acorus is a user-interface library for JMonkeyEngine desktop applications
that don't require graphical widgets such as checkboxes, menus, and dialogs.

Acorus provides simple mechanisms to:
+ bind keyboard keys, mouse buttons, and joystick buttons (hotkeys)
  to named actions
+ determine which actions are active (signals)
+ detect input combinations such as "Ctrl+C" (combos)
+ handle input differently in different contexts (input modes)
+ display on-screen UI help for an input mode,
  localized for the system's keyboard layout (help nodes)

Input modes can be configured dynamically and/or loaded from files.

Acorus provides an `Application` implementation tailored for demos and
a default input mode to emulate a `SimpleApplication`.
It also provides simplified management of asset locators and display settings.


<a name="build"></a>

## How to build Acorus from source

1. Install a [Java Development Kit (JDK)][openJDK],
   if you don't already have one.
2. Point the `JAVA_HOME` environment variable to your JDK installation:
  + using Bash: `export JAVA_HOME="` *path to installation* `"`
  + using Windows Command Prompt: `set JAVA_HOME="` *path to installation* `"`
  + using PowerShell: `$env:JAVA_HOME = '` *path to installation* `'`
3. Download and extract the Acorus source code from GitHub:
  + using Git:
    + `git clone https://github.com/stephengold/Acorus.git`
    + `cd Acorus`
    + `git checkout -b latest 0.9.7`
  + using a web browser:
    + browse to [the latest release][latest]
    + follow the "Source code (zip)" link
    + save the ZIP file
    + extract the contents of the saved ZIP file
    + `cd` to the extracted directory/folder
4. Run the [Gradle] wrapper:
  + using Bash or PowerShell: `./gradlew build`
  + using Windows Command Prompt: `.\gradlew build`

After a successful build,
Maven artifacts will be found in `AcorusLibrary/build/libs`.

You can install the artifacts to your local Maven repository:
 + using Bash or PowerShell:  `./gradlew install`
 + using Windows Command Prompt:  `.\gradlew install`

You can restore the project to a pristine state:
 + using Bash or PowerShell: `./gradlew clean`
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

I am grateful to [GitHub], [Sonatype], and [JFrog],
for providing free hosting for this project
and many other open-source projects.

I'm also grateful to my dear Holly, for keeping me sane.

If I've misattributed anything or left anyone out, please let me know, so I can
correct the situation: sgold@sonic.net

[Jump to table of contents](#toc)


[acorus]: https://github.com/stephengold/Acorus "Acorus Project"
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
[meld]: https://meldmerge.org/ "Meld Tool"
[mint]: https://linuxmint.com "Linux Mint Project"
[netbeans]: https://netbeans.org "NetBeans Project"
[openJDK]: https://openjdk.java.net "OpenJDK Project"
[sonatype]: https://www.sonatype.com "Sonatype"
[utilities]: https://github.com/stephengold/jme3-utilities "Jme3-utilities Project"
[winmerge]: https://winmerge.org "WinMerge Project"