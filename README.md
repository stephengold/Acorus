<img height="150" src="https://i.imgur.com/RN3bz0h.png" alt="Acorus screenshot">

[The Acorus Project][acorus] provides
a simple user-interface library
for [the jMonkeyEngine (JME) game engine][jme].

It contains 2 subprojects:

1. AcorusLibrary: the Acorus [JVM] runtime library
2. AcorusExamples: demos, examples, and non-automated test software

Complete source code (in [Java]) is provided under
[a 3-clause BSD license][license].

Acorus is oriented toward keyboard-driven demo/test applications.
Examples may be found in the [Heart], [Minie], [MonkeyWrench], and [Wes] projects.


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
that don't require graphical widgets
such as checkboxes, radio buttons, and sliders.

Acorus provides simple mechanisms to:
+ bind keyboard keys, mouse buttons, and joystick buttons (hotkeys)
  to names that are meaningful to the application (actions)
+ determine which hotkeys are active (signals)
+ detect input combinations such as "Ctrl+C" (combos)
+ use different hotkey bindings in different contexts (input modes)
+ display on-screen UI help for the active input mode,
  localized for the system's keyboard layout (help nodes)

Input modes can be configured dynamically and/or loaded from files.

Help nodes make Acorus-based user interfaces (somewhat) self-documenting.

The Acorus library also includes:
+ 2 implementations of the `Application` interface, tailored for demos
  (`ActionApplication` and `AcorusDemo`)
+ an input mode for emulating a `SimpleApplication` (`DefaultInputMode`)
+ a camera controller for orbiting the world's Y axis (`CameraOrbitAppState`)
+ a display-settings editor (`DsEditInputMode` and `DsEditOverlay`)
+ a class to simplify the management of asset locators (for asset editors)

