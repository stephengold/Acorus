/*
 Copyright (c) 2022-2025 Stephen Gold

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

import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * Notify an AcorusDemo regarding changes to ViewPort dimensions.
 * <p>
 * A named class is used in order to clarify RenderManager dumps.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class AcorusProcessor implements SceneProcessor {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(AcorusProcessor.class.getName());
    // *************************************************************************
    // fields

    /**
     * application to notify
     */
    final private AcorusDemo demoApp;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a processor to notify the specified app.
     *
     * @param demoApp the app to notify (not null)
     */
    AcorusProcessor(AcorusDemo demoApp) {
        Validate.nonNull(demoApp, "demo app");
        this.demoApp = demoApp;
    }
    // *************************************************************************
    // SceneProcessor methods

    /**
     * Invoked when the processor is removed from a RenderManager.
     */
    @Override
    public void cleanup() {
        // do nothing
    }

    /**
     * Initialize the processor.
     *
     * @param rm the RenderManager to which the processor was added
     * @param unused2 the ViewPort to which the processor is assigned
     */
    @Override
    public void initialize(RenderManager rm, ViewPort unused2) {
        // do nothing
    }

    /**
     * Test whether the processor has been initialized.
     *
     * @return true if initialized, otherwise false
     */
    @Override
    public boolean isInitialized() {
        return true;
    }

    /**
     * Invoked after a frame has been rendered and the queue flushed.
     *
     * @param unused the buffer to which the scene was rendered
     */
    @Override
    public void postFrame(FrameBuffer unused) {
        // do nothing
    }

    /**
     * Invoked after the scene graph has been queued, but before it is flushed.
     *
     * @param unused the queue to which the scene was rendered
     */
    @Override
    public void postQueue(RenderQueue unused) {
        // do nothing
    }

    /**
     * Invoked before a frame
     *
     * @param tpf the time per frame (in seconds, &ge;0)
     */
    @Override
    public void preFrame(float tpf) {
        // do nothing
    }

    /**
     * Invoked when the resolution of the ViewPort has changed.
     *
     * @param unused the affected ViewPort
     * @param newWidth the new width (in pixels)
     * @param newHeight the new height (in pixels)
     */
    @Override
    public void reshape(ViewPort unused, int newWidth, int newHeight) {
        //System.out.println("reshape " + newWidth + "x" + newHeight);
        //System.out.flush();
        demoApp.onViewPortResize(newWidth, newHeight);
    }

    /**
     * Alters the processor's profiler instance.
     *
     * @param unused the desired profiler instance
     */
    @Override
    public void setProfiler(AppProfiler unused) {
        // do nothing
    }
}
