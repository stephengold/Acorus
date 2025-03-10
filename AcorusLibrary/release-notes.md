# release log for the Acorus library and related examples

## Version 2.0.2 released on 12 January 2025

+ Overrode the initial renderer on Linux and macOS.

## Version 2.0.1 released on 11 January 2025

+ Worked around a blanking-after-restart issue on LWJGLv3/Linux.
+ Based on jMonkeyEngine v3.7.0-stable and version 9.2.0 of the Heart Library.

## Version 2.0.0 released on 13 February 2024

+ Moved `DsUtils` to the Heart Library. (API change)
+ Based on version 9.0.0 of the Heart Library.

## Version 1.1.1 released on 27 December 2023

+ Changed `DisplaySettings` so it doesn't require an `ActionApplication`.
+ Based on version 8.8.0 of the Heart Library.

## Version 1.1.0 released on 27 September 2023

+ Bugfix:  `Locators.configurationRegisterDefault()` fails if no sandbox
  has been designated
+ Removed the LWJGL v2 limitations from the `DisplaySettings` class, since the
  issues they mitigated were solved in jMonkeyEngine v3.6.1-stable .
+ Added the `hasSandbox()` method to the `ActionApplication` class.
+ Added API names for OpenGL v3.0 and v3.1 to the `DisplaySettings` class.
+ Based on jMonkeyEngine v3.6.1-stable and version 8.7.0 of the Heart Library.
+ Changes to the examples:
  + Run more of the apps in resizable windows.
  + On macOS, launch apps with the "-XstartOnFirstThread" Java option.
  + In `AppChooser`, indicate which apps have saved settings.
  + In `AppChooser`, update the overlay for every frame.

## Version 1.0.0 released on 22 March 2023

+ Bugfix:  setters throw `NullPointerException` if invoked on an
  uninitialized `Overlay`
+ Bugfix:  `Overlay` setters accept out-of-range indices
+ Added options to center or right-align content to the `Overlay` class.
+ Publicized the `hhmmss()` method in the `ActionApplication` class.
+ Based on jMonkeyEngine v3.6.0-stable and version 8.3.2 of the Heart Library.

## Version 0.9.18+for36 released on 2 March 2023

+ API changes:
  + Deleted the `framebufferHeight()`, `framebufferWidth()`,
    `getSystemListener()`, `windowXPosition()`, and `windowYPosition()` methods
     from the `DsUtils` class.
  + Renamed the `parseDisplaySize()` method in the `DsUtils` class.
+ Bugfix:  `HelloRecorder` doesn't always shut down cleanly
+ Added the `recordingQuality()` and `setRecordingQuality()` methods
  to the `ActionApplication` class.
+ If an `ActionApplication` lacks a sandbox, write screenshots and video
  to the working directory instead.
+ Based on jMonkeyEngine v3.6.0-beta3 and version 8.3.1+for36
  of the Heart Library.

## Version 0.9.17 released on 7 November 2022

+ API changes:
  + Based the `Overlay` class on `BaseAppState` instead of `SimpleAppState`.
  + Renamed the `DisplaySettings.graphicsAPI()` method.
+ Added the `bindSignal(String, String)` method to the `InputMode` class.
+ Added a "toggle recorder" action to the `ActionApplication` class.
+ Added the `HelloRecorder` tutorial app.
+ Based on version 8.2.0 of the Heart Library.

## Version 0.9.16 released on 23 June 2022

+ Bugfix:  null `colorSpace` in `HelpBuilder`
+ Based on version 8.0.0 of the Heart Library.
+ Added the "checkstyle" plugin to the build.

## Version 0.9.15 released on 29 April 2022

+ API changes:
  + privatized 2 `updateHelp()` methods in `AcorusDemo`
  + moved `updateFramebufferSize()` from `DsUtils` to `AcorusDemo`
  + added `ColorSpace` args to `HelpBuilder` methods
+ Added the `onColorSpaceChange()` callback
  to the `AcorusDemo` and `Overlay` classes.
