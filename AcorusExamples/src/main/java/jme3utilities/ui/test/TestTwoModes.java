/*
 Copyright (c) 2018-2023, Stephen Gold
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
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.Overlay;
import org.lwjgl.system.Configuration;

/**
 * Test/demonstrate multiple input modes in a single application.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class TestTwoModes extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * index of the status line that indicates the active input mode
     */
    final private static int modeStatusLine = 0;
    /**
     * index of the editable status line
     */
    final private static int editableStatusLine = 2;
    /**
     * number of status line in the overlay
     */
    final private static int numStatusLines = 3;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestTwoModes.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestTwoModes.class.getSimpleName();
    /**
     * action string to switch to "edit" mode
     */
    final private static String asEditText = "edit text";
    // *************************************************************************
    // fields

    /**
     * status overlay, displayed in the upper-left corner of the GUI node
     */
    private Overlay statusOverlay;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestTwoModes application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        String title = applicationName + " " + MyString.join(arguments);
        TestTwoModes application = new TestTwoModes();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setResizable(true);
        settings.setSamples(4); // anti-aliasing
        settings.setTitle(title); // Customize the window's title bar.
        application.setSettings(settings);
        /*
         * The AWT settings dialog interferes with LWJGL v3
         * on macOS and Raspbian, so don't show it!
         */
        application.setShowSettings(false);
        application.start();
    }

    /**
     * Switch from "edit" mode to the default mode.
     */
    static void switchToDefault() {
        InputMode dim = InputMode.findMode("default");
        InputMode editMode = InputMode.findMode("edit");

        editMode.setEnabled(false);
        dim.setEnabled(true);
    }
    // *************************************************************************
    // AcorusDemo methods

    /**
     * Initialize this application.
     */
    @Override
    public void acorusInit() {
        // Create a 3-D scene with something to look at:  a lit green cube.
        DemoScene.setup(this);
        addStatusOverlay();

        // Attach a (disabled) "edit" mode to the state manager.
        InputMode editMode = new EditMode();
        stateManager.attach(editMode);

        // Hide the render-statistics overlay.
        stateManager.getState(StatsAppState.class).toggleStats();

        super.acorusInit();
    }

    /**
     * Callback invoked immediately after initializing the hotkey bindings of
     * the default input mode.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();
        dim.bind(asEditText, KeyInput.KEY_RETURN, KeyInput.KEY_TAB);
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
        if (ongoing && actionString.equals(asEditText)) {
            // Switch from default mode to "edit" mode.
            InputMode dim = InputMode.findMode("default");
            InputMode editMode = InputMode.findMode("edit");

            dim.setEnabled(false);
            editMode.setEnabled(true);
            return;
        }

        super.onAction(actionString, ongoing, tpf);
    }

    /**
     * Callback invoked when the active InputMode changes.
     *
     * @param oldMode the old mode, or null if none
     * @param newMode the new mode, or null if none
     */
    @Override
    public void onInputModeChange(InputMode oldMode, InputMode newMode) {
        super.onInputModeChange(oldMode, newMode);

        if (oldMode != null) {
            logger.log(Level.WARNING, "leaving {0} mode", oldMode.shortName());
            statusOverlay.setText(modeStatusLine, "");
        }
        if (newMode != null) {
            String text = newMode.shortName() + " mode";
            statusOverlay.setText(modeStatusLine, text);

            logger.log(Level.WARNING, "entering {0} mode", newMode.shortName());
        }
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
        super.onViewPortResize(newWidth, newHeight);
        statusOverlay.onViewPortResize(newWidth, newHeight);
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);

        // Update the status overlay.
        EditMode editMode = (EditMode) InputMode.findMode("edit");
        String text = editMode.getText();
        if (editMode.isEnabled()) { // fake a blinking text cursor
            long now = System.nanoTime() % 1_000_000_000L;
            if (now > 500_000_000L) {
                text += "_";
            }
        }
        statusOverlay.setText(editableStatusLine, "Text: " + text);
    }
    // *************************************************************************
    // private methods

    /**
     * Add a status overlay to the GUI scene.
     */
    private void addStatusOverlay() {
        float width = 200f; // in pixels
        this.statusOverlay = new Overlay("status", width, numStatusLines);

        boolean success = stateManager.attach(statusOverlay);
        assert success;

        statusOverlay.setEnabled(true);
    }
}
