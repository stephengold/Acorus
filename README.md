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
+ a display-setting editor (`DsEditInputMode` and `DsEditOverlay`)
+ a class to simplify the management of asset locators (for asset editors)

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
        implementation 'com.github.stephengold:Acorus:0.9.14'
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
      <version>0.9.14</version>
    </dependency>

### Ant-built projects

For projects built using [Ant], download the Acorus and [Heart]
libraries from GitHub:

+ https://github.com/stephengold/Acorus/releases/tag/latest
+ https://github.com/stephengold/Heart/releases/tag/7.5.0

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
  + Select the "Heart-7.5.0.jar" file.
  + Click on the "Open" button.
6. (optional) Add jars for javadoc and sources:
  + Click on the "Edit" button.
  + Click on the "Browse..." button to the right of "Javadoc:"
  + Select the "Heart-7.5.0-javadoc.jar" file.
  + Click on the "Open" button.
  + Click on the "Browse..." button to the right of "Sources:"
  + Select the "Heart-7.5.0-sources.jar" file.
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
    + `git checkout -b latest 0.9.14`
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

### HelloAcorus

Every Acorus application extends the `ActionApplication` class
or one of its subclasses, usually `AcorusDemo`.

`HelloAcorus` is a very simple example of an `AcorusDemo`:
one without a `FlyByCamera`, a `DebugKeysAppState`,
a `ScreenshotAppState`, or a `StatsAppState`.

Note that the initialization method is named `acorusInit`, not `simpleInitApp`!

A help node is displayed in the upper right of the viewport.
It describes the available user actions.
In the example there is just one action, named "SIMPLEAPP_Exit",
which is bound to the Esc key.

Pressing the Esc key triggers the "SIMPLEAPP_Exit" action.
Code to handle that action is built into `ActionApplication`.
It causes the application to terminate.

Because action names are displayed in the help node,
they should ideally be short and descriptive.
Since "SIMPLEAPP_Exit" is part of the JMonkeyEngine legacy,
the `buildNode()` utility method that constructs the help node
gives it special treatment.
That's why "exit" is displayed in place of the actual name.

### TestHotkeys

The Esc key in `HelloAcorus` is an example of a hotkey.

Hotkeys include not only keyboard keys
but also mouse buttons and joystick buttons.
Each `Hotkey` is identified in 3 ways:
 + by its "universal code" (integer value)
 + by its US name (String value), and
 + by its localized name (also a String).

On systems with "US" QWERTY keyboards,
the US name and the localized name are identical.

The `TestHotKeys` example displays a continuously-updated list active hotkeys,
making it a convenient tool for identifying hotkeys.

### HelloBind

`HelloBind` binds 12 hotkeys to 4 custom actions.
Those actions cause squares to appear on (or disappear from) the display.

Acorus groups action bindings into input modes.
In this example, the 12 custom bindings are added
to an automatically created mode called the default input mode.

An input mode's bindings don't become effective (mapped)
until the mode is activated (enabled).
Acorus activates the default mode during initialization,
just after invoking the `moreDefaultBindings()` callback.
That makes `moreDefaultBindings()`
the ideal place to customize the default mode.

An action handler is simply a method
with the `onAction(String, boolean, float)` signature.
This signature is defined in JMonkeyEngine's `ActionListener` interface.
All Acorus applications and input modes implement this interface.

When a bound hotkey is pressed, Acorus invokes the input mode's action handler.
In the example, the input mode doesn't handle the action,
so it falls through to the application's handler.

NOTE:  When multiple hotkeys are bound to a single action (as here),
the help node separates the alternatives with slashes.

NOTE:  To conserve screen area in the help node,
the `buildNode()` method often abbreviates hotkey names.
For example, the `HelloBind` help node indicates "num4",
which is an abbreviated form of "numpad 4",
which is the US/local name for the "4" key on the numeric keypad.

### HelloSignals

Just as hotkeys can be bound to actions, they can also be bound to signals.

