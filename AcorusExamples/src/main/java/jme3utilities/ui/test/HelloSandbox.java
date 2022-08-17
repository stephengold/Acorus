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

import com.jme3.app.state.AppState;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import java.io.IOException;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.HelpVersion;
import org.lwjgl.system.Configuration;

/**
 * Test/demonstrate the Acorus sandbox and the built-in "screenshot" action.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class HelloSandbox extends AcorusDemo {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(HelloSandbox.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = HelloSandbox.class.getSimpleName();
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AcorusDemo without any initial appstates.
     */
    private HelloSandbox() {
        super((AppState[]) null);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloSandbox application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        String title = applicationName + " " + MyString.join(arguments);
        HelloSandbox application = new HelloSandbox();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setAudioRenderer(null);
        settings.setSamples(4); // anti-aliasing
        settings.setTitle(title); // Customize the window's title bar.
        application.setSettings(settings);
        /*
         * Designate a sandbox directory.
         * This has to be done *prior to* initialization.
         */
        try {
            ActionApplication.designateSandbox("./Written Assets");
        } catch (IOException exception) {
            // do nothing
        }
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
        setHelpVersion(HelpVersion.Detailed);
        /*
         * Create a 3-D scene with something to look at:  a lit green cube.
         */
        DemoScene.setup(this);
    }
}
