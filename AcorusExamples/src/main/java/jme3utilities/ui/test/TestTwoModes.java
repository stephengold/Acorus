/*
 Copyright (c) 2018-2022, Stephen Gold
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
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.HelpUtils;
import jme3utilities.ui.InputMode;

/**
 * An ActionApplication to test/demonstrate multiple input modes.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestTwoModes extends ActionApplication {
    // *************************************************************************
    // constants and loggers

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
    // *************************************************************************
    // fields

    /**
     * status displayed in the upper-left corner of the GUI node
     */
    private BitmapText statusText;
    /**
     * help for the default input mode
     */
    private Node defaultHelp;
    // *************************************************************************
    // new methods exposed

    /**
     * Build the help node for the specified mode.
     *
     * @param mode the input mode (not null)
     * @return a new orphan Node, suitable for attachment to the GUI node
     */
    Node buildHelpNode(InputMode mode) {
        Camera guiCamera = guiViewPort.getCamera();
        float x = 10f;
        float y = guiCamera.getHeight() - 30f; // leave room for status
        float width = guiCamera.getWidth() - 20f;
        float height = guiCamera.getHeight() - 20f;
        Rectangle bounds = new Rectangle(x, y, width, height);

        float space = 20f;
        Node result = HelpUtils.buildNode(mode, bounds, guiFont, space);

        return result;
    }

    /**
     * Main entry point for the TestTwoModes application.
     *
     * @param ignored array of command-line arguments (not null)
     */
    public static void main(String[] ignored) {
        /*
         * Mute the chatty loggers in certain packages.
         */
        Heart.setLoggingLevels(Level.WARNING);
        /*
         * Instantiate the application.
         */
        TestTwoModes application = new TestTwoModes();
        /*
         * Customize the window's title bar.
         */
        AppSettings settings = new AppSettings(true);
        settings.setTitle(applicationName);

        settings.setAudioRenderer(null);
        settings.setGammaCorrection(true);
        settings.setSamples(4); // anti-aliasing
        settings.setVSync(true);
        application.setSettings(settings);
        /*
         * Invoke the JME startup code,
         * which in turn invokes actionInitializeApplication().
         */
        application.start();
    }

    /**
     * Switch from edit mode to default mode.
     */
    void switchToDefault() {
        InputMode dim = InputMode.findMode("default");
        InputMode editMode = InputMode.findMode("edit");

        editMode.setEnabled(false);
        dim.setEnabled(true);

        guiNode.attachChild(defaultHelp);
    }
    // *************************************************************************
    // ActionApplication methods

    /**
     * Initialize this application.
     */
    @Override
    public void actionInitializeApplication() {
        DemoScene.setup(this);
        /*
         * Instantiate the status text and attach it to the GUI node.
         */
        statusText = new BitmapText(guiFont);
        statusText.setLocalTranslation(0f, cam.getHeight(), 0f);
        guiNode.attachChild(statusText);
        /*
         * Attach a (disabled) edit mode to the state manager.
         */
        InputMode editMode = new EditMode();
        stateManager.attach(editMode);
        /*
         * Hide the render-statistics overlay.
         */
        stateManager.getState(StatsAppState.class).toggleStats();
    }

    /**
     * Callback invoked immediately after initializing the hotkey bindings of
     * the default input mode.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();
        dim.bind("edit text", KeyInput.KEY_RETURN, KeyInput.KEY_TAB);
        /*
         * Build and attach the help node for default mode.
         * The help node can't be created until all hotkeys are bound.
         */
        defaultHelp = buildHelpNode(dim);
        guiNode.attachChild(defaultHelp);
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
        if (ongoing && actionString.equals("edit text")) {
            /*
             * Switch from default mode to edit mode.
             */
            InputMode dim = InputMode.findMode("default");
            InputMode editMode = InputMode.findMode("edit");

            dim.setEnabled(false);
            editMode.setEnabled(true);

            defaultHelp.removeFromParent();
            return;
        }

        super.onAction(actionString, ongoing, tpf);
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        /*
         * Update the displayed status.
         */
        EditMode editMode = (EditMode) InputMode.findMode("edit");
        String text = editMode.getText();

        if (editMode.isEnabled()) { // fake a blinking text cursor
            long now = System.nanoTime() % 1_000_000_000L;
            if (now > 500_000_000L) {
                text = text + "_";
            }
        }
        statusText.setText("Text: " + text);
    }
}