+ Added methods to the `Overlay` class:
  + `countLines()`
  + `getLocationPolicy()`
  + `height()`
  + `setColor()`
  + `setLocationPolicy()`
  + `setText()`
  + `setXMargin()`
  + `setYMargin()`
  + `width()`
  + `xMargin()`
  + `yMargin()`
+ Added accessors to the `HelpBuilder` class:
  + `copyBackgrounColor()`
  + `padding()`
  + `separation()`
  + `setPadding()`
  + `setSeparation()`
+ Implemented `setEnabled()` and `setText()` for an uninitialized `Overlay`.
+ Changed the examples to require LWJGL v3.
+ Added the `AppChooser` application.
+ Based on jMonkeyEngine v3.5.2-stable and version 7.6.0 of the Heart Library.

## Version 0.9.14 released on 15 April 2022

+ API changes:
  + renamed the `actionInitializeApplication()` method to `acorusInit()`
  + renamed the `AbstractDemo` class to `AcorusDemo`
  + renamed the `ActionAppState` class to `AcorusAppState`
  + renamed the `inputModeChange()` method to `onInputModeChange()`
  + renamed the `resize()` method to `onViewPortResize()`
  + replaced the `HelpUtils` class with `HelpBuilder`
+ Bugfix:  `TestCursors` fails to compile in Java 8
+ Changed the action string (in `DsEditInputMode`) that closes the overlay.
+ In the examples:
  + stopped using `setRenderer()` to automatically override the graphics API
  + renamed the `TestAbstractDemo` app to `TestAcorusDemo`
+ Added the `activateInputMode()` method to the `ActionApplication` class.
+ Added 6 getters and 5 setters to the `Overlay` class.
+ Changed `DisplaySettings` to recognize OpenGL compatibility profile.

## Version 0.9.13 released on 9 April 2022

+ Bugfix:  shift key not working in the `EditMode` example class
+ API changes to the `AbstractDemo` class:
  + deleted the `attachHelpNode()` method
  + renamed the `updateHelpNodes()` methods to `updateHelp()`
+ Other important changes to the `AbstractDemo` class:
  + It now generates only one help node at a time.
  + It now generates minimal help only when "toggle help" bindings exist;
    otherwise it generates detailed help.
  + The `HelpVersion` passed to `updateHelp()` is no longer a hint;
    it gets applied regardless of which version is currently attached.
  + changed the default initial help version to `Minimal`
  + added the `resize()` callback method
  + added the `setHelpMode()` method
  + added the `setHelpBackgroundColor()` method
  + added a no-argument `updateHelp()` method
  + defined an action-string constant to activate the display-settings editor
+ Added the `Overlay` class and made `DsEditOverlay` a subclass.
+ Publicized the `InputMode` constructor (for creating anonymous classes).
+ Publicized the `describe(Combo)` method in `HelpUtils`.
+ Added Tab and F2 bindings to close the overlay in `DsEditInputMode`.
+ Changed most of the example apps
  to extend `AbstractDemo` instead of `ActionApplication`.

## Version 0.9.12 released on 6 April 2022

+ Bugfix:  incorrect hotkey bindings in `DefaultInputMode`
+ API changes:
  + in the `ActionApplication` and `ActionAppState` classes,
    renamed `speed()` to `getSpeed()`
  + in the `ActionApplication` class,
    renamed `writtenAssetPath()` to `sandboxPath()`
  + in the `DisplaySettings` class:
    + replaced `DisplaySizeLimits` with `RectSizeLimits`
    + renamed `applyToDisplay()` to `applyToContext()`
    + renamed `setMaxDimensions()` to `setMaxSize()`
    + renamed `setMinDimensions()` to `setMinSize()`
+ Other potential breaking changes:
  + `ActionApplication` no longer designates the sandbox,
    so (by default) it does not add an asset locator
    nor does it attach a `ScreenshotAppState`.
    Applications that desire these features
    should invoke `designateSandbox()` prior to initialization.
