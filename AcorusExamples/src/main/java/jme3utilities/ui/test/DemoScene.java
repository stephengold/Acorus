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
package jme3utilities.ui.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.input.FlyByCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.util.logging.Logger;
import jme3utilities.MyAsset;

/**
 * Utility class to set up a demo scene containing a mysterious green box on a
 * gray background.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class DemoScene {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final static Logger logger = Logger.getLogger(DemoScene.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private DemoScene() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Set up the demo scene.
     *
     * @param app (not null)
     */
    static void setup(SimpleApplication app) {
        ViewPort viewPort = app.getViewPort();
        ColorRGBA gray = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);
        viewPort.setBackgroundColor(gray);

        addGreenBox(app);
        addLighting(app);
        configureCamera(app);
    }
    // *************************************************************************
    // private methods

    /**
     * Add a mysterious green box at the origin.
     *
     * @param app (not null)
     */
    private static void addGreenBox(SimpleApplication app) {
        float halfExtent = 1f; // mesh units
        Mesh boxMesh = new Box(halfExtent, halfExtent, halfExtent);
        Geometry box = new Geometry("box", boxMesh);
        Node rootNode = app.getRootNode();
        rootNode.attachChild(box);

        AssetManager assetManager = app.getAssetManager();
        ColorRGBA green = new ColorRGBA(0f, 0.3f, 0f, 1f);
        Material material = MyAsset.createShadedMaterial(assetManager, green);
        box.setMaterial(material);
    }

    /**
     * Add lighting for the demo scene.
     *
     * @param app (not null)
     */
    private static void addLighting(SimpleApplication app) {
        ColorRGBA ambientColor = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);
        AmbientLight ambient = new AmbientLight(ambientColor);
        Node rootNode = app.getRootNode();
        rootNode.addLight(ambient);

        Vector3f direction = new Vector3f(1f, -2f, -3f).normalizeLocal();
        DirectionalLight sun = new DirectionalLight(direction);
        rootNode.addLight(sun);
    }

    /**
     * Configure the camera for the demo scene.
     *
     * @param app (not null)
     */
    private static void configureCamera(SimpleApplication app) {
        FlyByCamera flyByCamera = app.getFlyByCamera();
        if (flyByCamera != null) {
            flyByCamera.setDragToRotate(true);
            flyByCamera.setMoveSpeed(5f);
        }

        Camera camera = app.getCamera();
        camera.setLocation(new Vector3f(-4f, 4f, 9f));
        camera.setRotation(new Quaternion(0.038f, 0.96148f, -0.1897f, 0.1951f));
    }
}