[Jump to the table of contents](#toc)


<a name="add"></a>

## How to add Acorus to an existing project

[How to add Acorus to an existing project](https://stephengold.github.io/Acorus/acorus-en/English/add.html)


<a name="build"></a>

## How to build Acorus from source

[How to build Acorus from source](https://stephengold.github.io/Acorus/acorus-en/English/build.html)


<a name="downloads"></a>

## Downloads

Newer releases (since v0.9.7) can be downloaded from
[GitHub](https://github.com/stephengold/Acorus/releases).

Old releases (through v0.9.6) can be downloaded from
[the Jme3-utilities Project](https://github.com/stephengold/jme3-utilities/releases).

Newer Maven artifacts (since v0.9.7) are available from
[MavenCentral](https://central.sonatype.com/artifact/com.github.stephengold/Acorus/2.0.2/versions).

Old Maven artifacts (v0.9.2 through v0.9.6) are available from
[MavenCentral jme3-utilities-ui](https://central.sonatype.com/artifact/com.github.stephengold/jme3-utilities-ui/0.9.6/versions).

[Jump to the table of contents](#toc)


<a name="conventions"></a>

## Conventions

Package names begin with `jme3utilities.ui`

The source code and pre-built libraries are compatible with JDK 8.

[Jump to the table of contents](#toc)


<a name="examples"></a>

## An overview of the example applications

Applications have been created to illustrate key concepts of Acorus.
They are found in the AcorusExamples subproject.
Since they aren't distributed in binary form,
you must build the project from source in order to run them.

To run them using the included AppChooser:
+ using Bash or [Fish] or PowerShell or Zsh: `./gradlew --no-daemon AppChooser`
+ using Windows Command Prompt: `.\gradlew --no-daemon AppChooser`

To run a specific app:
+ using Bash or Fish or PowerShell or Zsh: `./gradlew --no-daemon ` *appName*
+ using Windows Command Prompt: `.\gradlew --no-daemon ` *appName*

### HelloAcorus

[Getting started with Acorus](https://stephengold.github.io/Acorus/acorus-en/English/hello.html)

### TestHotkeys and HelloBind

[Binding hotkeys to actions](https://stephengold.github.io/Acorus/acorus-en/English/bind.html)

### HelloSignals

[Binding hotkeys to signals](https://stephengold.github.io/Acorus/acorus-en/English/signal.html)

### HelloCombo

[Binding combos to actions](https://stephengold.github.io/Acorus/acorus-en/English/combo.html)

### HelloSimpleApplication

[Mimicking a simple application](https://stephengold.github.io/Acorus/acorus-en/English/mimic.html)

### HelloToggleHelp

[Toggling the help node](https://stephengold.github.io/Acorus/acorus-en/English/toggle.html)

### TestAcorusDemo

[Additional features of AcorusDemo](https://stephengold.github.io/Acorus/acorus-en/English/more.html)

### HelloSandbox and HelloRecorder

[The Sandbox](https://stephengold.github.io/Acorus/acorus-en/English/sandbox.html)

### TestToggleFly and HelloCoas

[Controlling 3-D cameras](https://stephengold.github.io/Acorus/acorus-en/English/camera.html)

### TestTwoModes and TestCursors

[Defining multiple input modes](https://stephengold.github.io/Acorus/acorus-en/English/modes.html)

### TestDsEdit

[Editing display settings](https://stephengold.github.io/Acorus/acorus-en/English/dsedit.html)

### TestHeadless

[Using Acorus in a headless context](https://stephengold.github.io/Acorus/acorus-en/English/headless.html)

[Jump to the table of contents](#toc)


<a name="history"></a>

## History

The evolution of this project is chronicled in
[its release log][log].

Prior to January 2022, Acorus was a subproject of
[the Jme3-utilities Project][utilities] named "jme3-utilities-ui".

Since January 2022, Acorus has been a separate project, hosted at
[GitHub][acorus].

[Jump to the table of contents](#toc)


<a name="acks"></a>

## Acknowledgments

Like most projects, Acorus builds on the work of many who
have gone before.  I therefore acknowledge
the creators of (and contributors to) the following software:

+ the [Ant] and [Gradle] build tools
+ the [Antora] static website generator
+ the [Checkstyle] tool
+ the [FindBugs] source-code analyzer
+ the [Firefox] and [Google Chrome][chrome] web browsers
+ the [Git] and Subversion revision-control systems
+ the [GitKraken] client
+ the [Gradle] build tool
+ the [IntelliJ IDEA][idea] and [NetBeans] integrated development environments
+ the [Java] compiler, standard doclet, and runtime environment
+ [jMonkeyEngine][jme] and the jME3 Software Development Kit
+ the [Linux Mint][mint] operating system
+ [LWJGL], the Lightweight Java Game Library
+ the [Markdown] document-conversion tool
+ the [Meld] visual merge tool
+ Microsoft Windows
+ the [NetBeans] integrated development environment
+ the PMD source-code analyzer
+ the RealWorld Cursor Editor
+ the [WinMerge] differencing and merging tool

I am grateful to Juan Cruz Fandino for
[his pull request](https://github.com/stephengold/Acorus/pull/1).

I am grateful to [GitHub], [Sonatype], [JFrog], and [Imgur]
for providing free hosting for this project
and many other open-source projects.

I'm also grateful to my dear Holly, for keeping me sane.

If I've misattributed anything or left anyone out, please let me know, so I can
correct the situation: sgold@sonic.net

[Jump to the table of contents](#toc)


[acorus]: https://stephengold.github.io/Acorus "Acorus Project"
[antora]: https://antora.org/ "Antora Project"
[ant]: https://ant.apache.org "Apache Ant Project"
[checkstyle]: https://checkstyle.org "Checkstyle"
[chrome]: https://www.google.com/chrome "Chrome"
[findbugs]: http://findbugs.sourceforge.net "FindBugs Project"
[firefox]: https://www.mozilla.org/en-US/firefox "Firefox"
[fish]: https://fishshell.com/ "Fish command-line shell"
[git]: https://git-scm.com "Git"
[github]: https://github.com "GitHub"
[gitkraken]: https://www.gitkraken.com "GitKraken client"
[gradle]: https://gradle.org "Gradle Project"
[heart]: https://github.com/stephengold/Heart "Heart Project"
[idea]: https://www.jetbrains.com/idea/ "IntelliJ IDEA"
[imgur]: https://imgur.com/ "Imgur"
[java]: https://en.wikipedia.org/wiki/Java_(programming_language) "Java programming language"
[jfrog]: https://www.jfrog.com "JFrog"
[jme]: https://jmonkeyengine.org "jMonkeyEngine Project"
[jvm]: https://en.wikipedia.org/wiki/Java_virtual_machine "Java virtual machine"
[latest]: https://github.com/stephengold/Acorus/releases/tag/latest "latest release"
[license]: https://github.com/stephengold/Acorus/blob/master/LICENSE "Acorus license"
[log]: https://github.com/stephengold/Acorus/blob/master/AcorusLibrary/release-notes.md "release log"
[lwjgl]: https://www.lwjgl.org "Lightweight Java Game Library"
[markdown]: https://daringfireball.net/projects/markdown "Markdown Project"
[maven]: https://maven.apache.org "Maven Project"
[meld]: https://meldmerge.org "Meld merge tool"
[minie]: https://stephengold.github.io/Minie/minie/overview.html "Minie Project"
[mint]: https://linuxmint.com "Linux Mint Project"
[monkeywrench]: https://github.com/stephengold/MonkeyWrench "MonkeyWrench Project"
[netbeans]: https://netbeans.org "NetBeans Project"
[sonatype]: https://www.sonatype.com "Sonatype"
[utilities]: https://github.com/stephengold/jme3-utilities "Jme3-utilities Project"
[wes]: https://github.com/stephengold/Wes "Wes Project"
[winmerge]: https://winmerge.org "WinMerge Project"
