/*
 Copyright (c) 2020-2022, Stephen Gold
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
package jme3utilities.ui.test;

import com.jme3.app.StatsAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeVersion;
import com.jme3.texture.image.ColorSpace;
import java.awt.DisplayMode;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.debug.Dumper;
import jme3utilities.math.RectSizeLimits;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.DisplaySettings;
import jme3utilities.ui.DsEditOverlay;
import jme3utilities.ui.DsUtils;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.ShowDialog;
import jme3utilities.ui.UiVersion;

/**
 * Demonstrate how to edit an application's display settings using
 * DsEditInputMode and DsEditOverlay.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestDsEdit extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestDsEdit.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestDsEdit.class.getSimpleName();
    /**
     * action string to dump the RenderManager
     */
    final private static String asDumpScenes = "dump scenes";
    // *************************************************************************
    // fields

    /**
     * proposed display settings
     */
    private static DisplaySettings proposedSettings;
    /**
     * AppState to manage the display-settings editor overlay
     */
    private DsEditOverlay dseOverlay;
    /**
     * dump debugging information to System.out
     */
    final private Dumper dumper = new Dumper();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestDsEdit application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        /*
         * Mute the chatty loggers in certain packages.
         */
        Heart.setLoggingLevels(Level.WARNING);

        boolean resetOnly = false;
        ShowDialog showDialog = ShowDialog.Never;
        /*
         * Process any command-line arguments.
         */
        for (String arg : arguments) {
            switch (arg) {
                case "--deleteOnly":
                    deleteStoredSettings();
                    System.exit(0);
                    break;

                case "--resetOnly":
                    resetOnly = true;
                    break;

                case "--showSettingsDialog":
                    showDialog = ShowDialog.FirstTime;
                    break;

                case "--verbose":
                    Heart.setLoggingLevels(Level.INFO);
                    break;

                default:
                    logger.log(Level.WARNING,
                            "Unknown command-line argument {0}",
                            MyString.quote(arg));
            }
        }

        String title = applicationName + " " + MyString.join(arguments);
        mainStartup(resetOnly, showDialog, title);
    }
    // *************************************************************************
    // AcorusDemo methods

    /**
     * Initialize this application.
     */
    @Override
    public void acorusInit() {
        /*
         * Log library versions.
         */
        logger.log(Level.INFO, "jme3-core version is {0}",
                MyString.quote(JmeVersion.FULL_NAME));
        logger.log(Level.INFO, "Heart version is {0}",
                MyString.quote(Heart.versionShort()));
        logger.log(Level.INFO, "Acorus version is {0}",
                MyString.quote(UiVersion.versionShort()));

        dseOverlay = new DsEditOverlay(proposedSettings);
        dseOverlay.setBackgroundColor(new ColorRGBA(0f, 0.05f, 0f, 1f));
        boolean success = stateManager.attach(dseOverlay);
        assert success;

        configureDumper();
        /*
         * Hide the render-statistics overlay.
         */
        stateManager.getState(StatsAppState.class).toggleStats();

        super.acorusInit();

        DemoScene.setup(this);
        /*
         * Print the available and current display modes to the console.
         */
        for (DisplayMode mode : DsUtils.listDisplayModes()) {
            System.out.printf("%d x %d, %d bits, %d Hz%n",
                    mode.getWidth(), mode.getHeight(),
                    mode.getBitDepth(), mode.getRefreshRate());
        }

        DisplayMode mode = DsUtils.displayMode();
        System.out.printf("Current mode:  %d x %d, %d bits, %d Hz%n",
                mode.getWidth(), mode.getHeight(),
                mode.getBitDepth(), mode.getRefreshRate());

        boolean v3 = DsUtils.hasLwjglVersion3();
        System.out.printf("LWJGL v3 is %s.%n", v3);

        int fbWidth = DsUtils.framebufferWidth(context);
        int fbHeight = DsUtils.framebufferHeight(context);
        System.out.printf("Framebuffer:  %d x %d%n", fbWidth, fbHeight);

        int x = DsUtils.windowXPosition(context);
        int y = DsUtils.windowYPosition(context);
        System.out.printf("Window position:  x=%d, y=%d%n", x, y);

        System.out.flush();
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();
        dim.bind(asDumpScenes, KeyInput.KEY_P);
        dim.bind(asEditDisplaySettings, KeyInput.KEY_F2);
        dim.bind(asToggleHelp, KeyInput.KEY_H);
    }

    /**
     * Process an action that wasn't handled by the active InputMode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing) {
            switch (actionString) {
                case asDumpScenes:
                    dumper.dump(renderManager);
                    return;

                case asEditDisplaySettings:
                    activateInputMode("dsEdit");
                    return;

                case asToggleHelp:
                    toggleHelp();
                    return;
            }
        }
        /*
         * The action was not handled here: forward it to the superclass.
         */
        super.onAction(actionString, ongoing, tpf);
    }

    /**
     * Update colors after the renderer's ColorSpace changes.
     *
     * @param newSpace the new ColorSpace (not null)
     */
    @Override
    public void onColorSpaceChange(ColorSpace newSpace) {
        super.onColorSpaceChange(newSpace);
        dseOverlay.onColorSpaceChange(newSpace);
    }

    /**
     * Update the GUI layout and proposed settings after the GUI viewport gets
     * resized.
     *
     * @param newWidth the new width of the ViewPort (in pixels, &gt;0)
     * @param newHeight the new height of the ViewPort (in pixels, &gt;0)
     */
    @Override
    public void onViewPortResize(int newWidth, int newHeight) {
        dseOverlay.onViewPortResize(newWidth, newHeight);
        proposedSettings.resize(newWidth, newHeight);

        super.onViewPortResize(newWidth, newHeight);
    }
    // *************************************************************************
    // private methods

    /**
     * Configure the Dumper.
     */
    private void configureDumper() {
        dumper.setDumpTransform(true);
    }

    /**
     * Delete the application's stored settings, if any. TODO use Heart library
     */
    private static void deleteStoredSettings() {
        try {
            if (Preferences.userRoot().nodeExists(applicationName)) {
                Preferences.userRoot().node(applicationName).removeNode();
                logger.log(Level.WARNING,
                        "The stored settings for \"{0}\" were deleted.",
                        applicationName);
            } else {
                logger.log(Level.WARNING,
                        "No stored settings for \"{0}\" were found.",
                        applicationName);
            }

        } catch (BackingStoreException exception) {
            logger.log(Level.SEVERE,
                    "The stored settings for \"{0}\" are inaccessible.",
                    applicationName);
        }
    }

    /**
     * Initialization performed immediately after parsing the command-line
     * arguments.
     *
     * @param resetOnly true to reset the saved settings and then exit, false to
     * run the application
     * @param showDialog when to show the JME settings dialog (not null)
     * @param title for the title bar of the app's window
     */
    private static void mainStartup(boolean resetOnly,
            final ShowDialog showDialog, final String title) {
        TestDsEdit application = new TestDsEdit();

        RectSizeLimits sizeLimits = new RectSizeLimits(
                450, 380, // min width, height
                2_048, 1_080 // max width, height
        );
        proposedSettings = new DisplaySettings(application, applicationName,
                sizeLimits) {
            @Override
            protected void applyOverrides(AppSettings appSettings) {
                setShowDialog(showDialog);
                appSettings.setAudioRenderer(null);
                if (appSettings.getSamples() < 1) {
                    appSettings.setSamples(1);
                }
                appSettings.setResizable(true);
                appSettings.setTitle(title); // Customize the window's title bar.
            }
        };

        if (resetOnly) {
            /*
             * In case bad settings get written to persistent storage,
             * the --resetOnly command-line option can be used to recover.
             */
            proposedSettings.loadDefaults();
            boolean success = proposedSettings.save();
            if (success) {
                logger.log(Level.WARNING, "The stored settings for \"{0}\" "
                        + "were reset to their default values.",
                        applicationName);
            }
            System.exit(0);
        }

        AppSettings appSettings = proposedSettings.initialize();
        if (appSettings != null) {
            application.setSettings(appSettings);
            /*
             * If the settings dialog should be shown,
             * it has already been shown by DisplaySettings.initialize().
             */
            application.setShowSettings(false);

            application.start();
        }
    }
}