+ Other important changes to the `DisplaySettings` class:
  + don't save settings during `initialize()`
  + added a return value to the `save()` method
  + allow MSAA factors > 16
  + added public methods:
    + `graphicsApi()`
    + `isCentered()`
    + `listGraphicsApis()`
    + `load()`
    + `loadDefaults()`
    + `scaleSize()`
    + `setCentered()`
    + `setGraphicsApi()`
    + `setStartLocation()`
    + `startX()`
    + `startY()`
+ Important changes to the `AbstractDemo` class:
  + overrode the `inputModeChange()` method to update help nodes
  + allowed for the possibility of no active input mode
+ Important changes to the display-settings editor:
  + made it compatible with both LWJGL v2 and v3, minimizing use of AWT
  + created distinct display-mode matching rules for v2 and v3
  + worked around JME issues, including 798 and 1793
    and undocumented limitations of `restart()`
  + added heuristics for easier transitions between fullscreen and windowed
  + added capabilities to edit more settings:
    + window centering
    + window initial position
    + graphics API
    + graphics debug and graphics trace
  + added a 2nd status line to the overlay
  + added actions to reload default settings and revert to saved settings
  + added reflection-based utility methods to the `DsUtils` class:
    + `displayMode()`
    + `framebufferHeight()`
    + `framebufferWidth()`
    + `getSystemListener()`
    + `hasLwjglVersion3()`
    + `listDisplayModes()`
    + `updateFramebufferSize()`
    + `windowXPosition()`
    + `windowYPosition()`
+ Based on version 7.5.0 of the Heart Library.
+ Renamed tutorial apps:
  + `TestBareBones` to `HelloAcorus`
  + `TestBind` to `HelloBind`
  + `TestCoas` to `HelloCoas`
  + `TestCombo` to `HelloCombo`
  + `TestFlyCam` to `HelloSimpleApplication`
  + `TestMinHelp` to `HelloToggleHelp`
  + `TestScreenshot` to `HelloSandbox`
  + `TestSignal` to `HelloSignals`
+ Added the `HelloSandbox` tutorial app.
+ Added "--deleteOnly" and "--resetOnly" command-line options to `TestDsEdit`
  in order to recover from bad saved settings.

## Version 0.9.11 released on 27 March 2022

 + Bugfix:  asset exception in `AbstractDemo.generateMaterials()`
 + Added a test for `DebugKeysAppState` in `DefaultInputMode`.
 + Added a display-settings editing overlay, including 3 new classes
   (`DsEditOverlay`, `DsEditInputMode`, and `DsUtils`).
 + Added the `HelpVersion` class.
 + Added a new convenience constructor to the `Combo` class.
 + Added new methods to the library:
   + `AbstractDemo.updateHelpNodes()` (2 signatures)
   + `AbstractDemo.detailedHelpBounds()` callback
   + `ActionApplication.inputModeChange()` callback
   + `DisplaySettings.applicationName()` getter
   + `DisplaySettings.resize()`
 + Publicized the `toggleHelp()` and `toggleWorldAxes()`
   methods in the `AbstractDemo` class.
 + In the examples:
   + Renamed `TestCaos` to `TestCoas`.
   + Added 4 example apps
     (`TestAbstractDemo`, `TestBind`, `TestDsEdit`, and `TestSignal`).
   + Refactored the example apps to generate help nodes in `inputModeChange()`
     instead of `moreDefaultBindings()`.
   + Added command-line options to the title bars of all example apps.
 + Based on jMonkeyEngine v3.5.1-stable.

## Version 0.9.10 released on 9 March 2022

 + Added a 3rd option for displaying the settings dialog. (API change)
 + Privatized the `sizeLimits` field in the `DisplaySettings` class.
 + Bugfix:  signal names containing spaces aren't mapped properly
 + Improved the examples:
   + Added command-line arguments to adjust the logging level
     and bypass the settings dialog (for portability)
   + Added LWJGL natives for MacOSX_ARM64 (for portability)
 + Based on version 7.4.1 of the Heart Library.

