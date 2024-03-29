/*
 Copyright (c) 2022-2023, Stephen Gold

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
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyString;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.Signals;

/**
 * This example binds signals to 4 custom actions in the default input mode. The
 * actions cause squares to appear on (or disappear from) the display.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class HelloSignals extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final static Logger logger = Logger.getLogger(HelloSignals.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = HelloSignals.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * square controlled by F1
     */
    private static Geometry blueSquare;
    /**
     * square controlled by F2
     */
    private static Geometry greenSquare;
    /**
     * square controlled by F3
     */
    private static Geometry redSquare;
    /**
     * square controlled by F4
     */
    private static Geometry whiteSquare;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AcorusDemo without any initial appstates.
     */
    private HelloSignals() {
        super((AppState[]) null);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloSignals application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        AppChooser.setGlfwLibraryName();

        String title = applicationName + " " + MyString.join(arguments);
        HelloSignals application = new HelloSignals();
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
        this.greenSquare = new Geometry("red square", squareMesh);
        greenSquare.setMaterial(greenMaterial);
        greenSquare.move(x2, y1, z);

        Material redMaterial
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.Red);
        this.redSquare = new Geometry("red square", squareMesh);
        redSquare.setMaterial(redMaterial);
        redSquare.move(x1, y2, z);

        Material whiteMaterial
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.White);
        this.whiteSquare = new Geometry("white square", squareMesh);
        whiteSquare.setMaterial(whiteMaterial);
        whiteSquare.move(x2, y2, z);
    }

    /**
     * Callback invoked immediately after initializing the hotkey bindings of
     * the default input mode.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();
        dim.bindSignal("show blue",
                KeyInput.KEY_F1, KeyInput.KEY_1, KeyInput.KEY_NUMPAD1);
        dim.bindSignal("show green",
                KeyInput.KEY_F2, KeyInput.KEY_2, KeyInput.KEY_NUMPAD2);
        dim.bindSignal("show red",
                KeyInput.KEY_F3, KeyInput.KEY_3, KeyInput.KEY_NUMPAD3);
        dim.bindSignal("show white",
                KeyInput.KEY_F4, KeyInput.KEY_4, KeyInput.KEY_NUMPAD4);
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
         * Test which signals are currently active
         * and update the GUI node accordingly.
         */
        Signals signals = getSignals();

        if (signals.test("show blue")) {
            guiNode.attachChild(blueSquare);
        } else {
            blueSquare.removeFromParent();
        }

        if (signals.test("show green")) {
            guiNode.attachChild(greenSquare);
        } else {
            greenSquare.removeFromParent();
        }

        if (signals.test("show red")) {
            guiNode.attachChild(redSquare);
        } else {
            redSquare.removeFromParent();
        }

        if (signals.test("show white")) {
            guiNode.attachChild(whiteSquare);
        } else {
            whiteSquare.removeFromParent();
        }
    }
}
