/*
 Copyright (c) 2014-2022, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.ui;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.input.CameraInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferUtils;
import java.awt.DisplayMode;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.PropertiesLoader;
import jme3utilities.Validate;

/**
 * Simple application with an action-oriented user interface.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class ActionApplication
        extends SimpleApplication
        implements ActionListener {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ActionApplication.class.getName());
    /**
     * names of flyCam actions and the signals used to simulate them
     */
    final static String[] flycamNames = {
        CameraInput.FLYCAM_BACKWARD,
        CameraInput.FLYCAM_FORWARD,
        CameraInput.FLYCAM_LOWER,
        CameraInput.FLYCAM_RISE,
        CameraInput.FLYCAM_STRAFELEFT,
        CameraInput.FLYCAM_STRAFERIGHT
    };
    /**
     * action string to generate a screenshot
     */
    final public static String asScreenShot = "ScreenShot";
    /**
     * action string to toggle video recording on/off
     */
    final public static String asToggleRecorder = "toggle recorder";
    // *************************************************************************
    // fields

    /**
     * set to true in {@link #simpleInitApp()}
     */
    private static boolean isInitialized = false;
    /**
     * directory for writing assets, or null if none has been designated
     */
    private static File sandboxDirectory = null;
    /**
     * initial input mode: set in {@link #simpleInitApp()}
     */
    private InputMode defaultInputMode = null;
    /**
     * quality level for recorded video (&ge;0, &lt;1)
     */
    private float recordingQuality = 1f;
    /**
     * track input signals
     */
    final private Signals signals = new Signals();
    // *************************************************************************
    // constructors

    /**
     * Instantiate an application with the appstates favored by
     * SimpleApplication (AudioListenerState, ConstantVerifierState,
     * DebugKeysAppState, FlyCamAppState, and StatsAppState) pre-attached.
     *
     * A DefaultInputMode and a ScreenshotAppState will be attached during
     * initialization.
     */
    protected ActionApplication() {
    }

    /**
     * Instantiate an application with the specified appstates pre-attached. A
     * DefaultInputMode and a ScreenshotAppState will be attached during
     * initialization.
     *
     * @param initialAppStates the appstates to be pre-attached (may be null,
     * unaffected)
     */
    protected ActionApplication(AppState... initialAppStates) {
        super(initialAppStates);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Invoke the user's application startup code.
     */
    abstract public void acorusInit();

    /**
     * Activate the named InputMode.
     *
     * @param shortName the short name of the desired InputMode (not null, mode
     * must be initialized)
     */
    public static void activateInputMode(String shortName) {
        InputMode desired = InputMode.findMode(shortName);
        Validate.require(desired != null, "an initialized InputMode");

        InputMode active = InputMode.getActiveMode();
        if (active != desired) {
            InputMode.suspendAndActivate(desired);
        }
    }

    /**
     * Designate a directory for writing assets, and if it doesn't exist, create
     * it. Also causes a ScreenshotAppState to be added during initialization.
     * <p>
     * Invoke this at most once, prior to initialization---for instance, in
     * {@code main()}.
     *
     * @param desiredPath the desired filesystem path, or null for "./Written
     * Assets"
     * @throws IOException if directory creation fails
     */
    public static void designateSandbox(String desiredPath) throws IOException {
        if (sandboxDirectory != null) {
            throw new IllegalStateException(
                    "Don't invoke this method more than once.");
        }
        if (isInitialized) {
            throw new IllegalStateException(
                    "too late - application is already initialized");
        }

        if (desiredPath == null) {
            sandboxDirectory = new File("Written Assets");
        } else {
            sandboxDirectory = new File(desiredPath);
        }
        String fixedPath = sandboxPath();
        String quotedPath = MyString.quote(fixedPath);

        if (!sandboxDirectory.exists()) {
            boolean success = sandboxDirectory.mkdirs();
            if (!success) {
                throw new IOException(
                        "Failed to create a directory at " + quotedPath);
            }
        }

        assert sandboxDirectory.exists();
        if (!sandboxDirectory.isDirectory()) {
            logger.log(Level.WARNING, "{0} exists, but is not a directory.",
                    quotedPath);
        } else if (!sandboxDirectory.canWrite()) {
            logger.log(Level.WARNING, "{0} exists, but is not writeable.",
                    quotedPath);
        }
    }

    /**
     * Callback invoked when an ongoing action isn't handled after running
     * through the {@link #onAction(java.lang.String, boolean, float)} methods
     * of both the input mode and the application. Meant to be overridden.
     *
     * @param actionString textual description of the action (not null)
     */
    public void didntHandle(String actionString) {
        Validate.nonNull(actionString, "action string");
        assert isInitialized;

        logger.log(Level.WARNING, "Ongoing action {0} was not handled.",
                MyString.quote(actionString));
    }

    /**
     * Convert an asset path to a canonical filesystem path for writing the
     * asset. Assumes that {@link #designateSandbox(java.lang.String)} has been
     * invoked.
     *
     * @param assetPath (not null)
     * @return the file-system path (not null, not empty)
     */
    public static String filePath(String assetPath) {
        Validate.nonNull(assetPath, "asset path");
        if (sandboxDirectory == null) {
            throw new IllegalStateException("No sandbox been designated.");
        }

        File file = new File(sandboxDirectory, assetPath);
        String result = Heart.fixedPath(file);

        assert !result.isEmpty();
        return result;
    }

    /**
     * Access the default input mode.
     *
     * @return pre-existing instance (not null)
     */
    public InputMode getDefaultInputMode() {
        if (!isInitialized) {
            throw new IllegalStateException(
                    "The application hasn't been initialized yet.");
        }
        assert defaultInputMode != null;
        return defaultInputMode;
    }

    /**
     * Access the live display settings.
     *
     * @return the pre-existing instance (not null)
     */
    public AppSettings getSettings() {
        assert settings != null;
        return settings;
    }

    /**
     * Access the signal tracker.
     *
     * @return pre-existing instance (not null)
     */
    public Signals getSignals() {
        if (!isInitialized) {
            throw new IllegalStateException(
                    "The application hasn't been initialized yet.");
        }
        assert signals != null;
        return signals;
    }

    /**
     * Callback invoked immediately after initializing the hotkey bindings of
     * the default input mode. Meant to be overridden. Can be used to add action
     * names and/or override those bindings.
     */
    public void moreDefaultBindings() {
        // do nothing
    }

    /**
     * Callback invoked when the active InputMode changes. Meant to be
     * overridden.
     *
     * @param oldMode the old mode, or null if none
     * @param newMode the new mode, or null if none
     */
    public void onInputModeChange(InputMode oldMode, InputMode newMode) {
        // do nothing
    }

    /**
     * Return the quality level for new video recordings.
     *
     * @return the level (&ge;0, &le;1)
     */
    public float recordingQuality() {
        return recordingQuality;
    }

    /**
     * Determine the filesystem path to the directory for writing assets.
     * Assumes that {@link #designateSandbox(java.lang.String)} has been
     * invoked.
     *
     * @return the canonical pathname (not null, not empty)
     */
    public static String sandboxPath() {
        if (sandboxDirectory == null) {
            throw new IllegalStateException("No sandbox has been designated.");
        }
        String path = Heart.fixedPath(sandboxDirectory);

        assert !path.isEmpty();
        return path;
    }

    /**
     * Alter the quality level for new video recordings.
     *
     * @param qualityLevel (&ge;0, &le;1, default=1)
     */
    public void setRecordingQuality(float qualityLevel) {
        Validate.fraction(qualityLevel, "quality level");
        this.recordingQuality = qualityLevel;
    }
    // *************************************************************************
    // ActionListener methods

    /**
     * Process an action that wasn't handled by the active input mode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        assert isInitialized;
        if (ongoing) {
            // Process combo actions.
            if (actionString.startsWith(InputMode.comboActionPrefix)) {
                InputMode mode = InputMode.getActiveMode();
                if (mode != null) {
                    String arg = MyString.remainder(
                            actionString, InputMode.comboActionPrefix);
                    int code = Integer.parseInt(arg);
                    mode.processCombos(code, tpf);
                    return;
                }
            }
            /*
             * Process actions whose mappings may have been deleted by
             * DefaultInputMode.initialize().
             */
            switch (actionString) {
                case asScreenShot:
                    ScreenshotAppState screenshotAppState
                            = stateManager.getState(ScreenshotAppState.class);
                    if (screenshotAppState != null) {
                        screenshotAppState.onAction(actionString, ongoing, tpf);
                        logger.log(Level.WARNING, "Captured a screenshot.");
                    }
                    break;

                case asToggleRecorder:
                    toggleRecorder();
                    break;

                case SimpleApplication.INPUT_MAPPING_EXIT:
                    stop();
                    break;

                case SimpleApplication.INPUT_MAPPING_CAMERA_POS:
                    dumpCameraPosition();
                    break;

                case SimpleApplication.INPUT_MAPPING_HIDE_STATS:
                    StatsAppState statsAppState
                            = stateManager.getState(StatsAppState.class);
                    if (statsAppState != null) {
                        statsAppState.toggleStats();
                    }
                    break;

                case SimpleApplication.INPUT_MAPPING_MEMORY:
                    BufferUtils.printCurrentDirectMemory(null);
                    break;

                default:
                    didntHandle(actionString);
            }
        }
    }
    // *************************************************************************
    // SimpleApplication methods

    /**
     * Return the effective speed of physics and animations.
     *
     * @return the speed (&gt;0, standard speed &rarr; 1)
     */
    @Override
    public float getSpeed() {
        assert speed > 0f : speed;
        return speed;
    }

    /**
     * Alter the effective speeds of physics simulation and all animations.
     *
     * @param newSpeed animation speed (&gt;0, normal speed &rarr; 1)
     */
    @Override
    public void setSpeed(float newSpeed) {
        Validate.positive(newSpeed, "new speed");
        this.speed = newSpeed;
    }

    /**
     * Initialization code common to all action-oriented applications.
     */
    @Override
    final public void simpleInitApp() {
        if (isInitialized) {
            throw new IllegalStateException(
                    "application may only be initialized once");
        }
        isInitialized = true;

        Locators.setAssetManager(assetManager);
        if (sandboxDirectory != null) {
            // Initialize asset locators to the default list.
            assetManager.unregisterLocator("/", ClasspathLocator.class);
            Locators.useDefault();
        }

        // Register a loader for Properties assets.
        assetManager.registerLoader(PropertiesLoader.class, "properties");

        // Initialize hotkeys.
        Hotkey.initialize(inputManager);

        this.defaultInputMode = stateManager.getState(DefaultInputMode.class);
        if (defaultInputMode == null) {
            // Attach and enable the standard initial input mode.
            defaultInputMode = new DefaultInputMode();
            stateManager.attach(defaultInputMode);
            defaultInputMode.setEnabled(true);
        }

        ScreenshotAppState screenshotAppState
                = stateManager.getState(ScreenshotAppState.class);
        if (screenshotAppState == null) {
            String waPath;
            if (sandboxDirectory == null) {
                // Capture screenshots to the working directory.
                String workingDirectory = System.getProperty("user.dir");
                waPath = Heart.fixPath(workingDirectory) + File.separator;
            } else { // Capture screenshots to the sandbox.
                waPath = sandboxPath() + File.separator;
            }
            screenshotAppState
                    = new ScreenshotAppState(waPath, "screenshot");
            boolean success = stateManager.attach(screenshotAppState);
            assert success;
        }

        acorusInit(); // Invoke the startup code of the subclass.
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        assert isInitialized;
        /*
         * Handle flyCam signals whose mappings may have been deleted by
         * DefaultInputMode.initialize().
         */
        if (flyCam != null && flyCam.isEnabled()) {
            float realTpf = tpf / speed;
            for (String signalName : flycamNames) {
                if (signals.test(signalName)) {
                    flyCam.onAnalog(signalName, realTpf, realTpf);
                }
            }
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Process a "SIMPLEAPP_CameraPos" action.
     */
    private void dumpCameraPosition() {
        if (cam != null) {
            Vector3f loc = cam.getLocation(); // alias
            Quaternion rot = cam.getRotation(); // alias
            System.out.println("Camera Position: ("
                    + loc.x + ", " + loc.y + ", " + loc.z + ")");
            System.out.println("Camera Rotation: " + rot);
            System.out.println("Camera Direction: " + cam.getDirection());
            System.out.println("cam.setLocation(new Vector3f("
                    + loc.x + "f, " + loc.y + "f, " + loc.z + "f));");
            System.out.println("cam.setRotation(new Quaternion("
                    + rot.getX() + "f, " + rot.getY() + "f, "
                    + rot.getZ() + "f, " + rot.getW() + "f));");
        }
    }

    /**
     * Generate a timestamp.
     *
     * @return the timestamp value
     */
    private static String hhmmss() {
        Calendar rightNow = Calendar.getInstance();
        int hours = rightNow.get(Calendar.HOUR_OF_DAY);
        int minutes = rightNow.get(Calendar.MINUTE);
        int seconds = rightNow.get(Calendar.SECOND);
        String result = String.format("%02d%02d%02d", hours, minutes, seconds);

        return result;
    }

    /**
     * Process a "toggle recorder" action.
     */
    private void toggleRecorder() {
        VideoRecorderAppState vras
                = stateManager.getState(VideoRecorderAppState.class);
        if (vras == null) {
            String hhmmss = hhmmss();
            String fileName = String.format("recording-%s.avi", hhmmss);
            String path;
            if (sandboxDirectory == null) {
                // Record video to the working directory.
                path = Heart.fixPath(fileName);
            } else { // Record video to the sandbox.
                path = ActionApplication.filePath(fileName);
            }
            File file = new File(path);

            DisplayMode mode = DsUtils.displayMode();
            int frameRate = mode.getRefreshRate();
            assert frameRate > 0 : frameRate;

            AppState recorder = new VideoRecorderAppState(
                    file, recordingQuality, frameRate);
            stateManager.attach(recorder);

            String quotedPath = MyString.quote(file.getAbsolutePath());
            logger.log(Level.WARNING, "Started recording to {0}", quotedPath);

        } else {
            File file = vras.getFile();
            stateManager.detach(vras);

            String quotedPath = MyString.quote(file.getAbsolutePath());
            logger.log(Level.WARNING, "Stopped recording to {0}", quotedPath);
        }
    }
}
