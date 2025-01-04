/*
 Copyright (c) 2017-2025 Stephen Gold

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
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import java.awt.DisplayMode;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jme3utilities.DsUtils;
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
    final private Application application;
    /**
     * proposed settings, which can be applied (to the application context) or
     * saved (written to persistent storage)
     */
    final private AppSettings proposedSettings = new AppSettings(true);
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
        apiNameMap.put("OpenGL 3.0", AppSettings.LWJGL_OPENGL30);
        apiNameMap.put("OpenGL 3.1", AppSettings.LWJGL_OPENGL31);
        apiNameMap.put("OpenGL 3.2", AppSettings.LWJGL_OPENGL32);
        apiNameMap.put("OpenGL 3.3", AppSettings.LWJGL_OPENGL33);
        apiNameMap.put("OpenGL 4.0", AppSettings.LWJGL_OPENGL40);
        apiNameMap.put("OpenGL 4.1", AppSettings.LWJGL_OPENGL41);
        apiNameMap.put("OpenGL 4.2", AppSettings.LWJGL_OPENGL42);
        apiNameMap.put("OpenGL 4.3", AppSettings.LWJGL_OPENGL43);
        apiNameMap.put("OpenGL 4.4", AppSettings.LWJGL_OPENGL44);
        apiNameMap.put("OpenGL 4.5", AppSettings.LWJGL_OPENGL45);
        apiNameMap.put("OpenGL compatibility", AppSettings.LWJGL_OPENGL2);
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
     * Instantiate settings for the specified Application.
     *
     * @param app the current application instance (not null, alias created)
     * @param appName the name of the application (not null)
     * @param sizeLimits the desired framebuffer-size limits (not null, alias
     * created)
     */
    public DisplaySettings(
            Application app, String appName, RectSizeLimits sizeLimits) {
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
     * Apply the proposed settings to the graphics context and restart the
     * context to put them into effect.
     */
    public void applyToContext() {
        assert canApply();
        assert !areApplied;

        AppSettings clone = new AppSettings(false);
        clone.copyFrom(proposedSettings);

        //System.out.println(clone);
        application.setSettings(clone);
        //System.out.println("restart() threadId="
        //        + Thread.currentThread().getId());
        //System.out.flush();
        application.restart();
        this.areApplied = true;
    }

    /**
     * Test whether the proposed settings have been applied since their last
     * applicable modification.
     *
     * @return true if clean, otherwise false
     */
    public boolean areApplied() {
        return areApplied;
    }

    /**
     * Test whether the proposed settings have been saved (written to persistent
     * storage) since their last modification.
     *
     * @return true if clean, otherwise false
     */
    public boolean areSaved() {
        return areSaved;
    }

    /**
     * Test the validity of the proposed settings prior to a save.
     *
     * @return true if good enough, otherwise false
     */
    public boolean areValid() {
        String feedback = feedbackValid();
        boolean result = feedback.isEmpty();

        return result;
    }

    /**
     * Test whether the proposed settings can be applied immediately.
     *
     * @return true if they can be applied, otherwise false
     */
    public boolean canApply() {
        String feedback = feedbackApplicable();
        boolean result = feedback.isEmpty();

        return result;
    }

    /**
     * Determine the color depth.
     *
     * @return depth (in bits per pixel) or &le;0 for unknown/don't care
     */
    public int colorDepth() {
        int result = proposedSettings.getBitsPerPixel();
        return result;
    }

    /**
     * Explain why these settings cannot be applied.
     *
     * @return message text (not null)
     */
    public String feedbackApplicable() {
        boolean v3 = DsUtils.hasLwjglVersion3();
        if (v3) {
            Platform platform = JmeSystem.getPlatform();
            if (platform.getOs() == Platform.Os.Linux) {
                /*
                 * On some Linuxes, jme3-lwjgl3 works only
                 * with OpenGL compatibility profile.
                 */
                String newProfileName = proposedSettings.getRenderer();
                if (!newProfileName.equals(AppSettings.LWJGL_OPENGL2)) {
                    return "Can't exit compatibility profile!";
                }
            }
        }

        return "";
    }

    /**
     * Explain why the proposed settings are invalid.
     *
     * @return message text (not null, not empty) or "" if settings are valid
     */
    public String feedbackValid() {
        int height = proposedSettings.getHeight();
        int width = proposedSettings.getWidth();
        if (!sizeLimits.isInRange(width, height)) {
            return sizeLimits.feedbackInRange(width, height);
        }

        if (proposedSettings.isFullscreen()) {
            // assuming that the device supports full-screen exclusive mode
            boolean foundMatch = matchesAvailableDisplayMode();
            if (!foundMatch) {
                return "No matching display mode for device.";
            }
        }

        if (!DsUtils.hasLwjglVersion3() && msaaFactor() > 16) {
            return "Can't have MSAA factor >16 with LWJGL v2.";
        }

        return ""; // The proposed settings are valid.
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
    public String graphicsApi() {
        String result;
        String value = proposedSettings.getRenderer();
        for (Map.Entry<String, String> entry : apiNameMap.entrySet()) {
            if (entry.getValue().equals(value)) {
                result = entry.getKey();
                return result;
            }
        }
        throw new IllegalStateException("value = " + value);
    }

    /**
     * Determine the display height.
     *
     * @return height (in pixels, &gt;0)
     */
    public int height() {
        int result = proposedSettings.getHeight();
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
        // Attempt to load settings from user preferences (persistent storage).
        boolean loadedFromStore = false;
        try {
            if (Preferences.userRoot().nodeExists(applicationName)) {
                proposedSettings.load(applicationName);
                loadedFromStore = true;
            }
        } catch (BackingStoreException exception) {
            // do nothing
        }

        // Apply overrides to the loaded settings.
        applyOverrides(proposedSettings);

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
            // Show JME's settings dialog.
            boolean loadFlag = false;
            boolean proceed
                    = JmeSystem.showSettingsDialog(proposedSettings, loadFlag);
            if (!proceed) {
                // The user clicked on the "Cancel" button.
                return null;
            }
        }

        AppSettings clone = new AppSettings(false);
        clone.copyFrom(proposedSettings);

        return clone;
    }

    /**
     * Test whether center-on-start is enabled for LWJGL v3 windowed mode.
     *
     * @return true if centering, otherwise false
     */
    public boolean isCentered() {
        boolean result = proposedSettings.getCenterWindow();
        return result;
    }

    /**
     * Test whether full-screen mode is enabled.
     *
     * @return true if full-screen, otherwise false
     */
    public boolean isFullscreen() {
        boolean result = proposedSettings.isFullscreen();
        return result;
    }

    /**
     * Test whether gamma correction is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isGammaCorrection() {
        boolean result = proposedSettings.isGammaCorrection();
        return result;
    }

    /**
     * Test whether graphics debugging is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isGraphicsDebug() {
        boolean result = proposedSettings.isGraphicsDebug();
        return result;
    }

    /**
     * Test whether graphics tracing is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isGraphicsTrace() {
        boolean result = proposedSettings.isGraphicsTrace();
        return result;
    }

    /**
     * Test whether VSync is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isVSync() {
        boolean result = proposedSettings.isVSync();
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
     * Attempt to load settings from user preferences (persistent storage). If
     * successful, this reverts all properties to their last saved values.
     *
     * @return true if successful, otherwise false
     */
    public boolean load() {
        boolean success = false;
        try {
            if (Preferences.userRoot().nodeExists(applicationName)) {
                proposedSettings.load(applicationName);
                this.areApplied = false;
                this.areSaved = true;
                success = true;
            }
        } catch (BackingStoreException exception) {
            // do nothing
        }

        if (!success) {
            logger.log(Level.WARNING, "Failed to read settings for \"{0}\""
                    + " from persistent storage.", applicationName);
        }

        return success;
    }

    /**
     * Reset the proposed settings to the application's default values.
     */
    public void loadDefaults() {
        boolean loadDefaults = true;
        AppSettings defaults = new AppSettings(loadDefaults);

        proposedSettings.copyFrom(defaults);
        applyOverrides(proposedSettings);

        this.areApplied = false;
        this.areSaved = false;
    }

    /**
     * Determine the sampling factor for multi-sample anti-aliasing (MSAA).
     *
     * @return sampling factor (in samples per pixel, &ge;0)
     */
    public int msaaFactor() {
        int result = proposedSettings.getSamples();
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
        int result = proposedSettings.getFrequency();
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
            proposedSettings.setWidth(newWidth);
            this.areSaved = false;
        }

        int oldHeight = height();
        if (newHeight != oldHeight) {
            proposedSettings.setHeight(newHeight);
            this.areSaved = false;
        }
    }

    /**
     * Write the proposed settings to persistent storage, so that they will take
     * effect the next time the application is launched.
     *
     * @return true if successful, otherwise false
     */
    public boolean save() {
        boolean result = false;
        try {
            proposedSettings.save(applicationName);
            this.areSaved = true;
            result = true;
        } catch (BackingStoreException exception) {
            logger.log(Level.WARNING, "Failed to write settings for \"{0}\" "
                    + "to persistent storage.", applicationName);
        }

        return result;
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
            proposedSettings.setCenterWindow(newSetting);
            this.areApplied = false;
            this.areSaved = false;
        }
    }

    /**
     * Alter the color depth.
     *
     * @param newDepth the desired color depth (in bits per pixel) or &le;0 for
     * unknown/don't care
     */
    public void setColorDepth(int newDepth) {
        int oldBpp = colorDepth();
        if (newDepth != oldBpp) {
            proposedSettings.setBitsPerPixel(newDepth);
            this.areApplied = false;
            this.areSaved = false;
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
            proposedSettings.setWidth(newWidth);
            this.areApplied = false;
            this.areSaved = false;
        }
        int oldHeight = height();
        if (newHeight != oldHeight) {
            proposedSettings.setHeight(newHeight);
            this.areApplied = false;
            this.areSaved = false;
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
            proposedSettings.setFullscreen(newSetting);
            this.areApplied = false;
            this.areSaved = false;
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
            proposedSettings.setGammaCorrection(newSetting);
            this.areApplied = false;
            this.areSaved = false;
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
        String oldSetting = graphicsApi();
        if (!newSetting.equals(oldSetting)) {
            proposedSettings.setRenderer(newSetting);
            this.areApplied = false;
            this.areSaved = false;
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
            proposedSettings.setGraphicsDebug(newSetting);
            // after init, applying has no effect
            this.areSaved = false;
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
            proposedSettings.setGraphicsTrace(newSetting);
            // after init, applying has no effect
            this.areSaved = false;
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
     * @param newFactor number of samples per pixel (&ge;1)
     */
    public void setMsaaFactor(int newFactor) {
        Validate.positive(newFactor, "new factor");

        int oldFactor = msaaFactor();
        if (newFactor != oldFactor) {
            proposedSettings.setSamples(newFactor);
            this.areApplied = false;
            this.areSaved = false;
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
            proposedSettings.setFrequency(newRate);
            this.areApplied = false;
            this.areSaved = false;
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
            proposedSettings.setWindowXPosition(newX);
            this.areApplied = false;
            this.areSaved = false;
        }

        int oldY = startY();
        if (newY != oldY) {
            proposedSettings.setWindowYPosition(newY);
            this.areApplied = false;
            this.areSaved = false;
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
            proposedSettings.setVSync(newSetting);
            this.areApplied = false;
            this.areSaved = false;
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
        int result = proposedSettings.getWindowXPosition();
        return result;
    }

    /**
     * Return the initial Y location for LWJGL v3 windowed mode with
     * setCentered(false).
     *
     * @return the screen Y coordinate for the left edge of the content area
     */
    public int startY() {
        int result = proposedSettings.getWindowYPosition();
        return result;
    }

    /**
     * Determine the display width.
     *
     * @return width (in pixels, &gt;0)
     */
    public int width() {
        int result = proposedSettings.getWidth();
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
    protected Application getApplication() {
        Application result = application;
        assert result != null;
        return result;
    }
    // *************************************************************************
    // new private methods

    /**
     * Test whether the proposed settings match one or more of the default
     * monitor's display modes.
     *
     * @return true for a match, otherwise false
     */
    private boolean matchesAvailableDisplayMode() {
        boolean result = false;

        int bitDepth = proposedSettings.getBitsPerPixel();
        int frequency = proposedSettings.getFrequency();
        int height = proposedSettings.getHeight();
        int width = proposedSettings.getWidth();

        boolean hasLwjglVersion3 = DsUtils.hasLwjglVersion3();
        Iterable<DisplayMode> modes = DsUtils.listDisplayModes();
        for (DisplayMode mode : modes) {
            if (hasLwjglVersion3) {
                result = matchesAvailableDisplayMode3(
                        mode, bitDepth, frequency, width, height);
            } else {
                result = matchesAvailableDisplayMode2(
                        mode, bitDepth, frequency, width, height);
            }
            if (result) { // A single matching mode suffices.
                break;
            }
        }

        return result;
    }

    /**
     * Display-mode matching algorithm for LWJGL v2.
     *
     * @param mode an available DisplayMode (not null, unaffected)
     * @param depth the desired color depth (in bits per pixel)
     * @param rate the desired refresh rate (in Hertz)
     * @param width the desired display width (in pixels, &gt;0)
     * @param height the desired display height (in pixels, &gt;0)
     * @return true for a match, otherwise false
     */
    private static boolean matchesAvailableDisplayMode2(
            DisplayMode mode, int depth, int rate, int width, int height) {
        boolean result = false;

        int modeDepth = mode.getBitDepth();
        if (modeDepth == depth || depth == 24 && modeDepth == 32) {

            int modeRate = mode.getRefreshRate();
            if (modeRate == rate || rate == 60 && modeRate == 59) {

                if (mode.getWidth() == width && mode.getHeight() == height) {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Display-mode matching algorithm for LWJGL v3.
     *
     * @param mode an available DisplayMode (not null, unaffected)
     * @param depth the desired color depth (in bits per pixel) or &le;0 for
     * don't care
     * @param rate the desired refresh rate (in Hertz) or &le;0 for don't care
     * @param width the desired display width (in pixels, &gt;0)
     * @param height the desired display height (in pixels, &gt;0)
     * @return true for a match, otherwise false
     */
    private static boolean matchesAvailableDisplayMode3(
            DisplayMode mode, int depth, int rate, int width, int height) {
        boolean result = false;

        int modeDepth = mode.getBitDepth();
        if (modeDepth <= 0 || depth <= 0 || modeDepth == depth) {
            int modeRate = mode.getRefreshRate();
            if (modeRate <= 0 || rate <= 0 || modeRate == rate) {
                if (mode.getWidth() == width && mode.getHeight() == height) {
                    result = true;
                }
            }
        }

        return result;
    }
}
