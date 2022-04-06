/*
 Copyright (c) 2019-2022, Stephen Gold
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
package jme3utilities.ui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.KeyInput;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * InputMode for the Acorus display-settings editor.
 * <p>
 * Activate this mode using InputMode.suspendAndActivate().
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DsEditInputMode extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DsEditInputMode.class.getName());
    /**
     * asset path to the cursor for this mode
     */
    final private static String assetPath = "Textures/cursors/default.cur";
    /**
     * action string to apply the proposed settings
     */
    final private static String asApplyChanges = "apply changes";
    /**
     * action string to close the overlay
     */
    final private static String asCloseOverlay = "close overlay";
    /**
     * action string to reset the proposed settings to the app's defaults
     */
    final private static String asLoadDefaults = "load defaults";
    /**
     * action string to advance to the next field
     */
    final private static String asNextField = "next field";
    /**
     * action string to advance to the next value
     */
    final private static String asNextValue = "next value";
    /**
     * action string to go to the previous field
     */
    final private static String asPreviousField = "previous field";
    /**
     * action string to go to the previous value
     */
    final private static String asPreviousValue = "previous value";
    /**
     * action string to revert the proposed settings
     */
    final private static String asRevertChanges = "revert changes";
    /**
     * action string to save the proposed settings
     */
    final private static String asSaveChanges = "save changes";
    /**
     * signal that's active when a control key is pressed
     */
    final private static String signalCtrl = "ctrl";
    // *************************************************************************
    // fields

    /**
     * proposed display settings: set by constructor
     */
    final private DisplaySettings proposedSettings;
    /**
     * corresponding overlay: set by constructor
     */
    final private DsEditOverlay statusOverlay;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a uninitialized InputMode.
     *
     * @param overlay the corresponding overlay (not null)
     * @param settings the proposed display settings (not null, alias created)
     */
    DsEditInputMode(DsEditOverlay overlay, DisplaySettings settings) {
        super("dsEdit");
        Validate.nonNull(overlay, "overlay");
        Validate.nonNull(settings, "settings");

        this.statusOverlay = overlay;
        this.proposedSettings = settings;

        influence(overlay);
    }
    // *************************************************************************
    // InputMode methods

    /**
     * Add default hotkey bindings.
     */
    @Override
    protected void defaultBindings() {
        bindSignal(signalCtrl, KeyInput.KEY_LCONTROL, KeyInput.KEY_RCONTROL);

        Combo ctrlA = new Combo(KeyInput.KEY_A, signalCtrl, true);
        bind(asLoadDefaults, ctrlA);
        Combo ctrlS = new Combo(KeyInput.KEY_S, signalCtrl, true);
        bind(asSaveChanges, ctrlS);
        Combo ctrlZ = new Combo(KeyInput.KEY_Z, signalCtrl, true);
        bind(asRevertChanges, ctrlZ);

        bind(asApplyChanges, KeyInput.KEY_RETURN);
        bind(asCloseOverlay, KeyInput.KEY_ESCAPE);
        bind(asNextField, KeyInput.KEY_NUMPAD2, KeyInput.KEY_DOWN);
        bind(asNextValue,
                KeyInput.KEY_EQUALS, KeyInput.KEY_NUMPAD6, KeyInput.KEY_RIGHT);
        bind(asPreviousField, KeyInput.KEY_NUMPAD8, KeyInput.KEY_UP);
        bind(asPreviousValue,
                KeyInput.KEY_MINUS, KeyInput.KEY_NUMPAD4, KeyInput.KEY_LEFT);

        bind(AbstractDemo.asToggleHelp, KeyInput.KEY_H);
        bind(SimpleApplication.INPUT_MAPPING_HIDE_STATS, KeyInput.KEY_F5);
    }

    /**
     * Initialize this (disabled) mode prior to its first update.
     *
     * @param stateManager (not null)
     * @param application (not null)
     */
    @Override
    public void initialize(AppStateManager stateManager,
            Application application) {
        Validate.nonNull(stateManager, "state manager");

        AssetManager am = application.getAssetManager();
        JmeCursor cursor = (JmeCursor) am.loadAsset(assetPath);
        setCursor(cursor);

        super.initialize(stateManager, application);
    }

    /**
     * Process an action from the GUI or keyboard.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time per frame (in seconds)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing) {
            switch (actionString) {
                case asApplyChanges:
                    if (proposedSettings.canApply()
                            && !proposedSettings.areApplied()) {
                        proposedSettings.applyToContext();
                    }
                    return;

                case asCloseOverlay:
                    InputMode.resumeLifo();
                    return;

                case asLoadDefaults:
                    proposedSettings.loadDefaults();
                    return;

                case asNextField:
                    statusOverlay.advanceSelectedField(+1);
                    return;

                case asNextValue:
                    statusOverlay.advanceValue(+1);
                    return;

                case asPreviousField:
                    statusOverlay.advanceSelectedField(-1);
                    return;

                case asPreviousValue:
                    statusOverlay.advanceValue(-1);
                    return;

                case asRevertChanges:
                    revert();
                    return;

                case asSaveChanges:
                    save();
                    return;
            }
        }
        /*
         * Forward the unhandled action to the application.
         */
        getActionApplication().onAction(actionString, ongoing, tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Handle a "revert changes" action.
     */
    private void revert() {
        boolean success = proposedSettings.load();
        if (success) {
            String appName = proposedSettings.applicationName();
            logger.log(Level.WARNING,
                    "Read settings for \"{0}\" from persistent storage.",
                    appName);
        }
    }

    /**
     * Handle a "save changes" action.
     */
    private void save() {
        if (proposedSettings.areValid() && !proposedSettings.areSaved()) {
            boolean success = proposedSettings.save();
            if (success) {
                String appName = proposedSettings.applicationName();
                logger.log(Level.WARNING,
                        "Wrote settings for \"{0}\" to persistent storage.",
                        appName);
            }
        }
    }
}
