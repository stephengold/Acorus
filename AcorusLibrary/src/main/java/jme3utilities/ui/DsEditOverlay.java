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
package jme3utilities.ui;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.system.JmeContext;
import java.awt.DisplayMode;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import jme3utilities.InitialState;
import jme3utilities.SimpleAppState;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.RectSizeLimits;

/**
 * The overlay for the display-settings editor.
 * <p>
 * The overlay consists of status lines, one of which is selected for editing.
 * The overlay appears in the upper-left portion of the display.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DsEditOverlay extends SimpleAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * vertical separation between status lines (in pixels)
     */
    final private static float lineSpacing = 20f;
    /**
     * index of the status line for user feedback
     */
    final private static int feedbackStatusLine = 0;
    /**
     * index of the status line for full screen
     */
    final private static int fullScreenStatusLine = 2;
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
     * index of the status line for refresh rate
     */
    final private static int refreshRateStatusLine = 9;
    /**
     * number of lines of text in the overlay
     */
    final private static int numStatusLines = 10;
    /**
     * message logger for this class
     */
    final static Logger logger
            = Logger.getLogger(DsEditOverlay.class.getName());
    // *************************************************************************
    // fields

    /**
     * lines of text displayed in the upper-left corner of the GUI node ([0] is
     * the top line)
     */
    final private BitmapText[] statusLines = new BitmapText[numStatusLines];
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
    private int selectedLine = fullScreenStatusLine;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized AppState.
     *
     * @param proposedSettings the proposed display settings (not null, alias
     * created)
     */
    public DsEditOverlay(DisplaySettings proposedSettings) {
        super(InitialState.Disabled);
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
        /*
         * Not all status lines are fields!
         */
        int firstField = fullScreenStatusLine;
        int numFields;
        if (proposedSettings.isFullscreen()) {
            numFields = numStatusLines - firstField;
        } else {
            numFields = numStatusLines - firstField - 1; // skip refresh rate
        }

        int selectedField = selectedLine - firstField;
        int sum = selectedField + amount;
        selectedField = MyMath.modulo(sum, numFields);
        selectedLine = selectedField + firstField;
    }

    /**
     * Advance the value of the selected field by the specified amount.
     *
     * @param amount
     */
    void advanceValue(int amount) {
        switch (selectedLine) {
            case colorDepthStatusLine:
                advanceColorDepth(amount);
                break;

            case dimensionsStatusLine:
                advanceDimensions(amount);
                break;

            case fullScreenStatusLine:
                toggleFullScreen();
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

            case vSyncStatusLine:
                toggleVsync();
                break;

            default:
                throw new IllegalStateException("line = " + selectedLine);
        }
    }

    /**
     * Callback when the dimensions of the viewport have changed.
     *
     * @param width the new width (in pixels, &gt;0)
     * @param height the new height (in pixels; &gt;0)
     */
    public void resize(int width, int height) {
        Validate.positive(width, "width");
        Validate.positive(height, "height");
        /*
         * Re-position the status lines.
         */
        for (int lineIndex = 0; lineIndex < numStatusLines; ++lineIndex) {
            float y = height - lineSpacing * lineIndex;
            BitmapText line = statusLines[lineIndex];
            if (line != null) {
                line.setLocalTranslation(0f, y, 0f);
            }
        }
    }
    // *************************************************************************
    // new protected methods

    /**
     * Disable this overlay, assuming it is initialized and enabled.
     */
    protected void disable() {
        assert isInitialized();
        assert isEnabled();
        /*
         * Remove the status lines from the GUI node.
         */
        for (int i = 0; i < numStatusLines; ++i) {
            statusLines[i].removeFromParent();
        }

        super.setEnabled(false);
    }

    /**
     * Enable this overlay, assuming it is initialized and disabled.
     */
    protected void enable() {
        assert isInitialized();
        assert !isEnabled();

        super.setEnabled(true);
        /*
         * Add the status lines to the guiNode.
         */
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        float height = guiViewPort.getCamera().getHeight();
        for (int lineIndex = 0; lineIndex < numStatusLines; ++lineIndex) {
            statusLines[lineIndex] = new BitmapText(font);
            float y = height - lineSpacing * lineIndex;
            statusLines[lineIndex].setLocalTranslation(0f, y, 0f);
            guiNode.attachChild(statusLines[lineIndex]);
        }
    }
    // *************************************************************************
    // SimpleAppState methods

    /**
     * Clean up this AppState during the first update after it gets detached.
     * Should be invoked only by a subclass or by the AppStateManager.
     */
    @Override
    public void cleanup() {
        if (isEnabled()) {
            disable();
        }
        stateManager.detach(inputMode);

        super.cleanup();
    }

    /**
     * Initialize this AppState on the first update after it gets attached.
     *
     * @param stateManager application's state manager (not null)
     * @param application application which owns this state (not null)
     */
    @Override
    public void initialize(AppStateManager stateManager,
            Application application) {
        super.initialize(stateManager, application);
        stateManager.attach(inputMode);

        if (isEnabled()) {
            enable();
        }
    }

    /**
     * Enable or disable this overlay.
     *
     * @param newSetting true &rarr; enable, false &rarr; disable
     */
    @Override
    final public void setEnabled(boolean newSetting) {
        if (newSetting && !isEnabled()) {
            enable();
        } else if (!newSetting && isEnabled()) {
            disable();
        }
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

        String message = "";
        if (!proposedSettings.areValid()) {
            message = proposedSettings.feedbackValid();
        } else if (!proposedSettings.canApply()) {
            message = proposedSettings.feedbackApplicable();
        } else if (!proposedSettings.areApplied()) {
            message = "There are unapplied changes.";
        } else if (!proposedSettings.areSaved()) {
            message = "There are unsaved changes.";
        }
        updateStatusLine(feedbackStatusLine, message);

        boolean isFullScreen = proposedSettings.isFullscreen();
        message = "Full screen?  " + (isFullScreen ? "yes" : "no");
        updateStatusLine(fullScreenStatusLine, message);

        boolean isCentered = proposedSettings.isCentered();
        if (isCentered || isFullScreen || !DsUtils.isLWJGLv3()) {
            message = "Location:  centered";
        } else {
            JmeContext context = simpleApplication.getContext();
            int x = DsUtils.getWindowXPosition(context);
            int y = DsUtils.getWindowYPosition(context);
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

        message = "";
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

        message = "";
        if (isFullScreen) {
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
        Set<Integer> depthSet = new TreeSet<>();
        if (proposedSettings.isFullscreen()) {
            Iterable<DisplayMode> modes = DsUtils.getDisplayModes();
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
            depthSet.add(DisplayMode.BIT_DEPTH_MULTI);
        }
        int[] depthArray = new int[depthSet.size()];
        int index = 0;
        for (int depth : depthSet) {
            depthArray[index] = depth;
            ++index;
        }

        int depth = proposedSettings.colorDepth();
        depth = AbstractDemo.advanceInt(depthArray, depth, amount);
        proposedSettings.setColorDepth(depth);
    }

    /**
     * Advance the display dimensions by the specified amount.
     *
     * @param amount the number of values to advance (may be negative)
     */
    private void advanceDimensions(int amount) {
        Set<String> descriptionSet = new TreeSet<>();
        Iterable<DisplayMode> modes = DsUtils.getDisplayModes();
        int depth = proposedSettings.colorDepth();
        int rate = proposedSettings.refreshRate();
        RectSizeLimits sizeLimits = proposedSettings.getSizeLimits();
        /*
         * Enumerate the most relevant display sizes.
         */
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
        description = AbstractDemo.advanceString(
                descriptionArray, description, amount);

        int[] wh = DsUtils.parseDisplaySize(description);
        proposedSettings.setDimensions(wh[0], wh[1]);
    }

    /**
     * Advance the MSAA factor by the specified amount.
     *
     * @param amount the number of values to advance (may be negative)
     */
    private void advanceMsaaFactor(int amount) {
        int[] values = DsUtils.getMsaaFactors();
        int factor = proposedSettings.msaaFactor();
        factor = AbstractDemo.advanceInt(values, factor, amount);
        proposedSettings.setMsaaFactor(factor);
    }

    /**
     * Advance the refresh rate by the specified amount.
     *
     * @param amount the number of values to advance (may be negative)
     */
    private void advanceRefreshRate(int amount) {
        assert proposedSettings.isFullscreen();

        Iterable<DisplayMode> modes = DsUtils.getDisplayModes();
        int height = proposedSettings.height();
        int width = proposedSettings.width();
        /*
         * Enumerate the most relevant refresh rates.
         */
        Set<Integer> rateSet = new TreeSet<>();
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
        rate = AbstractDemo.advanceInt(rateArray, rate, amount);
        proposedSettings.setRefreshRate(rate);
    }

    /**
     * Toggle center-on-start between enabled and disabled.
     */
    private void toggleCentered() {
        boolean isFullScreen = proposedSettings.isFullscreen();
        if (isFullScreen || !DsUtils.isLWJGLv3()) {
            return;
        }

        boolean isCentered = proposedSettings.isCentered();
        if (isCentered) { // switch to specifying screen coordinates
            JmeContext context = simpleApplication.getContext();
            int x = DsUtils.getWindowXPosition(context);
            int y = DsUtils.getWindowYPosition(context);
            proposedSettings.setStartLocation(x, y);
        }
        proposedSettings.setCentered(!isCentered);
    }

    /**
     * Toggle fullscreen between enabled and disabled.
     */
    private void toggleFullScreen() {
        int rate;
        int depth;

        boolean isFullScreen = proposedSettings.isFullscreen();
        if (isFullScreen) { // switch to windowed mode
            rate = DisplayMode.REFRESH_RATE_UNKNOWN;
            depth = DisplayMode.BIT_DEPTH_MULTI;
            proposedSettings.scaleSize(0.8f, 0.8f);

        } else { // switch to full-screen mode
            DisplayMode mode = DsUtils.getDisplayMode();
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
        proposedSettings.setFullscreen(!isFullScreen);
    }

    /**
     * Toggle between gamma correction enabled/disabled.
     */
    private void toggleGammaCorrection() {
        boolean enabled = proposedSettings.isGammaCorrection();
        proposedSettings.setGammaCorrection(!enabled);
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
     */
    private void updateStatusLine(int lineIndex, String text) {
        BitmapText spatial = statusLines[lineIndex];

        if (lineIndex == selectedLine) {
            spatial.setColor(ColorRGBA.Yellow.clone());
            spatial.setText("--> " + text);
        } else {
            spatial.setColor(ColorRGBA.White.clone());
            spatial.setText(" " + text);
        }
    }
}
