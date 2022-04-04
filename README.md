# Acorus Project

[The Acorus Project][acorus] provides a simple user-interface library for
[the jMonkeyEngine (JME) game engine][jme].

It contains 2 sub-projects:

1. AcorusLibrary: the Acorus runtime library
2. AcorusExamples: demos, examples, and non-automated test software

Complete source code (in Java) is provided under
[a 3-clause BSD license][license].

Acorus is oriented toward keyboard-driven demo/test applications.
Examples may be found in the [Heart], [Minie], and [Wes] projects.


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
+ determine which hotkeys are active (signals)
+ detect input combinations such as "Ctrl+C" (combos)
+ use different hotkey bindings in different contexts (input modes)
+ display on-screen UI help for an input mode,
  localized for the system's keyboard layout (help nodes)

Input modes can be configured dynamically and/or loaded from files.

Help nodes make Acorus-based user interfaces (somewhat) self-documenting.

Acorus also provides:
+ 2 implementations of `Application` tailored for demos
  (`ActionApplication` and `AbstractDemo`)
+ an input mode for emulating a `SimpleApplication` (`DefaultInputMode`)
+ classes to simplify the management of:
  + asset locators (for asset editors) and
  + display settings (for settings dialogs).

[Jump to table of contents](#toc)


<a name="add"></a>

## How to add Acorus to an existing project

Acorus comes pre-built as a single library that depends on
[the Heart Library][heart], which in turn depends on
the standard "jme3-core" library from jMonkeyEngine.
Adding Acorus to an existing [jMonkeyEngine][jme] project should be
a simple matter of adding these libraries to the classpath.

For projects built using Maven or [Gradle], it is sufficient to add a
dependency on the Acorus Library.  The build tools should automatically
resolve the remaining dependencies.

### Gradle-built projects

Add to the project’s "build.gradle" file:

    repositories {
        mavenCentral()
    }
    dependencies {
        implementation 'com.github.stephengold:Acorus:0.9.11'
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
      <version>0.9.11</version>
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
    + `git checkout -b latest 0.9.11`
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

Applications have been created to illustrate key concepts of Acorus.
The following example apps are found in the AcorusExamples sub-project:

### TestBareBones

This is an example of a minimal `ActionApplication`:
one without a `StatsAppState`, a `DebugKeysAppState`, or a `FlyByCamera`.

A help node is displayed.
It indicates which hotkey is bound to each action.

There is a single `InputMode`, which binds 2 hotkeys to 2 different actions.
These bindings emulate the behavior of a `SimpleApplication`:
+ the Esc hotkey, to exit the application, and
+ a hotkey (either PrtSc or ScrLk or SysRq) to capture a screenshot to a file.

NOTE: If `ActionApplication` doesn't find a `ScreenshotAppState` during startup,
it attaches one.

### TestHotkeys

This example displays continuously-updated list active hotkeys,
making it a convenient tool for identifying hotkeys.

Hotkeys include not only keyboard keys
but also mouse buttons and joystick buttons.
Each `Hotkey` is identified in 3 ways:
 + by its "universal code" (integer value)
 + by its US name (String value), and
 + by its localized name (String value).

On systems with "US" QWERTY keyboards,
the US name and the localized name are identical.

### TestBind

This example binds 12 hotkeys to 4 custom actions in the default input mode.
The actions cause squares to appear on (or disappear from) the display.

An input mode's bindings don't become effective (mapped)
until the mode is activated (enabled).
Acorus activates the default mode during initialization,
just after invoking the `moreDefaultBindings()` callback,
so that's a good place to add bindings to the default mode.

When a bound hotkey is pressed, Acorus invokes the input mode's action handler.
In the example, the mode's handler doesn't handle the action,
so it falls through to the application's handler.

NOTE:  When multiple hotkeys are bound to the same action (as here),
the help node separates the alternatives with slashes.

### TestSignal

Just as hotkeys can be bound to actions, they can also be bound to signals.
Signals are used to keep track of which hotkeys are active.

This example binds 12 hotkeys to 4 custom signals in the default input mode.
The signals cause squares to appear on (or disappear from) the display.

NOTE:  When multiple hotkeys are bound to the same signal (as here),
the signal remains active as long as one or more of the hotkeys is active.

### TestCombo

A `Combo` consists of a hotkey plus positive and/or negative signals.

This example binds combos to actions.

Help nodes indicate combo bindings alongside ordinary hotkey bindings:
+ "shift+y" mean pressing the "y" key while the "shift" signal is active
+ "noshift+y" means pressing the "y" key while "shift" isn't active

### TestFlyCam

This example adds a `StatsAppState`, a `DebugKeysAppState`, and a `FlyByCamera`
to the `TestBareBones` example.

When `DefaultInputMode` detects a `StatsAppState`,
it binds the F5 key to toggle the state of the render statistics display.

When `DefaultInputMode` detects a `DebugKeysAppState`,
it bind 2 hotkeys (C and M on "US" QWERTY keyboards),
to print the camera position and memory statistics to the console.

When `DefaultInputMode` detects a `FlyByCamera`,
it binds 6 keys (W/A/S/D/Q/Z on "US" QWERTY keyboards) to camera motion.

Users accustomed to `SimpleApplication` tend to expect these bindings.

### TestMinHelp

Sometimes you want to display detailed help only when the user requests it.

This example adds an action to toggle the help node between 2 versions:
detailed and minimal.

### TestCoas

`FlyByCamera` is a powerful camera control,
but it's not convenient for studying a 3-D object from all sides.

This example adds a `CameraOrbitAppState` to `TestFlyCam`.
The left- and right-arrow keys
cause the camera to orbit around the center of the scene.

### TestToggleFly

This example extends `TestFlyCam`
with an action to toggle the state of the `FlyByCamera`.

Notice how the hotkey bindings change
when camera controller is enabled and disabled.

### TestTwoModes

This example demonstrates an `ActionApplication` with 2 input modes:
The initial mode enables `FlyByCamera`.
The other mode allows you to type a line of text.

### TestCursors

To help user tell which `InputMode` is active,
you can give each mode its own cursor.

This example demonstrates 4 input modes, each with its own cursor.

### TestHeadless

This is a simple example of a headless `ActionApplication`.

### TestAbstractDemo

This a simple example of an `AbstractDemo`.
`AbstractDemo` is a subclass of `ActionApplication`
that's more convenient for creating JME demos.
It provides methods to:

+ toggle between minimal/detailed help nodes
+ add world axes to a scene
+ pause animations without disabling camera motion
+ scale a C-G model to a specified height
+ position a C-G model so that it rests on the X-Z plane
  with center on the Y axis
+ select among float, int, and string values, as in a menu
+ store materials in a library and retrieve them by name

### TestDsEdit

This example demonstrates how to use the built-in `DsEditOverlay` appstate
to edit display settings from within an `AbstractDemo`.

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
[meld]: https://meldmerge.org "Meld Tool"
[minie]: https://github.com/stephengold/Minie "Minie Project"
[mint]: https://linuxmint.com "Linux Mint Project"
[netbeans]: https://netbeans.org "NetBeans Project"
[sonatype]: https://www.sonatype.com "Sonatype"
[utilities]: https://github.com/stephengold/jme3-utilities "Jme3-utilities Project"
[wes]: https://github.com/stephengold/Wes "Wes Project"
[winmerge]: https://winmerge.org "WinMerge Project"