## Version 0.9.9 released on 9 February 2022

 + Bugfix:  `DefaultInputMode` assumes `FlyByCamera` and `StatsAppState` exist
 + Weakened the argument of the `Locators.register(List<String>)` method.
 + Added a return value to the `InputMode.saveBindings()` method.
 + Publicized the `DefaultInputMode` class.
 + Added constructors for the `AbstractDemo`, `ActionApplication`,
   and `ActionAppState` classes.
 + Added library methods:
   + `AbstractDemo.advanceInt()`
   + `DisplaySettings.isForceDialog()`
   + `InputMode.getCursor()`
   + `InputMode.mapAll()`
   + `InputMode.unmapAll()`
   + `DefaultInputMode.updateBindings()`
 + Added English names for Hebrew keys and also for more Cyrillic keys.
 + Added the `TestBareBones` and `TestToggleFly` example applications.
 + Based on version 7.3.0 of the Heart Library.

## Version 0.9.8 released on 2 February 2022

 + Added English names for Cyrillic and Greek keys.
 + Added cases to handle headless contexts smoothly.
 + Added a `TestHeadless` application.

## Version 0.9.7 released on 29 January 2022

 + Split off the UI library from the Jme3-utilities Project, renamed it
   "Acorus", and moved it to a new GitHub repo. Added example apps from the
   "test" subproject of Jme3-utilities.
 + Re-designed `ActionAppState` to replace 2 protected fields with the
   `getActionApplication()` and `getSignals()` methods. (API change)
 + Made the `centerCgm()` and `setCgmHeight()` methods of `AbstractDemo`
   static. (API change)
 + De-publicized `Hotkey.initialize()` and added an argument. (API change)
 + Finalized 3 utility classes. (API change)
 + Removed the InputManager args from the `map()` and `unmap()` methods of the
   `Combo` and `Hotkey` classes. (API change)
 + Added joystick buttons to the `Hotkey` class.
 + Skipped unavailable key codes when using LWJGL v3.
 + Added a status display to the `TestHotkeys` app.

## Version 0.9.6 released on 23 January 2022

 + Added the `AbstractDemo` class, a subclass of `ActionApplication`.
 + Targeted Java v8.
 + Based on jMonkeyEngine v3.5.0-stable and version 7.2.0 of the Heart Library.

## Version 0.9.5 released on 21 August 2021

 + Renamed the alphabet hotkeys from uppercase to lowercase.
 + To better support non-US keyboards, added localization of hotkey names when
    the jme3-lwjgl3 library is accessible at runtime:
    + Added the `toStringLocal()` method to the `Combo` class.
    + Added the `findLocal()`, `findUs()`, `localName()`, and `usName()` methods
      to the Hotkey class.
    + Added the `bindLocal()` method to the `InputMode` class.
    + Renamed the `listHotkeys()` method in the `InputMode` class.
    + Removed the `find()` and `name()` methods from the `Hotkey` class.
    + Enhanced test coverage for issues related to non-US keyboards.
 + Renamed the `intialize()` method in the `Hotkey` class.
 + Based on version 7.0.0 of the Heart Library.

## Version 0.9.4 released on 31 May 2021

Based on jMonkeyEngine version 3.4.0-stable and Heart version 6.4.4.

## Version 0.9.3+for34 released on 23 April 2021

Based on jMonkeyEngine version 3.4.0-beta1 and Heart version 6.4.3+for34.

## Version 0.9.2 released on 9 February 2021

 + Published to MavenCentral instead of JCenter.
 + Based on version 6.4.2 of the Heart Library.

## Version 0.9.1 released on 23 November 2020

 + Added combo hints to help screens.
 + Allowed spaces in signal names (should still be discouraged).
 + Added the `listCombos()` method to the `InputMode` class.
 + Added `countSignals()`, `isPositive()`, and `signalName()` methods
   to the `Combo` class.
 + Clarified the `TestCombo` application.
 + Reimplemented `Signals` as a subclass of `SignalTracker`.
 + Based on version 6.2.0 of the Heart Library.

