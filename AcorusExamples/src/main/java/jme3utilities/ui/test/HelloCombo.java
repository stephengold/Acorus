/*
 Copyright (c) 2020-2023, Stephen Gold
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

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import java.util.Collection;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.Combo;
import jme3utilities.ui.HelpBuilder;
import jme3utilities.ui.InputMode;
import org.lwjgl.system.Configuration;

/**
 * Test/demonstrate combo bindings.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class HelloCombo extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final static Logger logger = Logger.getLogger(HelloCombo.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = HelloCombo.class.getSimpleName();
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AcorusDemo without any initial appstates.
     */
    private HelloCombo() {
        super((AppState[]) null);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloCombo application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        String title = applicationName + " " + MyString.join(arguments);
        HelloCombo application = new HelloCombo();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setResizable(true);
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

        ColorRGBA gray = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);
        viewPort.setBackgroundColor(gray);
    }

    /**
     * Callback invoked immediately after initializing the hotkey bindings of
     * the default input mode.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();
        dim.bindSignal("ctrl", KeyInput.KEY_LCONTROL, KeyInput.KEY_RCONTROL);
        dim.bindSignal("shift", KeyInput.KEY_LSHIFT, KeyInput.KEY_RSHIFT);

        Combo ctrlE = new Combo(KeyInput.KEY_E, "ctrl", true);
        Combo noShiftY = new Combo(KeyInput.KEY_Y, "shift", false);
        Combo shiftR = new Combo(KeyInput.KEY_R, "shift", true);
        dim.bind(SimpleApplication.INPUT_MAPPING_EXIT, ctrlE);
        dim.bind(SimpleApplication.INPUT_MAPPING_EXIT, noShiftY);
        dim.bind(SimpleApplication.INPUT_MAPPING_EXIT, shiftR);

        Combo noCtrlE = new Combo(KeyInput.KEY_E, "ctrl", false);
        Combo noShiftR = new Combo(KeyInput.KEY_R, "shift", false);
        Combo shiftY = new Combo(KeyInput.KEY_Y, "shift", true);
        dim.bind("hint", noCtrlE);
        dim.bind("hint", noShiftR);
        dim.bind("hint", shiftY);
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
                case "hint":
                    printQuitHint();
                    return;

                default:
            }
        }

        super.onAction(actionString, ongoing, tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Enumerate all the quit alternatives to System.out.
     */
    private void printQuitHint() {
        InputMode dim = getDefaultInputMode();
        Collection<Combo> quitCombos
                = dim.listCombos(SimpleApplication.INPUT_MAPPING_EXIT);
        int numCombos = quitCombos.size();
        assert numCombos > 0 : numCombos;
        Combo[] array = new Combo[numCombos];
        quitCombos.toArray(array);
        int lastIndex = numCombos - 1;

        System.out.println();
        System.out.print("Use ");
        for (int arrayIndex = 0; arrayIndex < numCombos; ++arrayIndex) {
            String comboName = HelpBuilder.describe(array[arrayIndex]);
            System.out.print(comboName);
            if (arrayIndex != lastIndex) {
                if (numCombos > 2) {
                    System.out.print(',');
                }
                System.out.print(' ');
                if (arrayIndex == lastIndex - 1) {
                    System.out.print("or ");
                }
            }
        }
        System.out.println(" to exit.");
        System.out.println();
    }
}
