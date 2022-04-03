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

import com.jme3.input.KeyInput;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeVersion;
import com.jme3.texture.FrameBuffer;
import java.awt.DisplayMode;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.debug.Dumper;
import jme3utilities.math.RectSizeLimits;
import jme3utilities.ui.AbstractDemo;
import jme3utilities.ui.DisplaySettings;
import jme3utilities.ui.DsEditOverlay;
import jme3utilities.ui.DsUtils;
import jme3utilities.ui.HelpVersion;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.ShowDialog;
import jme3utilities.ui.UiVersion;

/**
 * Demonstrate how to edit an application's display settings using
 * DsEditInputMode and DsEditOverlay.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestDsEdit extends AbstractDemo {
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
    /**
     * action string to activate the display-settings editor overlay
     */
    final private static String asEditDisplaySettings = "edit display settings";
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

        ShowDialog showDialog = ShowDialog.Never;
        /*
         * Process any command-line arguments.
         */
        for (String arg : arguments) {
            switch (arg) {
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
        mainStartup(showDialog, title);
    }
    // *************************************************************************
    // AbstractDemo methods

    /**
     * Initialize this application.
     */
    @Override
    public void actionInitializeApplication() {
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
        boolean success = stateManager.attach(dseOverlay);
        assert success;

        configureDumper();
        /*
         * Ensure that size-dependent data get initialized.
         */
        int width = guiViewPort.getCamera().getWidth();
        int height = guiViewPort.getCamera().getHeight();
        resize(width, height);

        addSceneProcessor();
        DemoScene.setup(this);
        /*
         * Print the available and current display modes to the console.
         */
        for (DisplayMode mode : DsUtils.listDisplayModes()) {
            System.out.printf("%d x %d, %d bits, %d Hz\n",
                    mode.getWidth(), mode.getHeight(),
                    mode.getBitDepth(), mode.getRefreshRate());
        }

        DisplayMode mode = DsUtils.displayMode();
        System.out.printf("Current mode:  %d x %d, %d bits, %d Hz\n",
                mode.getWidth(), mode.getHeight(),
                mode.getBitDepth(), mode.getRefreshRate());

        System.out.flush();
    }

    /**
     * Callback invoked when the active InputMode changes. TODO use the
     * AbstractDemo default implementation
     *
     * @param oldMode the old mode, or null if none
     * @param newMode the new mode, or null if none
     */
    @Override
    public void inputModeChange(InputMode oldMode, InputMode newMode) {
        if (newMode != null) {
            Camera guiCamera = guiViewPort.getCamera();
            int viewPortWidth = guiCamera.getWidth();
            int viewPortHeight = guiCamera.getHeight();
            updateHelpNodes(newMode, viewPortWidth, viewPortHeight,
                    HelpVersion.Detailed);
        }
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
    // *************************************************************************
    // private methods

    /**
     * Activate the named InputMode.
     *
     * @param shortName the short name of the desired InputMode (not null, mode
     * must be initialized)
     */
    private void activateInputMode(String shortName) {
        InputMode desired = InputMode.findMode(shortName);
        Validate.require(desired != null, "an initialized InputMode");

        InputMode active = InputMode.getActiveMode();
        if (active != desired) {
            InputMode.suspendAndActivate(desired);
        }
    }

    /**
     * Add a SceneProcessor to update the help nodes and the overlay whenever
     * the display changes size.
     */
    private void addSceneProcessor() {
        SceneProcessor sceneProcessor = new SceneProcessor() {
            @Override
            public void cleanup() {
                // do nothing
            }

            @Override
            public void initialize(RenderManager rm, ViewPort unused2) {
                // do nothing
            }

            @Override
            public boolean isInitialized() {
                return true;
            }

            @Override
            public void postFrame(FrameBuffer unused) {
                // do nothing
            }

            @Override
            public void postQueue(RenderQueue unused) {
                // do nothing
            }

            @Override
            public void preFrame(float tpf) {
                // do nothing
            }

            @Override
            public void reshape(ViewPort unused, int newWidth, int newHeight) {
                resize(newWidth, newHeight);
            }

            @Override
            public void setProfiler(AppProfiler unused) {
                // do nothing
            }
        };
        guiViewPort.addProcessor(sceneProcessor);
    }

    /**
     * Configure the Dumper.
     */
    private void configureDumper() {
        dumper.setDumpTransform(true);
    }

    /**
     * Initialization performed immediately after parsing the command-line
     * arguments.
     *
     * @param showDialog when to show the JME settings dialog (not null)
     * @param title for the title bar of the app's window
     */
    private static void mainStartup(final ShowDialog showDialog,
            final String title) {
        TestDsEdit application = new TestDsEdit();

        RectSizeLimits sizeLimits = new RectSizeLimits(
                450, 380, // min width, height
                2_048, 1_080 // max width, height
        );
        proposedSettings = new DisplaySettings(application, applicationName,
                sizeLimits) {
            @Override
            protected void applyOverrides(AppSettings settings) {
                setShowDialog(showDialog);
                settings.setAudioRenderer(null);
                settings.setRenderer(AppSettings.LWJGL_OPENGL32);
                if (settings.getSamples() < 1) {
                    settings.setSamples(1);
                }
                settings.setResizable(true);
                settings.setTitle(title); // Customize the window's title bar.
            }
        };

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

    /**
     * Update the GUI layout and proposed settings after a resize.
     *
     * @param newWidth the new width of the framebuffers (in pixels, &gt;0)
     * @param newHeight the new height of the framebuffers (in pixels, &gt;0)
     */
    private void resize(int newWidth, int newHeight) {
        dseOverlay.resize(newWidth, newHeight);
        proposedSettings.resize(newWidth, newHeight);

        InputMode activeMode = InputMode.getActiveMode();
        updateHelpNodes(activeMode, newWidth, newHeight, HelpVersion.Detailed);
    }
}
