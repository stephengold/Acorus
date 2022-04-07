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

import com.jme3.app.state.AppState;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.MySpatial;
import jme3utilities.Validate;
import jme3utilities.debug.AxesVisualizer;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;

/**
 * An abstract ActionApplication with additional data and methods for use in
 * demos.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class AbstractDemo extends ActionApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * animation/physics speed when paused
     */
    final public static float pausedSpeed = 1e-12f;
    /**
     * message logger for this class
     */
    final public static Logger loggerA
            = Logger.getLogger(AbstractDemo.class.getName());
    /**
     * action string to request JVM garbage collection
     */
    final public static String asCollectGarbage = "collect garbage";
    /**
     * action string to activate the display-settings editor
     */
    final public static String asEditDisplaySettings = "edit display settings";
    /**
     * action to toggle the state of the help node
     */
    final public static String asToggleHelp = "toggle help";
    /**
     * action to toggle animation and physics simulation: paused/running
     */
    final public static String asTogglePause = "toggle pause";
    /**
     * action to toggle visualization of world axes
     */
    final public static String asToggleWorldAxes = "toggle worldAxes";
    // *************************************************************************
    // constructors

    /**
     * Instantiate a demo with the appstates favored by SimpleApplication
     * (AudioListenerState, ConstantVerifierState, DebugKeysAppState,
     * FlyCamAppState, and StatsAppState) pre-attached.
     *
     * A DefaultInputMode and a ScreenshotAppState will be attached during
     * initialization.
     */
    protected AbstractDemo() {
    }

    /**
     * Instantiate a demo with the specified appstates pre-attached. A
     * DefaultInputMode and a ScreenshotAppState will be attached during
     * initialization.
     *
     * @param initialAppStates the appstates to be pre-attached (may be null,
     * unaffected)
     */
    protected AbstractDemo(AppState... initialAppStates) {
        super(initialAppStates);
    }
    // *************************************************************************
    // fields

    /**
     * visualizer for the world axes
     */
    private AxesVisualizer worldAxes = null;
    /**
     * library of named geometry materials
     */
    final private Map<String, Material> namedMaterials = new TreeMap<>();
    /**
     * Node for displaying hotkey help in the GUI scene
     */
    private Node helpNode;
    /**
     * Node for displaying "toggle help: H" in the GUI scene
     */
    private Node minHelpNode;
    // *************************************************************************
    // new methods exposed

    /**
     * Advance a float selection by the specified (cyclic) amount.
     *
     * @param valuesArray an array of values in ascending order (not null, not
     * empty, unaffected)
     * @param startValue the starting value (found in values[])
     * @param amount the number of values to advance (may be negative)
     * @return the new (advanced) value
     */
    public static float advanceFloat(float[] valuesArray, float startValue,
            int amount) {
        Validate.nonEmpty(valuesArray, "values array");

        int index = Arrays.binarySearch(valuesArray, startValue);

        float result;
        if (index < 0) {
            result = valuesArray[0];
        } else {
            assert valuesArray[index] == startValue;
            index = MyMath.modulo(index + amount, valuesArray.length);
            result = valuesArray[index];
        }

        return result;
    }

    /**
     * Advance an integer selection by the specified (cyclic) amount.
     *
     * @param valuesArray an array of values in ascending order (not null, not
     * empty, unaffected)
     * @param startValue the starting value (found in values[])
     * @param amount the number of values to advance (may be negative)
     * @return the new (advanced) value
     */
    public static int advanceInt(int[] valuesArray, int startValue,
            int amount) {
        Validate.nonEmpty(valuesArray, "values array");
        Validate.require(valuesArray.length > 0, "non-empty values array");

        int index = Arrays.binarySearch(valuesArray, startValue);

        int result;
        if (index < 0) {
            result = valuesArray[0];
        } else {
            assert valuesArray[index] == startValue;
            index = MyMath.modulo(index + amount, valuesArray.length);
            result = valuesArray[index];
        }

        return result;
    }

    /**
     * Advance a String selection by the specified (cyclic) amount.
     *
     * @param valuesArray an array of values in ascending order (not null, not
     * empty, unaffected)
     * @param startValue the starting value (found in values[])
     * @param amount the number of values to advance (may be negative)
     * @return the new (advanced) value
     */
    public static String advanceString(String[] valuesArray, String startValue,
            int amount) {
        Validate.nonEmpty(valuesArray, "values array");

        int index = Arrays.binarySearch(valuesArray, startValue);

        String result;
        if (index < 0) {
            result = valuesArray[0];
        } else {
            assert valuesArray[index].equals(startValue);
            index = MyMath.modulo(index + amount, valuesArray.length);
            result = valuesArray[index];
        }

        return result;
    }

    /**
     * Test whether the world axes are enabled.
     *
     * @return true if enabled, otherwise false
     */
    public boolean areWorldAxesEnabled() {
        boolean result;
        if (worldAxes == null) {
            result = false;
        } else {
            result = worldAxes.isEnabled();
        }

        return result;
    }

    /**
     * Generate (or re-generate) detailed and minimal versions of hotkey help.
     * Attach the minimal one to the GUI scene. TODO delete this?
     *
     * @param bounds the desired screen coordinates (not null, unaffected)
     */
    public void attachHelpNode(Rectangle bounds) {
        Validate.nonNull(bounds, "bounds");

        InputMode inputMode = getDefaultInputMode();
        updateHelpNodes(inputMode, bounds, HelpVersion.Minimal);
    }

    /**
     * Add a visualizer for the axes of the world coordinate system.
     *
     * @param axisLength the desired length for each axis arrow (in world units,
     * &gt;0)
     */
    public void attachWorldAxes(float axisLength) {
        Validate.positive(axisLength, "axis length");

        if (worldAxes != null) {
            rootNode.removeControl(worldAxes);
        }

        worldAxes = new AxesVisualizer(assetManager, axisLength);
        worldAxes.setLineWidth(AxesVisualizer.widthForSolid);

        rootNode.addControl(worldAxes);
        worldAxes.setEnabled(true);
    }

    /**
     * Translate a model's center so that the model rests on the X-Z plane, and
     * its center lies on the Y axis.
     *
     * @param cgModel (not null, modified)
     */
    public static void centerCgm(Spatial cgModel) {
        Validate.nonNull(cgModel, "model");

        Vector3f[] minMax = MySpatial.findMinMaxCoords(cgModel);
        Vector3f min = minMax[0];
        Vector3f max = minMax[1];
        Vector3f center = MyVector3f.midpoint(min, max, null);
        Vector3f offset = new Vector3f(center.x, min.y, center.z);

        Vector3f location = cgModel.getWorldTranslation();
        location.subtractLocal(offset);
        MySpatial.setWorldLocation(cgModel, location);
    }

    /**
     * Calculate screen bounds for a detailed help node. Meant to be overridden.
     *
     * @param viewPortWidth (in pixels, &gt;0)
     * @param viewPortHeight (in pixels, &gt;0)
     * @return a new instance
     */
    public Rectangle detailedHelpBounds(int viewPortWidth, int viewPortHeight) {
        /*
         * Position help nodes near the upper-right corner of the viewport.
         */
        float margin = 10f; // in pixels
        float height = viewPortHeight - (2f * margin);
        float width = 250f; // in pixels
        float leftX = viewPortWidth - (width + margin);
        float topY = margin + height;
        Rectangle result = new Rectangle(leftX, topY, width, height);

        return result;
    }

    /**
     * Find the named Material in the library.
     *
     * @param name the name of the Material to find (not null)
     * @return the pre-existing instance, or null if not found
     */
    public Material findMaterial(String name) {
        Validate.nonNull(name, "name");

        Material result = namedMaterials.get(name);
        return result;
    }

    /**
     * Initialize the library of named materials. Invoke during startup.
     */
    public void generateMaterials() {
        ColorRGBA green = new ColorRGBA(0f, 0.12f, 0f, 1f);
        Material platform = MyAsset.createShadedMaterial(assetManager, green);
        registerMaterial("platform", platform);
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
            Camera guiCamera = guiViewPort.getCamera();
            int viewPortWidth = guiCamera.getWidth();
            int viewPortHeight = guiCamera.getHeight();
            updateHelpNodes(newMode, viewPortWidth, viewPortHeight,
                    HelpVersion.Detailed);
        }
    }

    /**
     * Test whether animation is paused.
     *
     * @return true if paused, otherwise false
     */
    public boolean isPaused() {
        if (speed <= pausedSpeed) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a Material to the library.
     *
     * @param name the desired name for the Material, which is also the key that
     * will be used to find it (not null)
     * @param material (not null, alias created)
     */
    public void registerMaterial(String name, Material material) {
        Validate.nonNull(name, "name");
        Validate.nonNull(material, "material");
        assert !namedMaterials.containsKey(name);

        material.setName(name);
        namedMaterials.put(name, material);
    }

    /**
     * Scale the specified C-G model uniformly so that it has the specified
     * height, assuming Y-up orientation.
     *
     * @param cgModel (not null, modified)
     * @param height the desired height (in world units, &gt;0)
     */
    public static void setCgmHeight(Spatial cgModel, float height) {
        Validate.nonNull(cgModel, "model");
        Validate.positive(height, "height");

        Vector3f[] minMax = MySpatial.findMinMaxCoords(cgModel);
        Vector3f min = minMax[0];
        Vector3f max = minMax[1];
        float oldHeight = max.y - min.y;
        if (oldHeight > 0f) {
            cgModel.scale(height / oldHeight);
        }
    }

    /**
     * Toggle between the detailed help node and the minimal one.
     */
    public void toggleHelp() {
        if (helpNode.getParent() == null) {
            minHelpNode.removeFromParent();
            guiNode.attachChild(helpNode);
        } else {
            helpNode.removeFromParent();
            guiNode.attachChild(minHelpNode);
        }
    }

    /**
     * Toggle the animation and physics simulation: paused/running.
     */
    public void togglePause() {
        float newSpeed = isPaused() ? 1f : pausedSpeed;
        setSpeed(newSpeed);
    }

    /**
     * Toggle visualization of world axes.
     */
    public void toggleWorldAxes() {
        boolean enabled = worldAxes.isEnabled();
        worldAxes.setEnabled(!enabled);
    }

    /**
     * Generate (or re-generate) detailed and minimal versions of hotkey help
     * nodes for the specified input mode and viewport dimensions. Attach based
     * on the existing help nodes or as specified.
     *
     * @param inputMode the active input mode (unaffected) or null if none
     * @param viewPortWidth (in pixels, &gt;0)
     * @param viewPortHeight (in pixels, &gt;0)
     * @param preferredVersion which version to attach if no help nodes are
     * currently attached (not null)
     */
    public void updateHelpNodes(InputMode inputMode, int viewPortWidth,
            int viewPortHeight, HelpVersion preferredVersion) {
        Validate.positive(viewPortWidth, "viewport width");
        Validate.positive(viewPortHeight, "viewport height");
        Validate.nonNull(preferredVersion, "preferred version");

        Rectangle bounds = detailedHelpBounds(viewPortWidth, viewPortHeight);
        updateHelpNodes(inputMode, bounds, preferredVersion);
    }

    /**
     * Generate (or re-generate) detailed and minimal versions of help nodes for
     * the specified input mode and bounds. Attach based on the existing help
     * nodes or as specified.
     *
     * @param inputMode the active input mode (unaffected) or null if none
     * @param bounds the desired screen coordinates (not null, unaffected)
     * @param preferredVersion which version to attach if no help nodes are
     * currently attached (not null)
     */
    public void updateHelpNodes(InputMode inputMode, Rectangle bounds,
            HelpVersion preferredVersion) {
        Validate.nonNull(bounds, "bounds");
        Validate.nonNull(preferredVersion, "preferred version");

        Node detailedParent;
        Node minimalParent;
        switch (preferredVersion) {
            case Detailed:
                detailedParent = guiNode;
                minimalParent = null;
                break;

            case Minimal:
                detailedParent = null;
                minimalParent = guiNode;
                break;

            default:
                String message = "preferredVersion = " + preferredVersion;
                throw new IllegalArgumentException(message);
        }
        /*
         * If help nodes already exist, note where they are attached
         * and then detach them.
         */
        if (helpNode != null) {
            detailedParent = helpNode.getParent();
            helpNode.removeFromParent();
        }

        if (minHelpNode != null) {
            minimalParent = minHelpNode.getParent();
            minHelpNode.removeFromParent();
        }

        if (inputMode != null) {
            /*
             * Build and attach the detailed version.
             */
            generateDetailedHelp(inputMode, bounds, detailedParent);
            /*
             * Build and attach the minimal version.
             */
            generateMinimalHelp(bounds, minimalParent);
        }
    }
    // *************************************************************************
    // ActionApplication methods

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
                case asCollectGarbage:
                    System.gc();
                    return;
                case asToggleHelp:
                    toggleHelp();
                    return;
                case asTogglePause:
                    togglePause();
                    return;
                case asToggleWorldAxes:
                    toggleWorldAxes();
                    return;
            }
        }
        super.onAction(actionString, ongoing, tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Generate (or re-generate) the detailed help node for the specified input
     * mode and bounds and attach it to the specified parent.
     *
     * @param inputMode the input mode (not null, unaffected)
     * @param bounds the desired screen coordinates (not null, unaffected)
     * @param parent where to attach, or null to leave detached
     */
    private void generateDetailedHelp(InputMode inputMode, Rectangle bounds,
            Node parent) {
        float extraSpace = 20f; // separation between actions, in pixels
        helpNode = HelpUtils.buildNode(inputMode, bounds, guiFont, extraSpace);

        helpNode.setLocalTranslation(0f, 0f, 1f); // move to the front

        if (parent != null) {
            parent.attachChild(helpNode);
        }
    }

    /**
     * Generate (or re-generate) the minimal help node for the specified bounds
     * and attach it to the specified parent.
     *
     * @param detailedHelpBounds (not null, unaffected)
     * @param parent where to attach, or null to leave detached
     */
    private void generateMinimalHelp(Rectangle detailedHelpBounds,
            Node parent) {
        /*
         * Create a temporary InputMode with just a single binding.
         */
        InputMode tmpInputMode = new InputMode("dummy") {
            @Override
            protected void defaultBindings() {
                // do nothing
            }

            @Override
            public void onAction(String s, boolean b, float f) {
                // do nothing
            }
        };
        tmpInputMode.bind(asToggleHelp, KeyInput.KEY_H);
        /*
         * Narrow the bounds to just the upper right corner.
         */
        float width = 100f; // in pixels
        float x = detailedHelpBounds.x + detailedHelpBounds.width - width;
        float y = detailedHelpBounds.y;
        float height = detailedHelpBounds.height;
        Rectangle narrowBounds = new Rectangle(x, y, width, height);

        float extraSpace = 0f; // separation between actions, in pixels
        minHelpNode = HelpUtils.buildNode(
                tmpInputMode, narrowBounds, guiFont, extraSpace);

        minHelpNode.setLocalTranslation(0f, 0f, 1f); // move to the front

        if (parent != null) {
            parent.attachChild(minHelpNode);
        }
    }
}
