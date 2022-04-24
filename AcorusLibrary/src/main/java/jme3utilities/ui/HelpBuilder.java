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

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.texture.image.ColorSpace;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.mesh.RoundedRectangle;

/**
 * Generate hotkey clues for action-oriented applications.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelpBuilder {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(HelpBuilder.class.getName());
    /**
     * separator for compressed hotkey names in lists
     */
    final private static String chnSeparator = "/";
    // *************************************************************************
    // fields

    /**
     * gamma-encoded color for the background
     */
    final private ColorRGBA backgroundColor = ColorRGBA.Black.clone();
    /**
     * gamma-encoded foreground color for non-highlighted text
     */
    final private ColorRGBA foregroundColor = ColorRGBA.White.clone();
    /**
     * gamma-encoded foreground color for highlighted text
     */
    final private ColorRGBA highlightForegroundColor = ColorRGBA.Yellow.clone();
    /**
     * padding added to all 4 sides of the background (in framebuffer pixels,
     * &ge;0)
     */
    private float padding = 5f;
    /**
     * horizontal space between hotkey descriptions (in framebuffer pixels,
     * &gt;0)
     */
    private float separation = 20f;
    /**
     * Z offset of background geometries relative to their nodes
     */
    private float zBackground = 0f;
    /**
     * Z offsets of text spatials relative to their nodes
     */
    private float zText = 0.1f;
    // *************************************************************************
    // new methods exposed

    /**
     * Build a Node to describe the hotkey mappings of the specified InputMode
     * in detail.
     *
     * @param inputMode the InputMode to describe (not null, unaffected)
     * @param bounds (in framebuffer pixels, relative to the resulting node, not
     * null, unaffected)
     * @param font the font to use (not null, unaffected)
     * @param colorSpace the ColorSpace to use (not null)
     * @return a new orphan Node, suitable for attachment to the GUI node
     */
    public Node buildDetailedNode(InputMode inputMode, Rectangle bounds,
            BitmapFont font, ColorSpace colorSpace) {
        Validate.nonNull(inputMode, "input mode");
        Validate.nonNull(bounds, "bounds");
        Validate.nonNull(font, "font");
        Validate.nonNull(colorSpace, "color space");

        ColorRGBA fgColor = foregroundColor.clone();
        ColorRGBA highlightColor = highlightForegroundColor.clone();
        float x = bounds.x;
        float y = bounds.y;
        float maxX = x + 1f;
        float minY = y - 1f;
        float size = font.getCharSet().getRenderedSize();
        Map<String, String> actionToList = mapActions(inputMode);
        Node result = new Node("detailed help node");

        for (Map.Entry<String, String> entry : actionToList.entrySet()) {
            String actionName = entry.getKey();
            String hotkeyList = entry.getValue();
            String text = actionName + ": " + hotkeyList;

            BitmapText textSpatial = new BitmapText(font);
            result.attachChild(textSpatial);
            textSpatial.setSize(size);
            textSpatial.setText(text);
            float textWidth = textSpatial.getLineWidth();
            /*
             * Position the textSpatial relative to the Node.
             */
            if (x > bounds.x && x + textWidth > bounds.x + bounds.width) {
                // start a new line of text
                y -= textSpatial.getHeight();
                x = bounds.x;
            }
            textSpatial.setLocalTranslation(x, y, zText);
            maxX = Math.max(maxX, x + textWidth);
            minY = Math.min(minY, y - textSpatial.getHeight());
            x += textWidth + separation;

            if (actionName.equals(AcorusDemo.asToggleHelp)) {
                textSpatial.setColor(highlightColor); // alias created
            } else {
                textSpatial.setColor(fgColor); // alias created
            }
        }

        Geometry backgroundGeometry
                = buildBackground(bounds, maxX, minY, colorSpace);
        result.attachChild(backgroundGeometry);

        return result;
    }

    /**
     * Build a Node to describe the "toggle help" hotkey mappings of the
     * specified InputMode.
     *
     * @param inputMode the InputMode to describe (not null, unaffected)
     * @param bounds (in framebuffer pixels, relative to the resulting node, not
     * null, unaffected)
     * @param font the font to use (not null, unaffected)
     * @param colorSpace the ColorSpace to use (not null)
     * @return a new orphan Node, suitable for attachment to the GUI node
     */
    public Node buildMinimalNode(InputMode inputMode, Rectangle bounds,
            BitmapFont font, ColorSpace colorSpace) {
        Validate.nonNull(inputMode, "input mode");
        Validate.nonNull(bounds, "bounds");
        Validate.nonNull(font, "font");
        Validate.nonNull(colorSpace, "color space");

        Map<String, String> actionToList = mapActions(inputMode);
        String hotkeyList = actionToList.get(AcorusDemo.asToggleHelp);
        if (hotkeyList == null) {
            /*
             * The InputMode appears to lack any toggle mechanism.
             * Generate a detailed help node instead.
             */
            return buildDetailedNode(inputMode, bounds, font, colorSpace);
        }

        Node result = new Node("minimal help node");
        BitmapText textSpatial = new BitmapText(font);
        result.attachChild(textSpatial);

        String text = AcorusDemo.asToggleHelp + ": " + hotkeyList;
        textSpatial.setText(text);

        ColorRGBA color = highlightForegroundColor.clone();
        textSpatial.setColor(color); // alias created

        float x = bounds.x;
        float y = bounds.y;
        textSpatial.setLocalTranslation(x, y, zText);

        float size = font.getCharSet().getRenderedSize();
        textSpatial.setSize(size);

        float textWidth = textSpatial.getLineWidth();
        float maxX = x + Math.max(1f, textWidth);
        float textHeight = textSpatial.getHeight();
        float minY = y - Math.max(1f, textHeight);
        Geometry backgroundGeometry
                = buildBackground(bounds, maxX, minY, colorSpace);
        result.attachChild(backgroundGeometry);

        return result;
    }

    /**
     * Compactly describe the specified Combo using compressed local hotkey
     * names. Compare with {@link jme3utilities.ui.Combo#toStringLocal()}.
     *
     * @param combo the Combo to describe (not null)
     * @return a textual description (not null)
     */
    public static String describe(Combo combo) {
        StringBuilder result = new StringBuilder(40);

        int numSignals = combo.countSignals();
        for (int signalIndex = 0; signalIndex < numSignals; ++signalIndex) {
            boolean positiveFlag = combo.isPositive(signalIndex);
            if (!positiveFlag) {
                result.append("no");
            }

            String signalName = combo.signalName(signalIndex);
            result.append(signalName);
            result.append('+');
        }

        int code = combo.triggerCode();
        Hotkey hotkey = Hotkey.find(code);
        String hotkeyName = hotkey.localName();
        hotkeyName = compress(hotkeyName);
        result.append(hotkeyName);

        return result.toString();
    }

    /**
     * Alter the color for backgrounds.
     *
     * @param newColor the desired color (not null, unaffected, gamma-encoded,
     * default=opaque black)
     */
    public void setBackgroundColor(ColorRGBA newColor) {
        Validate.nonNull(newColor, "new color");
        backgroundColor.set(newColor);
    }
    // *************************************************************************
    // private methods

    /**
     * Beautify the specified action name.
     *
     * @param actionName the action name (not null)
     * @return the beautified name (not null)
     */
    private static String beautify(String actionName) {
        Validate.nonNull(actionName, "action name");

        String result = actionName;
        if (result.startsWith(InputMode.signalActionPrefix)) {
            result = MyString.remainder(result, InputMode.signalActionPrefix);
        }

        if (result.startsWith("SIMPLEAPP_")) {
            String suffix = MyString.remainder(result, "SIMPLEAPP_");
            result = MyString.firstToLower(suffix);
            if (result.equals("hideStats")) {
                result = "toggle stats";
            }
        } else if (result.startsWith("FLYCAM_")) {
            String suffix = MyString.remainder(result, "FLYCAM_");
            result = "camera " + MyString.firstToLower(suffix);
        }

        return result;
    }

    /**
     * Generate a background geometry for a help node.
     *
     * @param bounds (in screen coordinates, not null, unaffected)
     * @param maxX the highest screen X of the text
     * @param minY the lowest screen Y of the text
     * @param colorSpace (not null)
     * @return a new Geometry, suitable for attachment to the help node
     */
    private Geometry buildBackground(Rectangle bounds, float maxX, float minY,
            ColorSpace colorSpace) {
        float x1 = bounds.x - padding;
        float x2 = maxX + padding;
        float y1 = minY - padding;
        float y2 = bounds.y + padding;
        float zNorm = 1f;
        Mesh mesh = new RoundedRectangle(x1, x2, y1, y2, padding, zNorm);

        Geometry result = new Geometry("help background", mesh);
        result.setLocalTranslation(0f, 0f, zBackground);

        AssetManager assetManager = Locators.getAssetManager();
        ColorRGBA color = DsUtils.renderColor(colorSpace, backgroundColor);
        Material material
                = MyAsset.createUnshadedMaterial(assetManager, color);
        result.setMaterial(material);

        return result;
    }

    /**
     * Compactly describe the named hotkey.
     *
     * @param hotkeyName the hotkey name (not null)
     * @return the compressed name (not null)
     */
    private static String compress(String hotkeyName) {
        Validate.nonNull(hotkeyName, "hotkey name");

        String result = hotkeyName;
        if (result.endsWith(" arrow")) {
            result = MyString.removeSuffix(result, " arrow");
        }
        result = result.replace("numpad ", "num");

        return result;
    }

    /**
     * For the specified InputMode, construct a Map from beautified action names
     * to compressed hotkey names.
     *
     * @param inputMode (not null, unaffected)
     * @return a new String-to-String Map
     */
    private static Map<String, String> mapActions(InputMode inputMode) {
        List<String> actionNames = inputMode.listActionNames();
        Map<String, String> actionsToHots = new TreeMap<>();

        for (String actionName : actionNames) {
            String action = beautify(actionName);

            Collection<String> localHotkeyNames
                    = inputMode.listHotkeysLocal(actionName);
            for (String localHotkeyName : localHotkeyNames) {
                String description = compress(localHotkeyName);
                if (actionsToHots.containsKey(action)) {
                    String oldList = actionsToHots.get(action);
                    String newList = oldList + chnSeparator + description;
                    actionsToHots.put(action, newList);
                } else {
                    actionsToHots.put(action, description);
                }
            }

            Collection<Combo> combos = inputMode.listCombos(actionName);
            for (Combo combo : combos) {
                String description = describe(combo);
                if (actionsToHots.containsKey(action)) {
                    String oldList = actionsToHots.get(action);
                    String newList = oldList + chnSeparator + description;
                    actionsToHots.put(action, newList);
                } else {
                    actionsToHots.put(action, description);
                }
            }
        }

        return actionsToHots;
    }
}
