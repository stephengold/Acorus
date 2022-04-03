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
package jme3utilities.ui;

import com.jme3.system.JmeContext;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jme3utilities.Validate;
import jme3utilities.math.MyArray;

/**
 * Utility methods connected with display settings.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class DsUtils {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DsUtils.class.getName());
    /**
     * pattern for matching a display dimensions
     */
    final private static Pattern dimensionsPattern
            = Pattern.compile("^\\s*(\\d+)\\s*[x,]\\s*(\\d+)\\s*");
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private DsUtils() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Describe the specified display dimensions.
     *
     * @param width width in pixels (&gt;0)
     * @param height height in pixels (&gt;0)
     * @return a textual description (not null, not empty)
     */
    public static String describeDimensions(int width, int height) {
        Validate.positive(width, "width");
        Validate.positive(height, "height");

        String description = String.format("%d x %d", width, height);
        return description;
    }

    /**
     * Describe an MSAA sampling factor.
     *
     * @param factor samples per pixel (&ge;0, &le;16)
     * @return a textual description (not null, not empty)
     */
    public static String describeMsaaFactor(int factor) {
        String description;
        if (factor <= 1) {
            description = "disabled";
        } else {
            description = String.format("%dx", factor);
        }

        return description;
    }

    /**
     * Return the default monitor's current display mode.
     *
     * @return a new instance (not null)
     */
    public static DisplayMode displayMode() {
        DisplayMode result;

        try { // accessing GLFW via reflection of LWJGL v3
            Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
            Method getMonitor = glfwClass.getDeclaredMethod(
                    "glfwGetPrimaryMonitor");
            Method getMode = glfwClass.getDeclaredMethod(
                    "glfwGetVideoMode", long.class);

            // long monitorId = GLFW.glfwGetPrimaryMonitor();
            Object monitorId = getMonitor.invoke(null);

            // GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitorId);
            Object vidMode = getMode.invoke(null, monitorId);

            result = makeDisplayMode(vidMode);

        } catch (Exception e) {
            GraphicsEnvironment environment
                    = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = environment.getDefaultScreenDevice();
            result = device.getDisplayMode();
        }

        return result;
    }

    /**
     * Obtain an array of MSAA sampling factors. TODO rename listMsaaFactors?
     *
     * @return a new array (in ascending order)
     */
    public static int[] getMsaaFactors() {
        int[] msaaFactors = {1, 2, 4, 6, 8};

        assert MyArray.isSorted(msaaFactors);
        return msaaFactors;
    }

    /**
     * Return the current screen X coordinate for the left edge of the content
     * area.
     *
     * @param context the rendering context (not null)
     * @return screen X coordinate
     */
    public static int getWindowXPosition(JmeContext context) {
        try { // via reflection of LWJGL v2
            Class<?> displayClass
                    = Class.forName("org.lwjgl.opengl.Display");
            Method getX = displayClass.getDeclaredMethod("getX");

            // result = Display.getX();
            int result = (Integer) getX.invoke(null);
            return result;

        } catch (ClassNotFoundException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException exception) {
        }

        int x[] = new int[1];
        int y[] = new int[1];

        try { // accessing GLFW via reflection of LWJGL v3
            Class<?> windowClass
                    = Class.forName("com.jme3.system.lwjgl.LwjglWindow");
            Method getHandle = windowClass.getDeclaredMethod("getWindowHandle");

            Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
            Method getPos = glfwClass.getDeclaredMethod("glfwGetWindowPos",
                    long.class, int[].class, int[].class);

            // long windowId = context.getWindowHandle();
            Object windowId = getHandle.invoke(context);

            // GLFW.glfwGetWindowPos(windowId, x, y);
            getPos.invoke(null, windowId, x, y);

            int result = x[0];
            return result;

        } catch (ClassNotFoundException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Return the current screen Y coordinate for the top edge of the content
     * area.
     *
     * @param context the rendering context (not null)
     * @return the screen Y coordinate
     */
    public static int getWindowYPosition(JmeContext context) {
        try { // via reflection of LWJGL v2
            Class<?> displayClass
                    = Class.forName("org.lwjgl.opengl.Display");
            Method getY = displayClass.getDeclaredMethod("getY");

            // result = Display.getY();
            int result = (Integer) getY.invoke(null);
            return result;

        } catch (ClassNotFoundException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException exception) {
        }

        int x[] = new int[1];
        int y[] = new int[1];

        try { // accessing GLFW via reflection of LWJGL v3
            Class<?> windowClass
                    = Class.forName("com.jme3.system.lwjgl.LwjglWindow");
            Method getHandle = windowClass.getDeclaredMethod("getWindowHandle");

            Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
            Method getPos = glfwClass.getDeclaredMethod("glfwGetWindowPos",
                    long.class, int[].class, int[].class);

            // long windowId = context.getWindowHandle();
            Object windowId = getHandle.invoke(context);

            // GLFW.glfwGetWindowPos(windowId, x, y);
            getPos.invoke(null, windowId, x, y);

            int result = y[0];
            return result;

        } catch (ClassNotFoundException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Test whether jme3-lwjgl3 is in the classpath.
     *
     * @return true if present, otherwise false
     */
    public static boolean hasLwjglVersion3() {
        boolean result = true;
        try {
            Class.forName("com.jme3.system.lwjgl.LwjglWindow");
        } catch (ClassNotFoundException exception) {
            result = false;
        }

        return result;
    }

    /**
     * Enumerate the default monitor's available display modes.
     *
     * @return a new list of modes (not null)
     */
    public static List<DisplayMode> listDisplayModes() {
        List<DisplayMode> result;

        try { // accessing GLFW via reflection of LWJGL v3
            Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
            Method getMonitor = glfwClass.getDeclaredMethod(
                    "glfwGetPrimaryMonitor");
            Method getModes = glfwClass.getDeclaredMethod(
                    "glfwGetVideoModes", long.class);

            Class<?> vidModeClass = Class.forName("org.lwjgl.glfw.GLFWVidMode");
            Class<?>[] innerClasses = vidModeClass.getDeclaredClasses();
            assert innerClasses.length == 1 : innerClasses.length;
            Class<?> vmBuffer = innerClasses[0];
            Method get = vmBuffer.getMethod("get");
            Method hasRemaining = vmBuffer.getMethod("hasRemaining");

            // long monitorId = GLFW.glfwGetPrimaryMonitor();
            Object monitorId = getMonitor.invoke(null);

            // GLFWVidMode.Buffer buf = GLFW.glfwGetVideoModes(monitorId);
            Object buf = getModes.invoke(null, monitorId);

            result = new ArrayList<>(32);

            // while (buf.hasRemaining()) {
            while ((Boolean) hasRemaining.invoke(buf)) {

                // GLFWVidMode vidMode = modes.get();
                Object vidMode = get.invoke(buf);

                DisplayMode mode = makeDisplayMode(vidMode);
                result.add(mode);
            }
            logger.info("used GLFW via reflection of LWJGL v3");

        } catch (ClassNotFoundException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException exception) {
            logger.warning("using AWT due to " + exception.toString());

            // LWJGL v3 is unavailable, so use AWT instead.
            GraphicsEnvironment environment
                    = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = environment.getDefaultScreenDevice();
            DisplayMode[] modeArray = device.getDisplayModes();
            result = Arrays.asList(modeArray);
        }

        return result;
    }

    /**
     * Parse the specified text to obtain display dimensions.
     *
     * @param description the text to parse (not null, not empty)
     * @return a new array containing the width and height, or null for a syntax
     * error
     */
    public static int[] parseDisplaySize(String description) {
        Validate.nonEmpty(description, "text");

        String lcText = description.toLowerCase(Locale.ROOT);
        Matcher matcher = dimensionsPattern.matcher(lcText);
        int[] result = null;
        if (matcher.find()) {
            result = new int[2];
            String widthText = matcher.group(1);
            result[0] = Integer.parseInt(widthText);
            String heightText = matcher.group(2);
            result[1] = Integer.parseInt(heightText);
        }

        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Convert a GLFWVidMode to a DisplayMode via reflection of LWJGL v3.
     *
     * @param glfwVidMode (not null, unaffected)
     * @return a new instance
     *
     * @throws ClassNotFoundException if LWJGL v3 isn't on the classpath
     * @throws IllegalAccessException ?
     * @throws InvocationTargetException ?
     * @throws NoSuchMethodException ?
     */
    private static DisplayMode makeDisplayMode(Object glfwVidMode)
            throws ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Class<?> vidModeClass = Class.forName("org.lwjgl.glfw.GLFWVidMode");

        Method getBlueBits = vidModeClass.getDeclaredMethod("blueBits");
        Method getGreenBits = vidModeClass.getDeclaredMethod("greenBits");
        Method getHeight = vidModeClass.getDeclaredMethod("height");
        Method getRedBits = vidModeClass.getDeclaredMethod("redBits");
        Method getRate = vidModeClass.getDeclaredMethod("refreshRate");
        Method getWidth = vidModeClass.getDeclaredMethod("width");

        // int width = glfwVidMode.width();
        int width = (Integer) getWidth.invoke(glfwVidMode);

        // int height = glfwVidMode.height();
        int height = (Integer) getHeight.invoke(glfwVidMode);

        // int redBits = glfwVidMode.redBits();
        int redBits = (Integer) getRedBits.invoke(glfwVidMode);

        // int greenBits = glfwVidMode.greenBits();
        int greenBits = (Integer) getGreenBits.invoke(glfwVidMode);

        // int blueBits = glfwVidMode.blueBits();
        int blueBits = (Integer) getBlueBits.invoke(glfwVidMode);

        // int rate = glfwVidMode.refreshRate();
        int rate = (Integer) getRate.invoke(glfwVidMode);

        int bitDepth = redBits + greenBits + blueBits;
        DisplayMode result = new DisplayMode(width, height, bitDepth, rate);

        return result;
    }
}