## Version 0.9.0 released on 16 August 2020

 + Redesigned the API of the `InputMode` class:
   + Renamed the `findHotkeys()`, `getActionName()`, `getConfigPath()`,
     and `getShortName()` methods.
   + Privatized the `suspend()` and `resume()` methods.
   + Protected the `activate()` and `deactivate()` methods.
   + Extended the `bind(String, int)` method to accept multiple keycodes.
 + Redesigned the API of the `Hotkey` class:
   + De-publicized the `map()` and `unmap()` methods.
   + Renamed the `getButtonCode()`, `getCode()`, `getKeyCode()`, and `getName()`
     methods.
 + Other changes to the library API:
   + Deleted the `exists()` method from the `Signals` class.
   + Privatized the `mapActions()` method from the `HelpUtils` class.
 + Implemented combos and combo actions, including a `Combo` class.
 + Implemented a suspend/resume stack and added `suspendAndActivate()`
   and `resumeLifo()` methods to the `InputMode` class.
 + Added the `bindSignal()` convenience methods to the `InputMode` class.
 + Added the `TestCaos`, `TestCombo`, `TestCursors`,
   and `TestTwoModes` applications.
 + Based on version 6.0.0 of the Heart Library.

## Version 0.8.3 released on 27 April 2020

 + Updated the `DisplaySettings` class to allow BPP, MSAA, and gamma-correction
   changes to be applied via a restart, since JME issue #801 is fixed.
 + Added a `getApplication()` method to the `DisplaySettings` class.
 + Based on version 5.3.0 of the Heart Library.

## Version 0.8.2 released on 1 April 2020

Based on version 5.2.1 of the Heart Library.

## Version 0.8.1for33 released on 4 February 2020

 + Changed the Maven groupId from "jme3utilities" to "com.github.stephengold"
 + Based on version 5.0 of the Heart Library.

## Version 0.8.0for33 released on 24 January 2020

 + Moved the `PropertiesLoader` class to the jme3-utilities-heart
   library. (API change)
 + Finalized `simpleInitApp()` in the `ActionApplication` class. (API change)
 + Bugfix:  corrected the name for `KEY_NUMPADCOMMA` in the `Hotkey` class.
 + Added names for `KEY_PRTSCR`, `KEY_DECIMAL`, and `KEY_SUBTRACT`
   to the `HotKey` class.
 + Added screenshot support to the `ActionApplication` class.
 + Added black backgrounds to all help nodes.
 + Added the `TestHotKeys` application to the tests sub-project.
 + Added a help node to the `TestFlyCam` application.
 + Added a `dumpAll()` method to the `Locators` class, for debugging.
 + Added "green.cur" cursor texture to the built-in assets.
 + Based on version 4.4 of the jme3-utilities-heart library.

## Version 0.7.10for33 released on 4 January 2020

 + Publicized the `HelpUtils` class.
 + Changed `HelpUtils` to sort displayed hints by action.
 + Based on version 4.3 of the jme3-utilities-heart library and the NEW
   version 3.3.0-beta1 of the jme3-core library.

## Version 0.7.9for33 released on 8 December 2019

 + Added the `HelpUtils` class.
 + Implemented `unbind()` by keycode in the `InputMode` class.
 + Based on version 4.2 of the jme3-utilities-heart library and
   version v3.3.0-beta1 of the jme3-core library, which was later deleted!

## Version 0.7.8for33 released on 23 September 2019

Based on version 4.0 of the jme3-utilities-heart library and
version 3.3.0-alpha5 of the jme3-core library.

## Version 0.7.7for33 released on 25 August 2019

Based on version 3.0 of the jme3-utilities-heart library.

## Version 0.7.6for33 released on 7 August 2019

Based on version 2.31 of the jme3-utilities-heart library.

## Version 0.7.4for33 released on 5 July 2019

 + Privatized the `shortName` field and publicized the
   `signalActionPrefix` field of the `InputMode` class.
 + Based on version 2.29 of the jme3-utilities-heart library and
   version 3.3.0-alpha2 of the jme3-core library.

