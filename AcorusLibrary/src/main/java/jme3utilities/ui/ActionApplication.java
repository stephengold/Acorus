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
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.input.CameraInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferUtils;
import java.io.File;
import java.io.IOException;
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
    // *************************************************************************
    // fields

    /**
     * set to true in {@link #simpleInitApp()}
     */
    private static boolean isInitialized = false;
    /**
     * directory for writing assets, or null if none has been designated
     */
    private static File writtenAssetDir = null;
    /**
     * initial input mode: set in {@link #simpleInitApp()}
     */
    private InputMode defaultInputMode = null;
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
    abstract public void actionInitializeApplication();

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
    public static void designateWrittenAssetPath(String desiredPath)
            throws IOException {
        if (writtenAssetDir != null) {
            throw new IllegalStateException(
                    "Don't invoke this method more than once.");
        }
        if (isInitialized) {
            throw new IllegalStateException(
                    "too late - application is already initialized");
        }

        if (desiredPath == null) {
            writtenAssetDir = new File("./Written Assets");
        } else {
            writtenAssetDir = new File(desiredPath);
        }
        String fixedPath = writtenAssetPath();
        String quotedPath = MyString.quote(fixedPath);

        if (!writtenAssetDir.exists()) {
            boolean success = writtenAssetDir.mkdirs();
            if (!success) {
                throw new IOException(
                        "Failed to create a directory at " + quotedPath);
            }
        }

        assert writtenAssetDir.exists();
        if (!writtenAssetDir.isDirectory()) {
            logger.log(Level.WARNING, "{0} exists, but is not a directory.",
                    quotedPath);
        } else if (!writtenAssetDir.canWrite()) {
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
     * asset. Assumes that {@link #designateWrittenAssetPath(java.lang.String)}
     * has been invoked.
     *
     * @param assetPath (not null)
     * @return the file-system path (not null, not empty)
     */
    public static String filePath(String assetPath) {
        Validate.nonNull(assetPath, "asset path");
        if (writtenAssetDir == null) {
            throw new IllegalStateException(
                    "No written-asset path has been designated.");
        }

        File file = new File(writtenAssetDir, assetPath);
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
     * Callback invoked when the active InputMode changes. Meant to be
     * overridden.
     *
     * @param oldMode the old mode, or null if none
     * @param newMode the new mode, or null if none
     */
    public void inputModeChange(InputMode oldMode, InputMode newMode) {
        // do nothing
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
     * Determine the filesystem path to the directory for writing assets.
     * Assumes that {@link #designateWrittenAssetPath(java.lang.String)} has
     * been invoked.
     *
     * @return the canonical pathname (not null, not empty)
     */
    public static String writtenAssetPath() {
        if (writtenAssetDir == null) {
            throw new IllegalStateException(
                    "No written-asset path has been designated.");
        }
        String path = Heart.fixedPath(writtenAssetDir);

        assert !path.isEmpty();
        return path;
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
            /*
             * Process combo actions.
             */
            if (actionString.startsWith(InputMode.comboActionPrefix)) {
                InputMode mode = InputMode.getActiveMode();
                if (mode != null) {
                    String arg = MyString.remainder(actionString,
                            InputMode.comboActionPrefix);
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
                    }
                    break;

                case SimpleApplication.INPUT_MAPPING_EXIT:
                    stop();
                    break;

                case SimpleApplication.INPUT_MAPPING_CAMERA_POS:
                    if (cam != null) {
                        Vector3f loc = cam.getLocation();
                        Quaternion rot = cam.getRotation();
                        System.out.println("Camera Position: ("
                                + loc.x + ", " + loc.y + ", " + loc.z + ")");
                        System.out.println("Camera Rotation: " + rot);
                        System.out.println("Camera Direction: "
                                + cam.getDirection());
                        System.out.println("cam.setLocation(new Vector3f("
                                + loc.x + "f, " + loc.y + "f, " + loc.z
                                + "f));");
                        System.out.println("cam.setRotation(new Quaternion("
                                + rot.getX() + "f, " + rot.getY() + "f, "
                                + rot.getZ() + "f, " + rot.getW() + "f));");
                    }
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
        Validate.positive(newSpeed, "speed");
        speed = newSpeed;
    }

    /**
     * Startup code for this application.
     */
    @Override
    final public void simpleInitApp() {
        if (isInitialized) {
            throw new IllegalStateException(
                    "application may only be initialized once");
        }
        isInitialized = true;

        Locators.setAssetManager(assetManager);
        if (writtenAssetDir != null) {
            /*
             * Initialize asset locators to the default list.
             */
            assetManager.unregisterLocator("/", ClasspathLocator.class);
            Locators.useDefault();
        }
        /*
         * Register a loader for Properties assets.
         */
        assetManager.registerLoader(PropertiesLoader.class, "properties");
        /*
         * Initialize hotkeys.
         */
        Hotkey.initialize(inputManager);

        defaultInputMode = stateManager.getState(DefaultInputMode.class);
        if (defaultInputMode == null) {
            /*
             * Attach and enable the standard initial input mode.
             */
            defaultInputMode = new DefaultInputMode();
            stateManager.attach(defaultInputMode);
            defaultInputMode.setEnabled(true);
        }

        ScreenshotAppState screenshotAppState
                = stateManager.getState(ScreenshotAppState.class);
        if (screenshotAppState == null && writtenAssetDir != null) {
            /*
             * Capture a screenshot to the written-asset directory
             * each time KEY_SYSRQ (a.k.a. the PrtSc key) is pressed.
             */
            String waPath = writtenAssetPath() + "/";
            screenshotAppState
                    = new ScreenshotAppState(waPath, "screenshot");
            boolean success = stateManager.attach(screenshotAppState);
            assert success;
        }
        /*
         * Invoke the startup code of the subclass.
         */
        actionInitializeApplication();
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
}
