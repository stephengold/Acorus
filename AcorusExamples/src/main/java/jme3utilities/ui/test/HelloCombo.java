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

import com.jme3.app.SimpleApplication;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.Collection;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.Combo;
import jme3utilities.ui.HelpUtils;
import jme3utilities.ui.InputMode;

/**
 * Test/demonstrate combo bindings.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloCombo extends ActionApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(HelloCombo.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = HelloCombo.class.getSimpleName();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloCombo application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        HelloCombo application = new HelloCombo();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setRenderer(AppSettings.LWJGL_OPENGL32);
        settings.setTitle(title); // Customize the window's title bar.
        application.setSettings(settings);

        application.start();
    }
    // *************************************************************************
    // ActionApplication methods

    /**
     * Initialize this application.
     */
    @Override
    public void actionInitializeApplication() {
        // do nothing
    }

    /**
     * Callback invoked when the active InputMode changes.
     *
     * @param oldMode the old mode, or null if none
     * @param newMode the new mode, or null if none
     */
    @Override
    public void inputModeChange(InputMode oldMode, InputMode newMode) {
        if (newMode != null) {
            attachHelpNode(newMode);
        }
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
            }
        }

        super.onAction(actionString, ongoing, tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Build and attach the help node for the specified InputMode.
     *
     * @param inputMode (not null, unaffected)
     */
    private void attachHelpNode(InputMode inputMode) {
        Camera guiCamera = guiViewPort.getCamera();
        float x = 10f;
        float y = guiCamera.getHeight() - 10f;
        float width = guiCamera.getWidth() - 20f;
        float height = guiCamera.getHeight() - 20f;
        Rectangle bounds = new Rectangle(x, y, width, height);

        float space = 20f; // separation between actions, in pixels
        Node helpNode = HelpUtils.buildNode(
                inputMode, bounds, guiFont, space, ColorRGBA.Black);
        guiNode.attachChild(helpNode);
    }

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
            String comboName = array[arrayIndex].toStringLocal();
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
        System.out.println(" to quit.");
        System.out.println();
    }
}
