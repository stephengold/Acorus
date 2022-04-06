/*
 Copyright (c) 2022, Stephen Gold
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

import com.jme3.app.state.AppState;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyString;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.HelpUtils;
import jme3utilities.ui.InputMode;

/**
 * This example binds hotkeys to 4 custom actions in the default input mode. The
 * actions cause squares to appear on (or disappear from) the display.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloBind extends ActionApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(HelloBind.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = HelloBind.class.getSimpleName();
    // *************************************************************************
    // field

    /**
     * square controlled by F1
     */
    private Geometry blueSquare;
    /**
     * square controlled by F2
     */
    private Geometry greenSquare;
    /**
     * square controlled by F3
     */
    private Geometry redSquare;
    /**
     * square controlled by F4
     */
    private Geometry whiteSquare;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an ActionApplication without any initial appstates.
     */
    private HelloBind() {
        super((AppState[]) null);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloBind application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);
        HelloBind application = new HelloBind();
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
        float squareSize = 50f; // in pixels
        Quad squareMesh = new Quad(squareSize, squareSize);
        float x2 = cam.getWidth() / 2f;
        float y2 = cam.getHeight() / 2f;
        float x1 = x2 - squareSize;
        float y1 = y2 - squareSize;
        float z = 0.1f;

        Material blueMaterial
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.Blue);
        blueSquare = new Geometry("blue square", squareMesh);
        blueSquare.setMaterial(blueMaterial);
        blueSquare.move(x1, y1, z);

        Material greenMaterial
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.Green);
        greenSquare = new Geometry("red square", squareMesh);
        greenSquare.setMaterial(greenMaterial);
        greenSquare.move(x2, y1, z);

        Material redMaterial
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.Red);
        redSquare = new Geometry("red square", squareMesh);
        redSquare.setMaterial(redMaterial);
        redSquare.move(x1, y2, z);

        Material whiteMaterial
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.White);
        whiteSquare = new Geometry("white square", squareMesh);
        whiteSquare.setMaterial(whiteMaterial);
        whiteSquare.move(x2, y2, z);
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
        dim.bind("toggle blue",
                KeyInput.KEY_F1, KeyInput.KEY_1, KeyInput.KEY_NUMPAD1);
        dim.bind("toggle green",
                KeyInput.KEY_F2, KeyInput.KEY_2, KeyInput.KEY_NUMPAD2);
        dim.bind("toggle red",
                KeyInput.KEY_F3, KeyInput.KEY_3, KeyInput.KEY_NUMPAD3);
        dim.bind("toggle white",
                KeyInput.KEY_F4, KeyInput.KEY_4, KeyInput.KEY_NUMPAD4);
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
                case "toggle blue":
                    toggle(blueSquare);
                    return;
                case "toggle green":
                    toggle(greenSquare);
                    return;
                case "toggle red":
                    toggle(redSquare);
                    return;
                case "toggle white":
                    toggle(whiteSquare);
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

        float space = 20f;
        Node helpNode = HelpUtils.buildNode(inputMode, bounds, guiFont, space);
        guiNode.attachChild(helpNode);
    }

    /**
     * Cause the specified Spatial to appear (if hidden) or disappear (if
     * visible).
     *
     * @param spatial (not null)
     */
    private void toggle(Spatial spatial) {
        if (spatial.getParent() != null) {
            spatial.removeFromParent();
        } else {
            guiNode.attachChild(spatial);
        }
    }
}