Signals keep track of which hotkeys are active.
They are used to monitor modal keys (like Shift, Ctrl, and Alt)
and sometimes to control motion.

`HelloSignals` binds 12 hotkeys to 4 custom actions.
The signals cause squares to appear on (or disappear from) the display.

NOTE:  When multiple hotkeys are bound to the same signal (as here),
the signal remains active as long as any of the hotkeys are active.

### HelloCombo

A `Combo` represents a combination of hotkeys.
More precisely, it consists of a hotkey
plus a set of positive and/or negative signals.

The `HelloCombo` example binds 2 signals and 6 combos.

Help nodes indicate combo bindings alongside simple hotkey bindings:
+ "shift+y" mean pressing the "y" key while the "shift" signal is active
+ "noshift+y" means pressing the "y" key while "shift" isn't active

### HelloSimpleApplication

This example extends the `HelloAcorus` example
with a `FlyByCamera`, a `StatsAppState`, a `DebugKeysAppState`,
and a `ScreenshotAppState`.

When the default input mode detects a `StatsAppState`,
it binds the F5 key to the action
that toggles visibility of the render-statistics display.

When the default input mode detects a `DebugKeysAppState`,
it bind 2 hotkeys (C and M on "US" QWERTY keyboards)
to actions that print the camera position and memory statistics to the console.

When the default input mode detects a `FlyByCamera`,
it binds 6 hotkeys (W/A/S/D/Q/Z on "US" QWERTY keyboards)
to signals that control camera motion.

Users accustomed to `SimpleApplication` will expect these bindings.

### HelloToggleHelp

Since detailed help obscures a portion of the viewport,
you'll often want to display it only when the user explicitly requests it.

This example adds an action to toggle the help node between 2 versions:
detailed and minimal.
The minimal version merely indicates how to display the detailed one.

### HelloSandbox

Acorus allows you to designate a sandbox:
a directory to which Acorus will write files.
This directory is traditionally named "Written Assets".

If there's a designated sandbox,
`ActionApplication` attaches a `ScreenshotAppState`
and binds a hotkey (either PrtSc or ScrLk or SysRq)
to an action that captures a screenshot and writes it to the sandbox.

### TestAcorusDemo

`TestAcorusDemo` demonstrates a few more features of `AcorusDemo`:

+ add world axes to a scene
+ pause animations without disabling camera motion
+ scale a C-G model to a specified height
+ position a C-G model so that it rests on the X-Z plane
  with its center on the Y axis
+ select among string values, as in a menu
+ store materials in a "library" and retrieve them by name

### TestToggleFly

This example extends `TestSimpleApplication`
with an action to toggle the state of the `FlyByCamera`.

Notice how the hotkey bindings change
when camera controller is enabled and disabled.
Each change is reflected in the help node.

### HelloCoas

`FlyByCamera` is a powerful camera control,
but it's not convenient for studying a 3-D object from all sides.

This example adds a `CameraOrbitAppState` to `TestSimpleApplication`.
The left- and right-arrow keys
cause the camera to orbit around the world's Y axis.

`CameraOrbitAppState` is entirely controlled by 2 signals,
and it doesn't care which hotkeys are bound to those signals.
This makes it very easy to customize which hotkeys do what.

### TestTwoModes

In Acorus, an application can attach multiple input modes,
but it can only enable one at a time.

This example illustrates an `AcorusDemo` with 2 input modes:
The default mode (initially active) enables `FlyByCamera` for camera movement.
The other mode (named "edit") allows you to type a line of text.

### TestCursors

To remind the user which input mode is active,
you can give each mode its own cursor style.

This example demonstrates 4 input modes, each with its own cursor style.

### TestDsEdit

This example demonstrates how to use the built-in `DsEditOverlay` appstate
to edit display settings from within an application.

### TestHeadless

Acorus offers some features that aren't directly related to user interface.
Occasionally, an application that runs in a headless context
will want to utilize these features.

`TestHeadless` is a simple example of a headless `ActionApplication`.

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