## Version 0.7.3 released on 7 June 2019

 + Changed `ActionApplication` to feed a de-scaled `tpf` to `flyCam`.
 + Based on version 2.28.1 of the jme3-utilities-heart library.

## Version 0.7.2 released on 19 March 2019

Based on version 2.23 of the jme3-utilities-heart library.

## Version 0.7.1 released on 10 March 2019

 + Added a `CameraOrbitAppState` class based on the one in MinieExamples.
 + Based on version 2.21 of the jme3-utilities-heart library.

## Version 0.7.0 released on 13 January 2019

 + Added a `DisplaySizeLimits` class.
 + Added a `DisplaySettings` class based on the one in Maud.

## Version 0.6.8 released on 9 January 2019

 + Added an `ActionApplication.getSettings()` method.
 + Based on version 2.18 of the jme3-utilities-heart library.

## Version 0.6.7 released on 28 December 2018

 + Removed the deprecated `ActionApplication.getWrittenAssetDirPath()` method.
 + Based on version 2.17 of the jme3-utilities-heart library.

## Version 0.6.6 released on 28 November 2018

 + Fixed a bug where `FLYCAM_LOWER` mapped to the R key instead of the Z key.
 + Added an `ActionApplication.speed()` method.
 + Renamed `ActionApplication.getWrittenAssetDirPath()`
   to `writtenAssetDirPath()`.
 + Improved argument validation in the `Signals` class.
 + Based on version 2.14 of the jme3-utilities-heart library.

## Version 0.6.5 released on 23 September 2018

 + Renamed `UiVersion.getVersionShort()` to `versionShort()`. (API change)
 + Based on version 2.10 of the jme3-utilities-heart library.

## Version 0.6.4 released on 17 August 2018

Based on version 2.6 of the jme3-utilities-heart library.

## Version 0.6.3 released on 24 July 2018

Based on version 2.5 of the jme3-utilities-heart library.

## Version 0.6.2 released on 2 February 2018

Based on version 2.2 of the jme3-utilities-heart library.

## Version 0.6.1 released on 25 January 2018

Based on heart library v2.0 to make this library physics-independent.

## Version 0.6.0for32 released on 5 December 2017

 + 1st release to target JME 3.2
 + Replaced `PropertiesKey` with `UncachedKey`.
 + Simplified `InputMode` by removing the unused `stream` variable.

## Version 0.5.9 released on 13 September 2017

 + Changed semantics of the` Locators.register(List)` method.
 + Tried to avoid registering duplicate locators.
 + Added support for HttpZip and Url locators.
 + Standardized the BSD license texts.

## Version 0.5.8 released on 11 August 2017

 + Refined the API of `Locators` class.
 + Implemented save/restore for `Locators`.

## Version 0.5.7 released on 9 August 2017

 Added support for Zip locators.

## Version 0.5.6 released on 7 August 2017

+ Protected `flycamNames` from external modification.
+ Improved handling of filesystem exceptions.
+ Added several registration methods to the Locators class.

## Version 0.5.5 released on 16 July 2017

Publicized `InputMode.activate()` and `InputMode.deactivate()` so they can be
overridden.

## Version 0.5.4 released on 15 July 2017

+ Added a Locators class to manage asset locators.
+ Added getWrittenAssetDirPath() to the ActionApplication class.
+ Made ActionApplication.filePath() return an absolute pathname.

## Version 0.5.3 released on 20 May 2017

+ The library now depends on jme3-utilities-heart instead of SkyControl.
+ Added didntHandle() callback to the ActionApplication class.

## Version 0.5.2 released on 16 May 2017

Bugfix:  When disabling an InputMode, remove the listener.

## Version 0.5.1 released on 10 May 2017

Extended the Hotkey class to include mouse buttons, redesigned the API.

## Version 0.5.0 released on 9 April 2017

This was the initial baseline release.