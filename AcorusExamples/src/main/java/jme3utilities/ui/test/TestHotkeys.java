/*
 Copyright (c) 2020-2022, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

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

import com.jme3.font.BitmapText;
import com.jme3.system.AppSettings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.Hotkey;
import jme3utilities.ui.InputMode;

/**
 * An ActionApplication to log all button/keyboard input (for testing).
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestHotkeys extends ActionApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestHotkeys.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestHotkeys.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * multi-line status displayed in the upper-left corner of the GUI node
     */
    private static BitmapText statusText;
    /**
     * list of active hotkeys
     */
    final private static Collection<Hotkey> activeHotkeys = new ArrayList<>(10);
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestHotKeys application.
     *
     * @param ignored array of command-line arguments (not null)
     */
    public static void main(String[] ignored) {
        /*
         * Instantiate the application.
         */
        TestHotkeys application = new TestHotkeys();
        /*
         * Customize the window's title bar.
         */
        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setTitle(applicationName);
        settings.setAudioRenderer(null);
        settings.setUseJoysticks(true);
        application.setSettings(settings);
        /*
         * Invoke the JME startup code,
         * which in turn invokes actionInitializeApplication().
         */
        application.start();
    }
    // *************************************************************************
    // ActionApplication methods

    /**
     * Initialize this application.
     */
    @Override
    public void actionInitializeApplication() {
        if (settings.isFullscreen()) {
            logger.log(Level.SEVERE, "Not designed to run full-screen.");
            stop();
        }
        /*
         * Avert warnings about signals not added.
         */
        flyCam.setEnabled(false);
        /*
         * Instantiate the status text and attach it to the GUI.
         */
        statusText = new BitmapText(guiFont);
        statusText.setLocalTranslation(0f, cam.getHeight(), 0f);
        guiNode.attachChild(statusText);
    }

    /**
     * Add application-specific hotkey bindings and override any existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        List<Hotkey> allKeys = Hotkey.listAll();
        InputMode dim = getDefaultInputMode();
        /*
         * Bind (or rebind) each hotkey so that its action indicates
         * its universal code, US name, and local name.
         *
         * DefaultInputMode.onAction() will log each time a hotkey
         * gets activated or deactivated.
         *
         * Since DefaultInputMode doesn't recoginze any of these actions,
         * they are handled by the application's onAction() method.
         */
        for (Hotkey key : allKeys) {
            String actionName = String.format("code(%d)  US(%s)  local(%s)",
                    key.code(), key.usName(), key.localName());
            dim.bind(actionName, key);
        }
    }

    /**
     * Process an action that wasn't handled by the active input mode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        /*
         * Parse the action string to determine
         * which hotkey triggered the action.
         */
        int localIndex = actionString.indexOf("local(");
        assert localIndex >= 0 : localIndex;
        int beginIndex = localIndex + 6;

        int closeIndex = actionString.indexOf(")", beginIndex);
        assert closeIndex >= 0 : localIndex;

        String localName = actionString.substring(beginIndex, closeIndex);
        Hotkey hotkey = Hotkey.findLocal(localName);
        /*
         * Update the list of active keys.
         */
        if (ongoing) {
            activeHotkeys.add(hotkey);
        } else {
            activeHotkeys.remove(hotkey);
        }
        /*
         * Update the displayed status:  one line for each active hotkey.
         */
        String status = "";
        for (Hotkey active : activeHotkeys) {
            status += String.format("code(%d)  US(%s)  local(%s)\n",
                    active.code(), active.usName(), active.localName());
        }
        statusText.setText(status);
    }
}