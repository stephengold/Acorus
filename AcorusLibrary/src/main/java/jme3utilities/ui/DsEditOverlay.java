/*
 Copyright (c) 2020-2023, Stephen Gold
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
import com.jme3.math.ColorRGBA;
import com.jme3.system.JmeContext;
import java.awt.DisplayMode;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.RectSizeLimits;

/**
 * The overlay for the display-settings editor.
 * <p>
 * The overlay consists of status lines, one of which is always selected for
 * editing.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DsEditOverlay extends Overlay {
    // *************************************************************************
    // constants and loggers

    /**
     * index of the status line for user feedback
     */
    final private static int feedbackStatusLine = 0;
    /**
     * index of the status line for save status
     */
    final private static int saveStatusLine = 1;
    /**
     * index of the status line for fullscreen mode
     */
    final private static int fullscreenStatusLine = 2;
    /**
     * index of the status line for start location
     */
    final private static int locationStatusLine = 3;
    /**
     * index of the status line for dimensions
     */
    final private static int dimensionsStatusLine = 4;
    /**
     * index of the status line for vSync
     */
    final private static int vSyncStatusLine = 5;
    /**
     * index of the status line for gamma correction
     */
    final private static int gammaCorrectionStatusLine = 6;
    /**
     * index of the status line for color depth
     */
    final private static int colorDepthStatusLine = 7;
    /**
     * index of the status line for multi-sample anti-aliasing (MSAA)
     */
    final private static int msaaStatusLine = 8;
    /**
     * index of the status line for graphics API
     */
    final private static int apiStatusLine = 9;
    /**
     * index of the status line for the graphics-debug flag
     */
    final private static int debugStatusLine = 10;
    /**
     * index of the status line for the graphics-trace flag
     */
    final private static int traceStatusLine = 11;
    /**
     * index of the status line for refresh rate
     */
    final private static int refreshRateStatusLine = 12;
    /**
     * number of status line in the overlay
     */
    final private static int numStatusLines = 13;
    /**
     * message logger for this class
     */
    final static Logger logger
            = Logger.getLogger(DsEditOverlay.class.getName());
    // *************************************************************************
    // fields

    /**
     * proposed display settings: set by constructor
     */
    final private DisplaySettings proposedSettings;
    /**
     * InputMode for this overlay: set by constructor
     */
    final private DsEditInputMode inputMode;
    /**
     * index of the line being edited (&ge;1)
     */
    private int selectedLine = fullscreenStatusLine;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized AppState.
     *
     * @param proposedSettings the proposed display settings (not null, alias
     * created)
     */
    public DsEditOverlay(DisplaySettings proposedSettings) {
        super("dsEditor", 330f, numStatusLines);
        Validate.nonNull(proposedSettings, "proposed settings");

        this.proposedSettings = proposedSettings;
        this.inputMode = new DsEditInputMode(this, proposedSettings);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Advance the field selection by the specified amount.
     *
     * @param amount the number of fields to move downward
     */
    void advanceSelectedField(int amount) {
        // Not all status lines are fields!
        int firstField = fullscreenStatusLine;
        int numFields;
        if (proposedSettings.isFullscreen()) {
            numFields = numStatusLines - firstField;
        } else {
            numFields = numStatusLines - firstField - 1; // skip refresh rate
        }

        int selectedField = selectedLine - firstField;
        int sum = selectedField + amount;
        selectedField = MyMath.modulo(sum, numFields);
        this.selectedLine = selectedField + firstField;
    }

    /**
     * Advance the value of the selected field by the specified amount.
     *
     * @param amount the number of values to advance (may be negative)
     */
    void advanceValue(int amount) {
        switch (selectedLine) {
            case apiStatusLine:
                advanceGraphicsApi(amount);
                break;

            case colorDepthStatusLine:
                advanceColorDepth(amount);
                break;

            case debugStatusLine:
                toggleGraphicsDebugging();
                break;

            case dimensionsStatusLine:
                advanceDimensions(amount);
                break;

            case fullscreenStatusLine:
                toggleFullscreen();
                break;

            case gammaCorrectionStatusLine:
                toggleGammaCorrection();
                break;

            case locationStatusLine:
                toggleCentered();
                break;

            case msaaStatusLine:
                advanceMsaaFactor(amount);
                break;

            case refreshRateStatusLine:
                advanceRefreshRate(amount);
                break;

            case traceStatusLine:
                toggleGraphicsTracing();
                break;

            case vSyncStatusLine:
                toggleVsync();
                break;

            default:
                throw new IllegalStateException("line = " + selectedLine);
        }
    }
    // *************************************************************************
    // Overlay methods

    /**
     * Transition this AppState from terminating to detached. Should be invoked
     * only by a subclass or by the AppStateManager.
     * <p>
     * Invoked once for each time {@link #initialize(com.jme3.app.Application)}
     * is invoked.
     *
     * @param application the application which owns this AppState (not null)
     */
    @Override
    public void cleanup(Application application) {
        getStateManager().detach(inputMode);
        super.cleanup(application);
    }

    /**
     * Initialize this AppState on the first update after it gets attached.
     *
     * @param application application which owns this state (not null)
     */
    @Override
    public void initialize(Application application) {
        super.initialize(application);
        getStateManager().attach(inputMode);
    }

    /**
     * Callback to update this AppState prior to rendering. (Invoked once per
     * frame while the state is attached and enabled.)
     *
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);
        Application application = getApplication();
        JmeContext context = application.getContext();

        String message = "";
        boolean areValid = proposedSettings.areValid();
        if (!areValid) {
            message = proposedSettings.feedbackValid();
        } else if (!proposedSettings.canApply()) {
            message = proposedSettings.feedbackApplicable();
        } else if (!proposedSettings.areApplied()) {
            message = "Changes are ready to be applied.";
        }
        updateStatusLine(feedbackStatusLine, message);

        message = "";
        if (areValid && !proposedSettings.areSaved()) {
            message = "Changes are ready to be saved.";
        }
        updateStatusLine(saveStatusLine, message);

        boolean isFullscreen = proposedSettings.isFullscreen();
        message = "Fullscreen?  " + (isFullscreen ? "yes" : "no");
        updateStatusLine(fullscreenStatusLine, message);

        boolean isCentered = proposedSettings.isCentered();
        if (isCentered || isFullscreen || !DsUtils.hasLwjglVersion3()) {
            message = "Location:  centered";
        } else {
            int x = context.getWindowXPosition();
            int y = context.getWindowYPosition();
            proposedSettings.setStartLocation(x, y);
            message = "Location:  (" + x + ", " + y + ")";
        }
        updateStatusLine(locationStatusLine, message);

        int width = proposedSettings.width();
        int height = proposedSettings.height();
        message = "Dimensions:  " + DsUtils.describeDimensions(width, height);
        updateStatusLine(dimensionsStatusLine, message);

        boolean isVsync = proposedSettings.isVSync();
        message = "VSync?  " + (isVsync ? "yes" : "no");
        updateStatusLine(vSyncStatusLine, message);

        boolean isGammaCorrection = proposedSettings.isGammaCorrection();
        message = "Gamma correction?  " + (isGammaCorrection ? "yes" : "no");
        updateStatusLine(gammaCorrectionStatusLine, message);

        int colorDepth = proposedSettings.colorDepth();
        if (colorDepth <= 0) {
            message = "Color depth:  any/unknown";
        } else {
            message = String.format("Color depth:  %d bpp", colorDepth);
        }
        updateStatusLine(colorDepthStatusLine, message);

        int msaaFactor = proposedSettings.msaaFactor();
        message = "MSAA factor:  " + DsUtils.describeMsaaFactor(msaaFactor);
        updateStatusLine(msaaStatusLine, message);

        String api = proposedSettings.graphicsApi();
        message = "Graphics API:  " + api;
        updateStatusLine(apiStatusLine, message);

        boolean isDebug = proposedSettings.isGraphicsDebug();
        message = "Debug graphics?  " + (isDebug ? "yes" : "no");
        updateStatusLine(debugStatusLine, message);

        boolean isTrace = proposedSettings.isGraphicsTrace();
        message = "Trace graphics?  " + (isTrace ? "yes" : "no");
        updateStatusLine(traceStatusLine, message);

        message = "";
        if (isFullscreen) {
            int refreshRate = proposedSettings.refreshRate();
            if (refreshRate <= 0) {
                message = "Refresh rate:  any/unknown";
            } else {
                message = String.format("Refresh rate:  %d Hz", refreshRate);
            }
        }
        updateStatusLine(refreshRateStatusLine, message);
    }
    // *************************************************************************
    // private methods

    /**
     * Advance the color-depth selection by the specified amount.
     *
     * @param amount the number of values to advance (may be negative)
     */
    private void advanceColorDepth(int amount) {
        Collection<Integer> depthSet = new TreeSet<>();
        if (proposedSettings.isFullscreen()) {
            Iterable<DisplayMode> modes = DsUtils.listDisplayModes();
            int height = proposedSettings.height();
            int width = proposedSettings.width();

            for (DisplayMode mode : modes) {
                int modeDepth = mode.getBitDepth();
                if (mode.getHeight() == height
                        && mode.getWidth() == width) {
                    depthSet.add(modeDepth);
                }
            }
            if (depthSet.isEmpty()) {
                for (DisplayMode mode : modes) {
                    int modeDepth = mode.getBitDepth();
                    depthSet.add(modeDepth);
                }
            }
        }
        if (depthSet.isEmpty()) {
            depthSet.add(24);
        }
        int[] depthArray = new int[depthSet.size()];
        int index = 0;
        for (int depth : depthSet) {
            depthArray[index] = depth;
            ++index;
        }

        int depth = proposedSettings.colorDepth();
        depth = AcorusDemo.advanceInt(depthArray, depth, amount);
        proposedSettings.setColorDepth(depth);
    }

    /**
     * Advance the display dimensions by the specified amount.
     *
     * @param amount the number of values to advance (may be negative)
     */
    private void advanceDimensions(int amount) {
        Collection<String> descriptionSet = new TreeSet<>();
        Iterable<DisplayMode> modes = DsUtils.listDisplayModes();
        int depth = proposedSettings.colorDepth();
        int rate = proposedSettings.refreshRate();
        RectSizeLimits sizeLimits = proposedSettings.getSizeLimits();

        // Enumerate the most relevant display sizes.
        for (DisplayMode mode : modes) {
            int modeDepth = mode.getBitDepth();
            if (modeDepth <= 0 || depth <= 0 || modeDepth == depth) {

                int modeRate = mode.getRefreshRate();
                if (modeRate <= 0 || rate <= 0 || modeRate == rate) {
                    int height = mode.getHeight();
                    int width = mode.getWidth();
                    if (sizeLimits.isInRange(width, height)) {
                        String desc = DsUtils.describeDimensions(width, height);
                        descriptionSet.add(desc);
                    }
                }
            }
        }
        if (descriptionSet.isEmpty()) {
            for (DisplayMode mode : modes) {
                int height = mode.getHeight();
                int width = mode.getWidth();
                if (sizeLimits.isInRange(width, height)) {
                    String desc = DsUtils.describeDimensions(width, height);
                    descriptionSet.add(desc);
                }
            }
        }

        String[] descriptionArray = new String[descriptionSet.size()];
        int index = 0;
        for (String description : descriptionSet) {
            descriptionArray[index] = description;
            ++index;
        }

        int height = proposedSettings.height();
        int width = proposedSettings.width();
        String description = DsUtils.describeDimensions(width, height);
        description = AcorusDemo.advanceString(
                descriptionArray, description, amount);

        int[] wh = DsUtils.parseDimensions(description);
        proposedSettings.setDimensions(wh[0], wh[1]);
    }

    /**
     * Advance the graphics API by the specified amount.
     *
     * @param amount the number of values to advance (may be negative)
     */
    private void advanceGraphicsApi(int amount) {
        String[] values = DisplaySettings.listGraphicsApis();
        String api = proposedSettings.graphicsApi();

        api = AcorusDemo.advanceString(values, api, amount);
        proposedSettings.setGraphicsApi(api);
    }

    /**
     * Advance the MSAA factor by the specified amount.
     *
     * @param amount the number of values to advance (may be negative)
     */
    private void advanceMsaaFactor(int amount) {
        int factor = proposedSettings.msaaFactor();
        factor += amount;
        if (factor < 1) {
            factor = 1;
        }
        proposedSettings.setMsaaFactor(factor);
    }

    /**
     * Advance the refresh rate by the specified amount.
     *
     * @param amount the number of values to advance (may be negative)
     */
    private void advanceRefreshRate(int amount) {
        assert proposedSettings.isFullscreen();

        Iterable<DisplayMode> modes = DsUtils.listDisplayModes();
        int height = proposedSettings.height();
        int width = proposedSettings.width();

        // Enumerate the most relevant refresh rates.
        Collection<Integer> rateSet = new TreeSet<>();
        for (DisplayMode mode : modes) {
            if (mode.getHeight() == height && mode.getWidth() == width) {
                int rate = mode.getRefreshRate();
                rateSet.add(rate);
            }
        }
        if (rateSet.isEmpty()) {
            for (DisplayMode mode : modes) {
                int rate = mode.getRefreshRate();
                rateSet.add(rate);
            }
        }
        int[] rateArray = new int[rateSet.size()];
        int index = 0;
        for (int rate : rateSet) {
            rateArray[index] = rate;
            ++index;
        }

        int rate = proposedSettings.refreshRate();
        rate = AcorusDemo.advanceInt(rateArray, rate, amount);
        proposedSettings.setRefreshRate(rate);
    }

    /**
     * Toggle center-on-start between enabled and disabled.
     */
    private void toggleCentered() {
        boolean isFullscreen = proposedSettings.isFullscreen();
        if (isFullscreen || !DsUtils.hasLwjglVersion3()) {
            return;
        }

        boolean isCentered = proposedSettings.isCentered();
        if (isCentered) { // switch to specifying screen coordinates
            Application legacyApp = getApplication();
            JmeContext context = legacyApp.getContext();
            int x = context.getWindowXPosition();
            int y = context.getWindowYPosition();
            proposedSettings.setStartLocation(x, y);
        }
        proposedSettings.setCentered(!isCentered);
    }

    /**
     * Toggle fullscreen between enabled and disabled.
     */
    private void toggleFullscreen() {
        int rate;
        int depth;

        boolean isFullscreen = proposedSettings.isFullscreen();
        if (isFullscreen) { // switch to windowed mode
            rate = DisplayMode.REFRESH_RATE_UNKNOWN;
            depth = DisplayMode.BIT_DEPTH_MULTI;
            proposedSettings.scaleSize(0.8f, 0.8f);

        } else { // switch to full-screen mode
            DisplayMode mode = DsUtils.displayMode();
            rate = mode.getRefreshRate();
            if (rate <= 0) {
                rate = 60;
            }
            depth = mode.getBitDepth();
            if (depth <= 0) {
                depth = 24;
            }
            RectSizeLimits limits = proposedSettings.getSizeLimits();
            int width = mode.getWidth();
            int height = mode.getHeight();
            if (limits.isInRange(width, height)) {
                proposedSettings.setDimensions(width, height);
            }
        }

        proposedSettings.setRefreshRate(rate);
        proposedSettings.setColorDepth(depth);
        proposedSettings.setFullscreen(!isFullscreen);
    }

    /**
     * Toggle between gamma correction enabled/disabled.
     */
    private void toggleGammaCorrection() {
        boolean enabled = proposedSettings.isGammaCorrection();
        proposedSettings.setGammaCorrection(!enabled);
    }

    /**
     * Toggle between graphics debug enabled/disabled.
     */
    private void toggleGraphicsDebugging() {
        boolean enabled = proposedSettings.isGraphicsDebug();
        proposedSettings.setGraphicsDebug(!enabled);
    }

    /**
     * Toggle between graphics trace enabled/disabled.
     */
    private void toggleGraphicsTracing() {
        boolean enabled = proposedSettings.isGraphicsTrace();
        proposedSettings.setGraphicsTrace(!enabled);
    }

    /**
     * Toggle between Vsync enabled/disabled.
     */
    private void toggleVsync() {
        boolean enabled = proposedSettings.isVSync();
        proposedSettings.setVSync(!enabled);
    }

    /**
     * Update the indexed status line.
     *
     * @param lineIndex which line to update (&ge;0)
     * @param text the text to display (not null)
     */
    private void updateStatusLine(int lineIndex, String text) {
        if (lineIndex == selectedLine) {
            setText(lineIndex, "--> " + text, ColorRGBA.Yellow);
        } else {
            setText(lineIndex, text, ColorRGBA.White);
        }
    }
}
