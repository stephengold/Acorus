/*
 Copyright (c) 2022-2023, Stephen Gold

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
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.texture.image.ColorSpace;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyColor;
import jme3utilities.mesh.RoundedRectangle;

/**
 * An AppState for displaying "content" (lines of text) against a rectangular
 * background with rounded corners.
 * <p>
 * By default, an Overlay is located in the upper-left corner of the display and
 * the text is white and left-aligned on a black background.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Overlay extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final static Logger loggerO = Logger.getLogger(Overlay.class.getName());
    // *************************************************************************
    // fields

    /**
     * horizontal alignment of each content line
     */
    final private BitmapFont.Align[] contentAlignments;
    /**
     * lines of text content displayed in the overlay ([0] is the top line)
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
     * padding between the content lines and the edges of the background (in
     * framebuffer pixels, &ge;0)
     */
    private float padding = 5f;
    /**
     * width of the background, including padding (in framebuffer pixels, &gt;0)
     */
    private float width;
    /**
     * horizontal margin between the background and the edges of the viewport
     * (in framebuffer pixels, &ge;0) Ignored if locationPolicy=Center.
     */
    private float xMargin = 5f;
    /**
     * vertical margin between the background and the edges of the viewport (in
     * framebuffer pixels, &ge;0) Ignored if locationPolicy=Center.
     */
    private float yMargin = 5f;
    /**
     * rounded-rectangle geometry to ensure content visibility
     */
    final private Geometry background;
    /**
     * location policy for the background, relative to viewport edges (not null)
     */
    private LocationPolicy locationPolicy = LocationPolicy.UpperLeft;
    /**
     * node to attach (to the application's GUI node) It's corresponds to the
     * upper-left corner of the background.
     */
    final private Node node;
    /**
     * text of each content line
     */
    final private String[] contentStrings;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized AppState.
     *
     * @param id the desired AppState ID (not null, unique within the current
     * application)
     * @param width the desired width of the background in framebuffer pixels
     * (&gt;0)
     * @param numLines the desired number of content lines (&gt;0)
     */
    public Overlay(String id, float width, int numLines) {
        super(id);
        Validate.nonNull(id, "name");
        Validate.positive(width, "width");
        Validate.positive(numLines, "number of lines");

        this.setEnabled(false);
        this.node = new Node("overlay node for " + id);

        this.contentAlignments = new BitmapFont.Align[numLines];
        this.contentLines = new BitmapText[numLines];
        this.contentColors = new ColorRGBA[numLines];
        this.contentStrings = new String[numLines];
        for (int lineIndex = 0; lineIndex < numLines; ++lineIndex) {
            this.contentAlignments[lineIndex] = BitmapFont.Align.Left;
            // contentLines are created by initialize()
            this.contentColors[lineIndex] = ColorRGBA.White.clone();
            this.contentStrings[lineIndex] = "";
        }
        this.width = width;

        String geometryName = "overlay background for " + id;
        Mesh backgroundMesh = createBackgroundMesh();
        this.background = new Geometry(geometryName, backgroundMesh);
        node.attachChild(background);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the horizontal alignment of the indexed content line.
     *
     * @param lineIndex which content line to access (&ge;0, &lt;numLines)
     * @return the enum value (not null)
     */
    public BitmapFont.Align alignment(int lineIndex) {
        int numLines = countLines();
        Validate.inRange(lineIndex, "line index", 0, numLines - 1);

        BitmapFont.Align result = contentAlignments[lineIndex];
        return result;
    }

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
     * Determine the number of the content lines.
     *
     * @return the count (&gt;0)
     */
    public int countLines() {
        int result = contentLines.length;
        assert result > 0 : result;
        return result;
    }

    /**
     * Return the location policy.
     *
     * @return the enum value (not null)
     */
    public LocationPolicy getLocationPolicy() {
        assert locationPolicy != null;
        return locationPolicy;
    }

    /**
     * Return height of the background.
     *
     * @return the height (including padding, in framebuffer pixels, &gt;0)
     */
    public float height() {
        int numLines = countLines();
        float result = 2 * padding + lineSpacing * numLines;

        assert result > 0f : result;
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
     * Relocate this overlay for the specified viewport dimensions.
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
     * top/bottom/left/right edges of the background.
     *
     * @return the padding (in framebuffer pixels, &ge;0)
     */
    public float padding() {
        assert padding >= 0f : padding;
        return padding;
    }

    /**
     * Alter the horizontal alignment of the indexed content line.
     *
     * @param lineIndex which line to modify (&ge;0, &lt;numLines)
     * @param alignment the desired alignment (not null, default=Left)
     */
    public void setAlignment(int lineIndex, BitmapFont.Align alignment) {
        int numLines = countLines();
        Validate.inRange(lineIndex, "line index", 0, numLines - 1);
        Validate.nonNull(alignment, "alignment");

        if (alignment != contentAlignments[lineIndex]) {
            this.contentAlignments[lineIndex] = alignment;
            if (isInitialized()) {
                updateContentOffset(lineIndex);
            }
        }
    }

    /**
     * Alter the horizontal alignment of all content.
     *
     * @param alignment the desired alignment (not null, default=Left)
     */
    public void setAlignmentAll(BitmapFont.Align alignment) {
        Validate.nonNull(alignment, "alignment");

        int numLines = countLines();
        for (int lineIndex = 0; lineIndex < numLines; ++lineIndex) {
            this.contentAlignments[lineIndex] = alignment;
        }

        if (isInitialized()) {
            updateContentOffsets();
        }
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
            Application application = getApplication();
            Renderer renderer = application.getRenderer();
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
        if (newZ != backgroundZ) {
            this.backgroundZ = newZ;
            updateLocation();
        }
    }

    /**
     * Alter the color of the indexed content line.
     *
     * @param lineIndex which line to modify (&ge;0, &lt;countLines)
     * @param color the desired foreground color (not null, unaffected,
     * gamma-encoded, default=White)
     */
    public void setColor(int lineIndex, ColorRGBA color) {
        int numLines = countLines();
        Validate.inRange(lineIndex, "line index", 0, numLines - 1);
        Validate.nonNull(color, "color");

        contentColors[lineIndex].set(color);
        if (isInitialized()) {
            BitmapText line = contentLines[lineIndex];
            line.setColor(color.clone());
        }
    }

    /**
     * Alter the Z offset of content relative to the background.
     *
     * @param newOffset the desired offset (&gt;0, default=0.1)
     */
    public void setContentZOffset(float newOffset) {
        Validate.positive(newOffset, "new offset");

        if (newOffset != contentZOffset) {
            this.contentZOffset = newOffset;
            if (isInitialized()) {
                updateContentOffsets();
            }
        }
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
            if (isInitialized()) {
                updateContentOffsets();
            }

            Mesh backgroundMesh = createBackgroundMesh();
            background.setMesh(backgroundMesh);
        }
    }

    /**
     * Move to the specified location.
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
     * Alter the location policy.
     *
     * @param newPolicy the desired policy (not null, default=UpperLeft)
     */
    public void setLocationPolicy(LocationPolicy newPolicy) {
        Validate.nonNull(newPolicy, "new policy");

        this.locationPolicy = newPolicy;
        updateLocation();
    }

    /**
     * Alter the padding between the content lines and the edges of the
     * background.
     *
     * @param newPadding the desired padding (in framebuffer pixels, &ge;0,
     * default=5)
     */
    public void setPadding(float newPadding) {
        Validate.nonNegative(newPadding, "new padding");

        if (newPadding != padding) {
            this.padding = newPadding;
            if (isInitialized()) {
                updateContentOffsets();
            }

            Mesh backgroundMesh = createBackgroundMesh();
            background.setMesh(backgroundMesh);
        }
    }

    /**
     * Alter the text of the indexed content line.
     *
     * @param lineIndex which line to modify (&ge;0, &lt;countLines)
     * @param text the desired text (not null)
     */
    public void setText(int lineIndex, String text) {
        int numLines = countLines();
        Validate.inRange(lineIndex, "line index", 0, numLines - 1);
        Validate.nonNull(text, "text");

        this.contentStrings[lineIndex] = text;
        if (isInitialized()) {
            BitmapText line = contentLines[lineIndex];
            line.setText(text);
            if (contentAlignments[lineIndex] != BitmapFont.Align.Left) {
                updateContentOffset(lineIndex);
            }
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
        int numLines = countLines();
        Validate.inRange(lineIndex, "line index", 0, numLines - 1);
        Validate.nonNull(text, "text");
        Validate.nonNull(color, "color");

        this.contentColors[lineIndex].set(color);
        this.contentStrings[lineIndex] = text;
        if (isInitialized()) {
            BitmapText line = contentLines[lineIndex];
            line.setColor(color.clone());
            line.setText(text);
            if (contentAlignments[lineIndex] != BitmapFont.Align.Left) {
                updateContentOffset(lineIndex);
            }
        }
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
     * viewport. (Ignored if the location policy is Center.)
     *
     * @param newMargin the desired margin (in framebuffer pixels, &ge;0,
     * default=5)
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
     * viewport. (Ignored if the location policy is Center.)
     *
     * @param newMargin the desired margin (in framebuffer pixels, &ge;0,
     * default=5)
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
    // BaseAppState methods

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
    protected void cleanup(Application application) {
        // do nothing
    }

    /**
     * Initialize this AppState prior to its first update. Should be invoked
     * only by a subclass or by the AppStateManager.
     *
     * @param application the application which owns this AppState (not null)
     */
    @Override
    protected void initialize(Application application) {
        // background
        AssetManager assetManager = application.getAssetManager();
        Material material = new Material(assetManager, Materials.UNSHADED);
        background.setMaterial(material);

        Renderer renderer = application.getRenderer();
        ColorSpace colorSpace = renderer.isMainFrameBufferSrgb()
                ? ColorSpace.sRGB : ColorSpace.Linear;
        updateBackgroundMaterialColor(colorSpace);

        // content lines
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        int numLines = countLines();
        for (int lineIndex = 0; lineIndex < numLines; ++lineIndex) {
            BitmapText bitmap = new BitmapText(font);
            this.contentLines[lineIndex] = bitmap;
            node.attachChild(bitmap);

            bitmap.setName("line [" + lineIndex + "]");
            String text = contentStrings[lineIndex];
            bitmap.setText(text);
        }
        updateBitmapColors(colorSpace);
        updateContentOffsets();
    }

    /**
     * Transition this AppState from enabled to disabled.
     */
    @Override
    protected void onDisable() {
        node.removeFromParent();
    }

    /**
     * Transition this AppState from disabled to enabled.
     */
    @Override
    protected void onEnable() {
        updateLocation();

        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        Node guiNode = simpleApp.getGuiNode();
        guiNode.attachChild(node);
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
        float height = height();
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
        ColorRGBA color = MyColor.renderColor(colorSpace, backgroundColor);
        material.setColor("Color", color);
    }

    /**
     * Adjust the indexed content line for a new ColorSpace.
     *
     * @param lineIndex (&ge;0)
     * @param colorSpace the desired ColorSpace (not null)
     */
    private void updateBitmapColor(int lineIndex, ColorSpace colorSpace) {
        BitmapText bitmap = contentLines[lineIndex];
        ColorRGBA color = contentColors[lineIndex];
        color = MyColor.renderColor(colorSpace, color);
        bitmap.setColor(color);
    }

    /**
     * Adjust every content line for a new ColorSpace.
     *
     * @param colorSpace the desired ColorSpace (not null)
     */
    private void updateBitmapColors(ColorSpace colorSpace) {
        int numLines = countLines();
        for (int lineIndex = 0; lineIndex < numLines; ++lineIndex) {
            updateBitmapColor(lineIndex, colorSpace);
        }
    }

    /**
     * Update the local translation of the indexed content line.
     *
     * @param lineIndex (&ge;0)
     */
    private void updateContentOffset(int lineIndex) {
        BitmapFont.Align hAlign = contentAlignments[lineIndex];
        BitmapText line = contentLines[lineIndex];

        float xOffset;
        float textWidth;
        switch (hAlign) {
            case Left:
                xOffset = padding;
                break;
            case Center:
                textWidth = line.getLineWidth();
                xOffset = (width - textWidth) / 2f; // TODO rounding
                break;
            case Right:
                textWidth = line.getLineWidth();
                xOffset = width - padding - textWidth;
                break;
            default:
                throw new IllegalStateException("hAlign = " + hAlign);
        }

        float yOffset = -(padding + lineSpacing * lineIndex);
        line.setLocalTranslation(xOffset, yOffset, contentZOffset);
    }

    /**
     * Update the local translation of every content line.
     */
    private void updateContentOffsets() {
        int numLines = countLines();
        for (int lineIndex = 0; lineIndex < numLines; ++lineIndex) {
            updateContentOffset(lineIndex);
        }
    }

    /**
     * Update the location of the Overlay after a change.
     */
    private void updateLocation() {
        if (isInitialized()) {
            Application application = getApplication();
            ViewPort guiViewPort = application.getGuiViewPort();
            Camera guiCamera = guiViewPort.getCamera();
            int viewPortWidth = guiCamera.getWidth();
            int viewPortHeight = guiCamera.getHeight();
            updateLocation(viewPortWidth, viewPortHeight);
        }
    }

    /**
     * Update the location of the Overlay after a change.
     *
     * @param viewPortWidth the viewport width (in framebuffer pixels, &gt;0)
     * @param viewPortHeight the viewport height (in framebuffer pixels, &gt;0)
     */
    private void updateLocation(int viewPortWidth, int viewPortHeight) {
        assert viewPortWidth > 0f : viewPortWidth;
        assert viewPortHeight > 0f : viewPortHeight;
        assert locationPolicy != null;

        float height = height();
        float leftX;
        float topY;
        switch (locationPolicy) {
            case Center:
                leftX = (viewPortWidth - width) / 2;
                topY = (viewPortHeight + height) / 2;
                break;
            case CenterLeft:
                topY = (viewPortHeight + height) / 2;
                leftX = xMargin;
                break;
            case LowerLeft:
                leftX = xMargin;
                topY = height + yMargin;
                break;
            case UpperLeft:
                leftX = xMargin;
                topY = viewPortHeight - yMargin;
                break;
            default:
                String message = "locationPolicy = " + locationPolicy;
                throw new IllegalStateException(message);
        }

        Vector3f location = new Vector3f(leftX, topY, backgroundZ);
        setLocation(location);
    }
}
