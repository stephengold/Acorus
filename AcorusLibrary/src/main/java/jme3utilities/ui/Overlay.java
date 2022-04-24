/*
 Copyright (c) 2022, Stephen Gold
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
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.texture.image.ColorSpace;
import java.util.logging.Logger;
import jme3utilities.InitialState;
import jme3utilities.SimpleAppState;
import jme3utilities.Validate;
import jme3utilities.mesh.RoundedRectangle;

/**
 * A rounded rectangle that displays lines of text.
 * <p>
 * The overlay appears in the upper-left portion of the display.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Overlay extends SimpleAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final static Logger loggerO = Logger.getLogger(Overlay.class.getName());
    // *************************************************************************
    // fields

    /**
     * lines of text displayed in the overlay ([0] is the top line)
     */
    final private BitmapText[] contentLines;
    /**
     * gamma-encoded color of the background
     */
    final private ColorRGBA backgroundColor = ColorRGBA.Black.clone();
    /**
     * gamma-encoded foreground color of each content line
     */
    final private ColorRGBA[] contentColors;
    /**
     * Z coordinate for the background (in case the framebuffer gets resized)
     */
    private float backgroundZ = -2f;
    /**
     * Z offset of content relative to the background (&gt;0)
     */
    private float contentZOffset = 0.1f;
    /**
     * vertical interval between successive content lines (in framebuffer
     * pixels, &gt;0)
     */
    private float lineSpacing = 20f;
    /**
     * padding between the content lines and top/bottom/left edges of the
     * background (in framebuffer pixels, &ge;0)
     */
    private float padding = 5f;
    /**
     * width of the background (in framebuffer pixels, &gt;0)
     */
    private float width;
    /**
     * horizontal margin between the background and the edges of the viewport
     * (in framebuffer pixels, &ge;0)
     */
    private float xMargin = 10f;
    /**
     * vertical margin between the background and the edges of the viewport (in
     * framebuffer pixels, &ge;0)
     */
    private float yMargin = 10f;
    /**
     * rounded-rectangle geometry to ensure content visibility
     */
    final private Geometry background;
    /**
     * number of content lines: set by constructor
     */
    final private int numLines;
    /**
     * node to attach (to the application's GUI node) It's corresponds to the
     * upper-left corner of the background.
     */
    final private Node node;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized AppState.
     *
     * @param id the desired AppState ID (not null, unique within the current
     * application)
     * @param width the desired width of the background in framebuffer pixels
     * (&gt;0)
     * @param numLines the number of content lines (&gt;0)
     */
    public Overlay(String id, float width, int numLines) {
        super(InitialState.Disabled);
        Validate.nonNull(id, "name");
        Validate.positive(width, "width");
        Validate.positive(numLines, "number of lines");

        this.setId(id);
        this.node = new Node("overlay node for " + id);

        this.contentLines = new BitmapText[numLines];
        this.contentColors = new ColorRGBA[numLines];
        this.numLines = numLines;
        this.width = width;

        String geometryName = "overlay background for " + id;
        Mesh backgroundMesh = createBackgroundMesh();
        this.background = new Geometry(geometryName, backgroundMesh);
        node.attachChild(background);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the Z offset of the content lines relative to the background.
     *
     * @return the offset (&gt;0)
     */
    public float contentZOffset() {
        assert contentZOffset > 0f : contentZOffset;
        return contentZOffset;
    }

    /**
     * Copy the color of the background.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return the gamma-encoded color (either {@code storeResult} or a new
     * instance)
     */
    public ColorRGBA copyBackgroundColor(ColorRGBA storeResult) {
        if (storeResult == null) {
            return backgroundColor.clone();
        } else {
            return storeResult.set(backgroundColor);
        }
    }

    /**
     * Copy the location of upper-left corner of the background.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return the location vector (either {@code storeResult} or a new
     * instance)
     */
    public Vector3f copyLocation(Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;

        Vector3f location = background.getLocalTranslation(); // alias
        result.set(location);

        return result;
    }

    /**
     * Return the vertical interval between successive content lines.
     *
     * @return the Y interval (in framebuffer pixels, &gt;0)
     */
    public float lineSpacing() {
        assert lineSpacing > 0f : lineSpacing;
        return lineSpacing;
    }

    /**
     * Update colors after the renderer's ColorSpace changes.
     *
     * @param newColorSpace the new ColorSpace (not null)
     */
    public void onColorSpaceChange(ColorSpace newColorSpace) {
        Validate.nonNull(newColorSpace, "new ColorSpace");

        if (isInitialized()) {
            updateBackgroundMaterialColor(newColorSpace);
            updateBitmapColors(newColorSpace);
        }
    }

    /**
     * Relocate this overlay for the specified viewport dimensions. The default
     * policy is to locate the overlay near the upper-left corner of the
     * viewport. TODO alternative policies
     *
     * @param newViewPortWidth the new viewport width (in framebuffer pixels,
     * &gt;0)
     * @param newViewPortHeight the new viewport height (in framebuffer pixels,
     * &gt;0)
     */
    public void onViewPortResize(int newViewPortWidth, int newViewPortHeight) {
        Validate.positive(newViewPortWidth, "new viewport width");
        Validate.positive(newViewPortHeight, "new viewport height");

        updateLocation(newViewPortWidth, newViewPortHeight);
    }

    /**
     * Return the amount of padding between the content lines and
     * top/bottom/left edges of the background.
     *
     * @return the padding (in framebuffer pixels, &ge;0)
     */
    public float padding() {
        assert padding >= 0f : padding;
        return padding;
    }

    /**
     * Alter the color of the background.
     *
     * @param newColor the desired color (not null, unaffected, gamma-encoded,
     * default=opaque black)
     */
    public void setBackgroundColor(ColorRGBA newColor) {
        Validate.nonNull(newColor, "new color");

        backgroundColor.set(newColor);
        if (isInitialized()) {
            Renderer renderer = simpleApplication.getRenderer();
            ColorSpace colorSpace = renderer.isMainFrameBufferSrgb()
                    ? ColorSpace.sRGB : ColorSpace.Linear;
            updateBackgroundMaterialColor(colorSpace);
        }
    }

    /**
     * Alter the Z coordinate for the background.
     *
     * @param newZ the desired Z coordinate (default=-2)
     */
    public void setBackgroundZ(float newZ) {
        this.backgroundZ = newZ;
        updateLocation();
    }

    /**
     * Alter the Z offset of content relative to the background.
     *
     * @param newOffset the desired offset (&gt;0, default=0.1)
     */
    public void setContentZOffset(float newOffset) {
        Validate.positive(newOffset, "new offset");

        this.contentZOffset = newOffset;
        updateContentOffsets();
    }

    /**
     * Alter the vertical interval between successive content lines.
     *
     * @param newSpacing the desired Y offset (in framebuffer pixels, &gt;0,
     * default=20)
     */
    public void setLineSpacing(float newSpacing) {
        Validate.positive(newSpacing, "new spacing");

        if (newSpacing != lineSpacing) {
            this.lineSpacing = newSpacing;
            updateContentOffsets();

            Mesh backgroundMesh = createBackgroundMesh();
            background.setMesh(backgroundMesh);
        }
    }

    /**
     * Move this Overlay to the specified location.
     *
     * @param newLocation the desired framebuffer coordinates for the upper-left
     * corner of the background (not null, unaffected)
     */
    public void setLocation(Vector3f newLocation) {
        Validate.nonNull(newLocation, "new location");

        node.setLocalTranslation(newLocation);
        this.backgroundZ = newLocation.z;
    }

    /**
     * Alter the padding between the content lines and top/bottom/left edges of
     * the background.
     *
     * @param newPadding the desired padding (in framebuffer pixels, &ge;0,
     * default=5)
     */
    public void setPadding(float newPadding) {
        Validate.nonNegative(newPadding, "new padding");

        if (newPadding != padding) {
            this.padding = newPadding;
            updateContentOffsets();

            Mesh backgroundMesh = createBackgroundMesh();
            background.setMesh(backgroundMesh);
        }
    }

    /**
     * Alter the indexed content line.
     *
     * @param lineIndex which line to modify (&ge;0, &lt;numLines)
     * @param text the desired text (not null)
     * @param color the desired foreground color (not null, unaffected,
     * gamma-encoded)
     */
    public void setText(int lineIndex, String text, ColorRGBA color) {
        Validate.inRange(lineIndex, "line index", 0, numLines);
        Validate.nonNull(text, "text");
        Validate.nonNull(color, "color");

        BitmapText line = contentLines[lineIndex];
        if (!color.equals(contentColors[lineIndex])) {
            line.setColor(color.clone());
            contentColors[lineIndex].set(color);
        }

        line.setText(text);
    }

    /**
     * Alter the width of the background.
     *
     * @param newWidth the desired width (in framebuffer pixels, &gt;0)
     */
    public void setWidth(float newWidth) {
        Validate.positive(newWidth, "new width");

        if (newWidth != width) {
            this.width = newWidth;
            Mesh backgroundMesh = createBackgroundMesh();
            background.setMesh(backgroundMesh);
        }
    }

    /**
     * Alter the horizontal margin between the background and the edges of the
     * viewport.
     *
     * @param newMargin the desired margin (in framebuffer pixels, &ge;0,
     * default=10)
     */
    public void setXMargin(float newMargin) {
        Validate.nonNegative(newMargin, "new margin");

        if (newMargin != xMargin) {
            this.xMargin = newMargin;
            updateLocation();
        }
    }

    /**
     * Alter the vertical margin between the background and the edges of the
     * viewport.
     *
     * @param newMargin the desired margin (in framebuffer pixels, &ge;0,
     * default=10)
     */
    public void setYMargin(float newMargin) {
        Validate.nonNegative(newMargin, "new margin");

        if (newMargin != yMargin) {
            this.yMargin = newMargin;
            updateLocation();
        }
    }

    /**
     * Return width of the background.
     *
     * @return the width (including padding, in framebuffer pixels, &gt;0)
     */
    public float width() {
        assert width > 0f : width;
        return width;
    }

    /**
     * Return the horizontal margin between the background and the edges of the
     * viewport.
     *
     * @return the margin (in framebuffer pixels, &ge;0)
     */
    public float xMargin() {
        assert xMargin >= 0f : xMargin;
        return xMargin;
    }

    /**
     * Return the vertical margin between the background and the edges of the
     * viewport.
     *
     * @return the margin (in framebuffer pixels, &ge;0)
     */
    public float yMargin() {
        assert yMargin >= 0f : yMargin;
        return yMargin;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Enable this overlay, assuming it is initialized and disabled.
     */
    protected void activate() {
        assert isInitialized();
        assert !isEnabled();

        super.setEnabled(true);
        updateLocation();
        guiNode.attachChild(node);
    }

    /**
     * Disable this overlay, assuming it is initialized and enabled.
     */
    protected void deactivate() {
        assert isInitialized();
        assert isEnabled();

        node.removeFromParent();
        super.setEnabled(false);
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
            deactivate();
        }

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
        /*
         * background
         */
        Material material = new Material(assetManager, Materials.UNSHADED);
        background.setMaterial(material);

        Renderer renderer = simpleApplication.getRenderer();
        ColorSpace colorSpace = renderer.isMainFrameBufferSrgb()
                ? ColorSpace.sRGB : ColorSpace.Linear;
        updateBackgroundMaterialColor(colorSpace);
        /*
         * content lines and their colors
         */
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        for (int lineIndex = 0; lineIndex < numLines; ++lineIndex) {
            BitmapText bitmap = new BitmapText(font);
            contentColors[lineIndex] = ColorRGBA.White.clone();
            contentLines[lineIndex] = bitmap;
            node.attachChild(bitmap);
        }
        updateBitmapColors(colorSpace);
        updateContentOffsets();

        if (isEnabled()) {
            activate();
        }
    }

    /**
     * Enable or disable this overlay.
     *
     * @param newSetting true &rarr; activate, false &rarr; deactivate
     */
    @Override
    final public void setEnabled(boolean newSetting) {
        if (newSetting && !isEnabled()) {
            activate();
        } else if (!newSetting && isEnabled()) {
            deactivate();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Create a new background mesh after a change to lineSpacing, padding, or
     * width.
     *
     * @return a new Mesh
     */
    private Mesh createBackgroundMesh() {
        float height = 2 * padding + lineSpacing * numLines;
        float x1 = 0f;
        float x2 = width;
        float y1 = -height;
        float y2 = 0f;
        float zNorm = 1f;
        Mesh result = new RoundedRectangle(x1, x2, y1, y2, padding, zNorm);

        return result;
    }

    /**
     * Adjust the "Color" parameter of the background material for the specified
     * ColorSpace.
     *
     * @param colorSpace the desired ColorSpace (not null)
     */
    private void updateBackgroundMaterialColor(ColorSpace colorSpace) {
        Material material = background.getMaterial();
        ColorRGBA color = DsUtils.renderColor(colorSpace, backgroundColor);
        material.setColor("Color", color);
    }

    /**
     * Adjust the color of the indexed bitmap for the specified ColorSpace.
     *
     * @param lineIndex (&ge;0)
     * @param colorSpace the desired ColorSpace (not null)
     */
    private void updateBitmapColor(int lineIndex, ColorSpace colorSpace) {
        BitmapText bitmap = contentLines[lineIndex];
        ColorRGBA color = contentColors[lineIndex];
        color = DsUtils.renderColor(colorSpace, color);
        bitmap.setColor(color);
    }

    /**
     * Adjust the colors of all bitmaps for the specified ColorSpace.
     *
     * @param colorSpace the desired ColorSpace (not null)
     */
    private void updateBitmapColors(ColorSpace colorSpace) {
        for (int lineIndex = 0; lineIndex < numLines; ++lineIndex) {
            updateBitmapColor(lineIndex, colorSpace);
        }
    }

    /**
     * Update the local translation of each content line after a change to
     * padding, lineSpacing, or contentZOffset.
     */
    private void updateContentOffsets() {
        for (int lineIndex = 0; lineIndex < numLines; ++lineIndex) {
            BitmapText content = contentLines[lineIndex];
            float y = -(padding + lineSpacing * lineIndex);
            content.setLocalTranslation(padding, y, contentZOffset);
        }
    }

    /**
     * Update the location of the Overlay after a change to backgroundZ or
     * margin.
     */
    private void updateLocation() {
        Camera guiCamera = guiViewPort.getCamera();
        int viewPortWidth = guiCamera.getWidth();
        int viewPortHeight = guiCamera.getHeight();
        updateLocation(viewPortWidth, viewPortHeight);
    }

    /**
     * Update the location of the Overlay after a change to backgroundZ, margin,
     * or viewport dimensions.
     */
    private void updateLocation(int viewPortWidth, int viewPortHeight) {
        float topY = viewPortHeight - yMargin;
        Vector3f location = new Vector3f(xMargin, topY, backgroundZ);
        setLocation(location);
    }
}
