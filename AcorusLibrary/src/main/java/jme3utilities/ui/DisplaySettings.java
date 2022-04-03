/*
 Copyright (c) 2017-2022, Stephen Gold
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

import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import java.awt.DisplayMode;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jme3utilities.Validate;
import jme3utilities.math.RectSizeLimits;

/**
 * Manage an application's render settings.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DisplaySettings {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DisplaySettings.class.getName());
    // *************************************************************************
    // fields

    /**
     * application instance (not null)
     */
    final private ActionApplication application;
    /**
     * cached settings that can be applied (to the application context) or saved
     * (written to persistent storage)
     */
    final private AppSettings cachedSettings = new AppSettings(true);
    /**
     * true&rarr;settings have been applied since their last modification,
     * otherwise false
     */
    private boolean areApplied = true;
    /**
     * true&rarr;settings have been saved since their last modification,
     * otherwise false
     */
    private boolean areSaved = false;
    /**
     * map graphics API names to AppSettings.getRenderer() values
     */
    final private static Map<String, String> apiNameMap = new TreeMap<>();

    static {
        apiNameMap.put("OpenGL 3.2", AppSettings.LWJGL_OPENGL32);
        apiNameMap.put("OpenGL 3.3", AppSettings.LWJGL_OPENGL33);
        apiNameMap.put("OpenGL 4.0", AppSettings.LWJGL_OPENGL40);
        apiNameMap.put("OpenGL 4.1", AppSettings.LWJGL_OPENGL41);
        apiNameMap.put("OpenGL 4.2", AppSettings.LWJGL_OPENGL42);
        apiNameMap.put("OpenGL 4.3", AppSettings.LWJGL_OPENGL43);
        apiNameMap.put("OpenGL 4.4", AppSettings.LWJGL_OPENGL44);
        apiNameMap.put("OpenGL 4.5", AppSettings.LWJGL_OPENGL45);
    }
    /**
     * framebuffer-size limits (not null)
     */
    final private RectSizeLimits sizeLimits;
    /**
     * how often the JME settings dialog should be shown during initialize()
     */
    private ShowDialog showDialog = ShowDialog.FirstTime;
    /**
     * the name of the application, which is the key for loading/saving app
     * settings from Java's user preferences and also (by default) displayed in
     * the application's title bar (not null)
     */
    final private String applicationName;
    // *************************************************************************
    // constructors

    /**
     * Instantiate settings for the specified ActionApplication.
     *
     * @param app the current application instance (not null, alias created)
     * @param appName the name of the application (not null)
     * @param sizeLimits the desired framebuffer-size limits (not null, alias
     * created)
     */
    public DisplaySettings(ActionApplication app, String appName,
            RectSizeLimits sizeLimits) {
        Validate.nonNull(app, "application");
        Validate.nonNull(appName, "application name");
        Validate.nonNull(sizeLimits, "size limits");

        this.application = app;
        this.applicationName = appName;
        this.sizeLimits = sizeLimits;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the name of the application whose settings are being edited.
     *
     * @return the name (not null)
     */
    public String applicationName() {
        assert applicationName != null;
        return applicationName;
    }

    /**
     * Apply the cached settings to the graphics context and restart the context
     * to put them into effect.
     */
    public void applyToContext() {
        assert canApply();
        assert !areApplied;

        AppSettings clone = new AppSettings(false);
        clone.copyFrom(cachedSettings);

        application.setSettings(clone);
        application.restart();
        areApplied = true;
    }

    /**
     * Test whether the cached settings have been applied since their last
     * modification.
     *
     * @return true if clean, otherwise false
     */
    public boolean areApplied() {
        return areApplied;
    }

    /**
     * Test whether the cached settings have been saved (written to persistent
     * storage) since their last modification.
     *
     * @return true if clean, otherwise false
     */
    public boolean areSaved() {
        return areSaved;
    }

    /**
     * Test the validity of the cached settings prior to a save.
     *
     * @return true if good enough, otherwise false
     */
    public boolean areValid() {
        int height = cachedSettings.getHeight();
        int width = cachedSettings.getWidth();
        if (!sizeLimits.isInRange(width, height)) {
            return false;
        }

        if (cachedSettings.isFullscreen()) {
            // assuming that the device supports full-screen exclusive mode
            boolean foundMatch = matchesAvailableDisplayMode();
            return foundMatch;
        }

        return true;
    }

    /**
     * Test whether the cached settings can be applied immediately.
     *
     * @return true if they can be applied, otherwise false
     */
    public boolean canApply() {
        boolean result = areValid();
        return result;
    }

    /**
     * Determine the color depth.
     *
     * @return depth (in bits per pixel) or &le;0 for unknown/don't care
     */
    public int colorDepth() {
        int result = cachedSettings.getBitsPerPixel();
        return result;
    }

    /**
     * Explain why these settings cannot be applied.
     *
     * @return message text (not null)
     */
    public String feedbackApplicable() {
        return "";
    }

    /**
     * Explain why the cached settings are invalid.
     *
     * @return message text (not null)
     */
    public String feedbackValid() {
        int height = cachedSettings.getHeight();
        int width = cachedSettings.getWidth();
        if (!sizeLimits.isInRange(width, height)) {
            return sizeLimits.feedbackInRange(width, height);
        }

        if (cachedSettings.isFullscreen()) {
            // assuming that the device supports full-screen exclusive mode
            boolean foundMatch = matchesAvailableDisplayMode();
            if (!foundMatch) {
                return "No matching mode for device.";
            }
        }

        return "";
    }

    /**
     * Access the max/min framebuffer size.
     *
     * @return the pre-existing instance (not null)
     */
    public RectSizeLimits getSizeLimits() {
        return sizeLimits;
    }

    /**
     * Return the name of the graphics API.
     *
     * @return a name found in {@code listGraphicsApis()} (not null, not empty)
     */
    public String graphicsAPI() {
        String result;
        String value = cachedSettings.getRenderer();
        for (Map.Entry<String, String> entry : apiNameMap.entrySet()) {
            if (entry.getValue().equals(value)) {
                result = entry.getKey();
                return result;
            }
        }
        throw new RuntimeException("value = " + value);
    }

    /**
     * Determine the display height.
     *
     * @return height (in pixels, &gt;0)
     */
    public int height() {
        int result = cachedSettings.getHeight();
        assert result > 0 : result;
        return result;
    }

    /**
     * Initialize the settings before the application starts.
     *
     * @return a new AppSettings instance, or null if user clicked on the
     * "Cancel" button
     */
    public AppSettings initialize() {
        /*
         * Attempt to load settings from user preferences (persistent storage).
         */
        boolean loadedFromStore = false;
        try {
            if (Preferences.userRoot().nodeExists(applicationName)) {
                cachedSettings.load(applicationName);
                loadedFromStore = true;
            }
        } catch (BackingStoreException e) {
        }
        /*
         * Apply overrides to the loaded settings.
         */
        applyOverrides(cachedSettings);

        boolean show;
        switch (showDialog) {
            case Always:
                show = true;
                break;

            case FirstTime:
                show = !loadedFromStore;
                break;

            case Never:
                show = false;
                break;

            default:
                String message = "dialogOption = " + showDialog;
                throw new IllegalStateException(message);
        }

        if (show) {
            /*
             * Show JME's settings dialog.
             */
            boolean loadFlag = false;
            boolean proceed
                    = JmeSystem.showSettingsDialog(cachedSettings, loadFlag);
            if (!proceed) {
                /*
                 * The user clicked on the "Cancel" button.
                 */
                return null;
            }
        }

        if (areValid()) {
            save();
        }

        AppSettings clone = new AppSettings(false);
        clone.copyFrom(cachedSettings);

        return clone;
    }

    /**
     * Test whether center-on-start is enabled for LWJGL v3 windowed mode.
     *
     * @return true if centering, otherwise false
     */
    public boolean isCentered() {
        boolean result = cachedSettings.getCenterWindow();
        return result;
    }

    /**
     * Test whether full-screen mode is enabled.
     *
     * @return true if full-screen, otherwise false
     */
    public boolean isFullscreen() {
        boolean result = cachedSettings.isFullscreen();
        return result;
    }

    /**
     * Test whether gamma correction is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isGammaCorrection() {
        boolean result = cachedSettings.isGammaCorrection();
        return result;
    }

    /**
     * Test whether graphics debugging is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isGraphicsDebug() {
        boolean result = cachedSettings.isGraphicsDebug();
        return result;
    }

    /**
     * Test whether graphics tracing is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isGraphicsTrace() {
        boolean result = cachedSettings.isGraphicsTrace();
        return result;
    }

    /**
     * Test whether VSync is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isVSync() {
        boolean result = cachedSettings.isVSync();
        return result;
    }

    /**
     * Enumerate all known graphics APIs.
     *
     * @return a new array (not null)
     */
    public static String[] listGraphicsApis() {
        int numApis = apiNameMap.size();
        String[] result = new String[numApis];
        int i = 0;
        for (String name : apiNameMap.keySet()) {
            result[i] = name;
            ++i;
        }

        return result;
    }

    /**
     * Determine the sampling factor for multi-sample anti-aliasing (MSAA).
     *
     * @return sampling factor (in samples per pixel, &ge;0)
     */
    public int msaaFactor() {
        int result = cachedSettings.getSamples();
        assert result >= 0 : result;
        return result;
    }

    /**
     * Determine the display's refresh rate, which is relevant only to
     * full-screen displays.
     *
     * @return frequency (in Hertz) or &le;0 for unknown/don't care
     */
    public int refreshRate() {
        int result = cachedSettings.getFrequency();
        return result;
    }

    /**
     * Update the dimensions of a resizable viewport, assuming the change has
     * already been applied.
     *
     * @see #setDimensions(int, int)
     *
     * @param newWidth (in pixels, &gt;0)
     * @param newHeight (in pixels, &gt;0)
     */
    public void resize(int newWidth, int newHeight) {
        int oldWidth = width();
        if (newWidth != oldWidth) {
            cachedSettings.setWidth(newWidth);
            areSaved = false;
        }

        int oldHeight = height();
        if (newHeight != oldHeight) {
            cachedSettings.setHeight(newHeight);
            areSaved = false;
        }
    }

    /**
     * Write the cached settings to persistent storage, so they will take effect
     * the next time the application is launched.
     */
    public void save() {
        try {
            cachedSettings.save(applicationName);
            areSaved = true;
        } catch (BackingStoreException e) {
            String message = "Display settings were not saved.";
            logger.warning(message);
        }
    }

    /**
     * Scale the height and width of the display by the specified factors and
     * clamp to the size limits.
     *
     * @param widthFactor scaling factor for the display width (&gt;0)
     * @param heightFactor scaling factor for the display height (&gt;0)
     */
    public void scaleSize(float widthFactor, float heightFactor) {
        Validate.positive(widthFactor, "width factor");
        Validate.positive(heightFactor, "height factor");

        int newWidth = width();
        newWidth = Math.round(widthFactor * newWidth);
        newWidth = sizeLimits.clampWidth(newWidth);

        int newHeight = height();
        newHeight = Math.round(widthFactor * newHeight);
        newHeight = sizeLimits.clampHeight(newHeight);

        setDimensions(newWidth, newHeight);
    }

    /**
     * Alter whether center-on-start is enabled for LWJGL v3 windowed mode.
     *
     * @param newSetting true for centering, false to specify screen coordinates
     */
    public void setCentered(boolean newSetting) {
        boolean oldSetting = isCentered();
        if (newSetting != oldSetting) {
            cachedSettings.setCenterWindow(newSetting);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Alter the color depth.
     *
     * @param newDepth color depth (in bits per pixel) or &le;0 for
     * unknown/don't care
     */
    public void setColorDepth(int newDepth) {
        int oldBpp = colorDepth();
        if (newDepth != oldBpp) {
            cachedSettings.setBitsPerPixel(newDepth);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Alter the display dimensions.
     *
     * @param newWidth width (in pixels, &ge;minWidth, &le;maxWidth)
     * @param newHeight height (in pixels, &ge;minHeight, &le;maxHeight)
     */
    public void setDimensions(int newWidth, int newHeight) {
        assert sizeLimits.isInRange(newWidth, newHeight);

        int oldWidth = width();
        if (newWidth != oldWidth) {
            cachedSettings.setWidth(newWidth);
            areApplied = false;
            areSaved = false;
        }
        int oldHeight = height();
        if (newHeight != oldHeight) {
            cachedSettings.setHeight(newHeight);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Enable or disable full-screen mode.
     *
     * @param newSetting true&rarr;full screen, false&rarr; windowed
     */
    public void setFullscreen(boolean newSetting) {
        boolean oldSetting = isFullscreen();
        if (newSetting != oldSetting) {
            cachedSettings.setFullscreen(newSetting);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Enable or disable gamma-correction mode.
     *
     * @param newSetting true&rarr;enable correction, false&rarr; disable it
     */
    public void setGammaCorrection(boolean newSetting) {
        boolean oldSetting = isGammaCorrection();
        if (newSetting != oldSetting) {
            cachedSettings.setGammaCorrection(newSetting);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Alter the graphics API.
     *
     * @param apiName a name found in {@code listGraphicsApis()} (not null, not
     * empty, found in {@code apiNameMap})
     */
    public void setGraphicsApi(String apiName) {
        Validate.require(apiNameMap.containsKey(apiName), "be a known API");

        String newSetting = apiNameMap.get(apiName);
        String oldSetting = graphicsAPI();
        if (!newSetting.equals(oldSetting)) {
            cachedSettings.setRenderer(newSetting);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Alter the graphics-debug flag.
     *
     * @param newSetting true to enable debugging, false to disable it
     */
    public void setGraphicsDebug(boolean newSetting) {
        boolean oldSetting = isGraphicsDebug();
        if (newSetting != oldSetting) {
            cachedSettings.setGraphicsDebug(newSetting);
            // after init, applying has no effect
            areSaved = false;
        }
    }

    /**
     * Alter the graphics-trace flag.
     *
     * @param newSetting true to enable tracing, false to disable it
     */
    public void setGraphicsTrace(boolean newSetting) {
        boolean oldSetting = isGraphicsTrace();
        if (newSetting != oldSetting) {
            cachedSettings.setGraphicsTrace(newSetting);
            // after init, applying has no effect
            areSaved = false;
        }
    }

    /**
     * Maximize the display dimensions.
     */
    public void setMaxSize() {
        setDimensions(sizeLimits.maxWidth, sizeLimits.maxHeight);
    }

    /**
     * Minimize the display dimensions.
     */
    public void setMinSize() {
        setDimensions(sizeLimits.minWidth, sizeLimits.minHeight);
    }

    /**
     * Alter the sampling factor for multi-sample anti-aliasing (MSAA).
     *
     * @param newFactor number of samples per pixel (&ge;1, &le;16)
     */
    public void setMsaaFactor(int newFactor) {
        Validate.inRange(newFactor, "new factor", 1, 16);

        int oldFactor = msaaFactor();
        if (newFactor != oldFactor) {
            cachedSettings.setSamples(newFactor);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Alter the refresh rate.
     *
     * @param newRate frequency (in Hertz) or &le;0 for unknown/don't care
     */
    public void setRefreshRate(int newRate) {
        int oldRate = refreshRate();
        if (newRate != oldRate) {
            cachedSettings.setFrequency(newRate);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Alter how often the JME settings dialog should be shown during
     * initialize().
     *
     * @param newSetting the desired option (not null)
     */
    public void setShowDialog(ShowDialog newSetting) {
        Validate.nonNull(newSetting, "new setting");
        this.showDialog = newSetting;
    }

    /**
     * Alter the initial location for LWJGL v3 windowed mode with
     * setCentered(false).
     *
     * @param newX screen X coordinate for the left edge of the content area
     * @param newY screen Y coordinate for the top edge of the content area
     */
    public void setStartLocation(int newX, int newY) {
        int oldX = startX();
        if (newX != oldX) {
            cachedSettings.setWindowXPosition(newX);
            areApplied = false;
            areSaved = false;
        }

        int oldY = startY();
        if (newY != oldY) {
            cachedSettings.setWindowYPosition(newY);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Enable or disable VSync mode.
     *
     * @param newSetting true&rarr;synchronize, false&rarr; don't synchronize
     */
    public void setVSync(boolean newSetting) {
        boolean oldSetting = isVSync();
        if (newSetting != oldSetting) {
            cachedSettings.setVSync(newSetting);
            areApplied = false;
            areSaved = false;
        }
    }

    /**
     * Determine how often the JME settings dialog should be shown during
     * initialize().
     *
     * @return an enum value
     */
    public ShowDialog showDialog() {
        return showDialog;
    }

    /**
     * Return the initial X location for LWJGL v3 windowed mode with
     * setCentered(false).
     *
     * @return the screen X coordinate for the left edge of the content area
     */
    public int startX() {
        int result = cachedSettings.getWindowXPosition();
        return result;
    }

    /**
     * Return the initial Y location for LWJGL v3 windowed mode with
     * setCentered(false).
     *
     * @return the screen Y coordinate for the left edge of the content area
     */
    public int startY() {
        int result = cachedSettings.getWindowYPosition();
        return result;
    }

    /**
     * Determine the display width.
     *
     * @return width (in pixels, &gt;0)
     */
    public int width() {
        int result = cachedSettings.getWidth();
        assert result > 0 : result;
        return result;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Apply overrides to the specified settings.
     * <p>
     * This implementation is meant to be overridden by an application-specific
     * version.
     *
     * @param settings which settings to modify (not null)
     */
    protected void applyOverrides(AppSettings settings) {
        int minHeight = sizeLimits.minHeight;
        settings.setMinHeight(minHeight);

        int minWidth = sizeLimits.minWidth;
        settings.setMinWidth(minWidth);

        settings.setResizable(false);
        settings.setTitle(applicationName);
    }

    /**
     * Access the application.
     *
     * @return the pre-existing instance (not null)
     */
    protected ActionApplication getApplication() {
        ActionApplication result = application;
        assert result != null;
        return result;
    }

    /**
     * Test whether the cached settings match one or more of the default
     * monitor's available display modes.
     *
     * @return true for a match, otherwise false
     */
    private boolean matchesAvailableDisplayMode() {
        boolean result = false;

        int bitDepth = cachedSettings.getBitsPerPixel();
        int frequency = cachedSettings.getFrequency();
        int height = cachedSettings.getHeight();
        int width = cachedSettings.getWidth();

        Iterable<DisplayMode> modes = DsUtils.getDisplayModes();
        for (DisplayMode mode : modes) {
            // TODO see algorithm in LwjglDisplay.getFullscreenDisplayMode()

            int modeBitDepth = mode.getBitDepth();
            if (modeBitDepth <= 0
                    || bitDepth <= 0
                    || modeBitDepth == bitDepth) {

                int modeFrequency = mode.getRefreshRate();
                if (modeFrequency <= 0
                        || frequency <= 0
                        || modeFrequency == frequency) {

                    if (mode.getWidth() == width
                            && mode.getHeight() == height) {
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
    }
}
