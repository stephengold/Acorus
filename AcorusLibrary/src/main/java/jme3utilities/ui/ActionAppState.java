/*
 Copyright (c) 2014-2022, Stephen Gold
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
import com.jme3.app.state.AppStateManager;
import java.util.logging.Logger;
import jme3utilities.InitialState;
import jme3utilities.SimpleAppState;

/**
 * A SimpleAppState in an ActionApplication.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ActionAppState extends SimpleAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ActionAppState.class.getName());
    // *************************************************************************
    // constructor

    /**
     * Instantiate an uninitialized state.
     *
     * @param enabled true &rarr; enabled, false &rarr; disabled
     */
    public ActionAppState(boolean enabled) {
        super(enabled);
    }

    /**
     * Instantiate an uninitialized state.
     *
     * @param initialState Enabled or Disabled (null means disabled)
     */
    public ActionAppState(InitialState initialState) {
        super(initialState);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Access the application. Allowed only if the AppState has been
     * initialized.
     *
     * @return the pre-existing instance (not null)
     */
    public ActionApplication getActionApplication() {
        assert simpleApplication != null;
        return (ActionApplication) simpleApplication;
    }

    /**
     * Access the user-interface signals. Allowed only if the AppState has been
     * initialized.
     *
     * @return the pre-existing instance (not null)
     */
    public Signals getSignals() {
        Signals result = getActionApplication().getSignals();
        return result;
    }

    /**
     * Return the effective speed of physics and animations. Allowed only if the
     * AppState has been initialized.
     *
     * @return the speed (&gt;0, standard speed &rarr; 1)
     */
    public float speed() {
        float result = getActionApplication().getSpeed();
        return result;
    }
    // *************************************************************************
    // SimpleAppState methods

    /**
     * Initialize this AppState on the first update after it gets attached.
     *
     * @param sm application's state manager (not null)
     * @param app application which owns this state (not null)
     */
    @Override
    public void initialize(AppStateManager sm, Application app) {
        if (!(app instanceof ActionApplication)) {
            throw new IllegalArgumentException(
                    "application should be an ActionApplication");
        }

        super.initialize(sm, app);
    }
}
