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

import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.HelpUtils;
import jme3utilities.ui.InputMode;

/**
 * An example of minimizing a help node the hard way.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloToggleHelp extends ActionApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(HelloToggleHelp.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = HelloToggleHelp.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * Node for displaying detailed hotkey help in the GUI scene
     */
    private Node detailedHelpNode;
    /**
     * Node for displaying minimal help ("toggle help: H") in the GUI scene
     */
    private Node minHelpNode;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloToggleHelp application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        HelloToggleHelp application = new HelloToggleHelp();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setRenderer(AppSettings.LWJGL_OPENGL32);
        settings.setSamples(4); // anti-aliasing
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
        DemoScene.setup(this);
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
            attachHelpNodes(newMode);
        }
    }

    /**
     * Callback invoked immediately after initializing the hotkey bindings of
     * the default input mode.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();
        dim.bind("toggle help", KeyInput.KEY_H);
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
                case "toggle help":
                    toggleHelp();
                    return;
            }
        }
        super.onAction(actionString, ongoing, tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Generate both versions of the help node for the specified InputMode.
     * Attach the minimal one to the GUI scene.
     *
     * @param inputMode (not null, unaffected)
     */
    private void attachHelpNodes(InputMode inputMode) {
        Camera guiCamera = guiViewPort.getCamera();
        /*
         * Build the detailed help node.
         */
        float x = 10f;
        float y = guiCamera.getHeight() - 10f;
        float width = guiCamera.getWidth() - 20f;
        float height = guiCamera.getHeight() - 20f;
        Rectangle bounds = new Rectangle(x, y, width, height);

        float space = 20f;
        detailedHelpNode
                = HelpUtils.buildNode(inputMode, bounds, guiFont, space);
        detailedHelpNode.move(0f, 0f, 1f); // move (slightly) to the front
        /*
         * Build and attach the minimal help node.
         */
        InputMode dummyMode = new InputMode("dummy") {
            @Override
            protected void defaultBindings() {
            }

            @Override
            public void onAction(String s, boolean b, float f) {
            }
        };
        dummyMode.bind("toggle help", KeyInput.KEY_H);

        width = 100f; // in pixels
        x = guiCamera.getWidth() - 110f;
        Rectangle dummyBounds = new Rectangle(x, y, width, height);

        minHelpNode = HelpUtils.buildNode(dummyMode, dummyBounds, guiFont, 0f);
        guiNode.attachChild(minHelpNode);
    }

    /**
     * Toggle between the detailed help node and the minimal one.
     */
    private void toggleHelp() {
        if (detailedHelpNode.getParent() == null) {
            minHelpNode.removeFromParent();
            guiNode.attachChild(detailedHelpNode);
        } else {
            detailedHelpNode.removeFromParent();
            guiNode.attachChild(minHelpNode);
        }
    }
}
