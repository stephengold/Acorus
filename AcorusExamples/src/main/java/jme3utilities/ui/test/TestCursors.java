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
import com.jme3.app.state.AppState;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.InputMode;

/**
 * Test/demonstrate the cursors that are built in to Acorus.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestCursors extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestCursors.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestCursors.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * status displayed in the upper-left corner of the GUI node
     */
    private BitmapText statusText;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AcorusDemo without any initial appstates.
     */
    private TestCursors() {
        super((AppState[]) null);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestCursors application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        TestCursors application = new TestCursors();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setTitle(title); // Customize the window's title bar.
        application.setSettings(settings);

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
        /*
         * Instantiate the status text and attach it to the GUI node.
         */
        statusText = new BitmapText(guiFont);
        statusText.setLocalTranslation(0f, cam.getHeight(), 0f);
        guiNode.attachChild(statusText);

        super.acorusInit();
        /*
         * DefaultInputMode uses the "default" cursor style.
         * Create input modes for the other 3 built-in cursor styles.
         */
        for (final String name : new String[]{"dialog", "green", "menu"}) {
            InputMode mode = new InputMode(name) {
                @Override
                protected void defaultBindings() {
                    bind(SimpleApplication.INPUT_MAPPING_EXIT,
                            KeyInput.KEY_ESCAPE);

                    bind("default", KeyInput.KEY_F1);
                    if (!name.equals("dialog")) {
                        bind("dialog", KeyInput.KEY_F2);
                    }
                    if (!name.equals("green")) {
                        bind("green", KeyInput.KEY_F3);
                    }
                    if (!name.equals("menu")) {
                        bind("menu", KeyInput.KEY_F4);
                    }
                }

                @Override
                public void onAction(String as, boolean ongoing, float tpf) {
                    /*
                     * Forward all actions to the application for processing.
                     */
                    getActionApplication().onAction(as, ongoing, tpf);
                }
            };

            String assetPath = String.format("Textures/cursors/%s.cur", name);
            JmeCursor cursor = (JmeCursor) assetManager.loadAsset(assetPath);
            mode.setCursor(cursor);

            stateManager.attach(mode);
        }
    }

    /**
     * Callback invoked when the active InputMode changes.
     *
     * @param oldMode the old mode, or null if none
     * @param newMode the new mode, or null if none
     */
    public void inputModeChange(InputMode oldMode, InputMode newMode) {
        super.inputModeChange(oldMode, newMode);

        if (newMode != null) {
            String message = "mode = " + newMode.shortName();
            statusText.setText(message);
        }
    }

    /**
     * Callback invoked immediately after initializing the hotkey bindings of
     * the default input mode.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();
        dim.bind("dialog", KeyInput.KEY_F2);
        dim.bind("green", KeyInput.KEY_F3);
        dim.bind("menu", KeyInput.KEY_F4);
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
                case "default":
                case "dialog":
                case "green":
                case "menu":
                    setMode(actionString);
                    return;
            }
        }
        super.onAction(actionString, ongoing, tpf);
    }
    // *************************************************************************
    // private methods

    private void setMode(String modeName) {
        InputMode oldMode = InputMode.getActiveMode();
        InputMode newMode = InputMode.findMode(modeName);

        oldMode.setEnabled(false);
        newMode.setEnabled(true);
    }
}
