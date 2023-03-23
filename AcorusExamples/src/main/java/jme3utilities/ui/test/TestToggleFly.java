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

import com.jme3.input.KeyInput;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.DefaultInputMode;
import jme3utilities.ui.InputMode;
import org.lwjgl.system.Configuration;

/**
 * Test/demonstrate how the default hotkey bindings change when the FlyByCamera
 * is enabled/disabled.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class TestToggleFly extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final static Logger logger
            = Logger.getLogger(TestToggleFly.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestToggleFly.class.getSimpleName();
    /**
     * action string to toggle the state of the FlyByCamera
     */
    final private static String asToggleFlyCam = "toggle flyCam";
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestToggleFly application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        String title = applicationName + " " + MyString.join(arguments);
        TestToggleFly application = new TestToggleFly();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
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
    // *************************************************************************
    // AcorusDemo methods

    /**
     * Initialize this application.
     */
    @Override
    public void acorusInit() {
        super.acorusInit();

        // Create a 3-D scene with something to look at:  a lit green cube.
        DemoScene.setup(this);
    }

    /**
     * Callback invoked immediately after initializing the hotkey bindings of
     * the default input mode.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();
        dim.bind(asToggleFlyCam, KeyInput.KEY_T, KeyInput.KEY_TAB);
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
                case asToggleFlyCam:
                    toggleFlyCam();
                    return;

                default:
            }
        }

        super.onAction(actionString, ongoing, tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Toggle the state of the FlyByCamera between disabled and enabled.
     */
    private void toggleFlyCam() {
        boolean isEnabled = flyCam.isEnabled();
        flyCam.setEnabled(!isEnabled);

        // Update the bindings to reflect the new state.
        DefaultInputMode dim = (DefaultInputMode) getDefaultInputMode();
        dim.updateBindings();

        // Update the help node to reflect the changed bindings.
        updateHelp();
    }
}
