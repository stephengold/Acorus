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
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
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
     * vertical interval between successive content lines (in framebuffer
     * pixels)
     */
    final private static float lineSpacing = 20f;
    /**
     * padding between the content lines and top/bottom/left edges of the
     * background (in framebuffer pixels)
     */
    final private static float padding = 5f;
    /**
     * Z offset of content relative to the background
     */
    final private static float contentZOffset = 0.1f;
    /**
     * message logger for this class
     */
    final static Logger logger = Logger.getLogger(Overlay.class.getName());
    // *************************************************************************
    // fields

    /**
     * lines of text displayed in the overlay ([0] is the top line)
     */
    final private BitmapText[] contentLines;
    /**
     * color of the background
     */
    final private ColorRGBA backgroundColor = new ColorRGBA(0f, 0f, 0f, 1f);
    /**
     * Z coordinate for the background (in case the framebuffer gets resized)
     */
    private float backgroundZ = -2f;
    /**
     * width of the background (in framebuffer pixels, &gt;0)
     */
    private float width;
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
        this.numLines = numLines;
        this.width = width;

        String geometryName = "overlay background for " + getId();
        Mesh backgroundMesh = createBackgroundMesh();
        this.background = new Geometry(geometryName, backgroundMesh);
        node.attachChild(background);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Relocate this overlay for the specified viewport dimensions. The policy
     * is to locate the overlay 10px inward from the upper-left corner of the
     * viewport. TODO alternative policies
     *
     * @param newViewPortWidth the new viewport width (in framebuffer pixels,
     * &gt;0)
     * @param newViewPortHeight the new viewport height (in framebuffer pixels;
     * &gt;0)
     */
    public void resize(int newViewPortWidth, int newViewPortHeight) {
        Validate.positive(newViewPortWidth, "new viewport width");
        Validate.positive(newViewPortHeight, "new viewport height");

        float margin = 10f; // in framebuffer pixels
        Vector3f location
                = new Vector3f(margin, newViewPortHeight - margin, backgroundZ);
        setLocation(location);
    }

    /**
     * Alter the color of the background.
     *
     * @param newColor the desired background color (not null, unaffected,
     * default=opaque black)
     */
    public void setBackgroundColor(ColorRGBA newColor) {
        Validate.nonNull(newColor, "new color");

        backgroundColor.set(newColor);
        if (isInitialized()) {
            Material material = background.getMaterial();
            material.setColor("Color", backgroundColor.clone());
        }
    }

    /**
     * Relocate this overlay.
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
     * Alter the indexed content line.
     *
     * @param lineIndex which line to modify (&ge;0, &lt;numLines)
     * @param text the desired text (not null)
     * @param color the desired foreground color (not null, unaffected)
     */
    public void setText(int lineIndex, String text, ColorRGBA color) {
        Validate.inRange(lineIndex, "line index", 0, numLines);
        Validate.nonNull(text, "text");
        Validate.nonNull(color, "color");

        BitmapText line = contentLines[lineIndex];
        ColorRGBA oldColor = line.getColor();
        if (!color.equals(oldColor)) {
            line.setColor(color.clone());
        }

        line.setText(text);
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

        Camera guiCamera = guiViewPort.getCamera();
        int viewPortWidth = guiCamera.getWidth();
        int viewPortHeight = guiCamera.getHeight();
        resize(viewPortWidth, viewPortHeight);

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
        material.setColor("Color", backgroundColor.clone());
        background.setMaterial(material);
        /*
         * content lines
         */
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        for (int lineIndex = 0; lineIndex < numLines; ++lineIndex) {
            BitmapText bitmap = new BitmapText(font);
            contentLines[lineIndex] = bitmap;
            node.attachChild(bitmap);
        }
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
}
