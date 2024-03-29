/*
 Copyright (c) 2018-2023, Stephen Gold

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

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.logging.Logger;
import jme3utilities.InitialState;
import jme3utilities.Validate;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.ReadXZ;
import jme3utilities.math.VectorXZ;

/**
 * An AppState to cause a camera to orbit a vertical axis.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CameraOrbitAppState extends AcorusAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(CameraOrbitAppState.class.getName());
    /**
     * local copy of {@link com.jme3.math.Vector3f#UNIT_Y}
     */
    final private static Vector3f unitY = new Vector3f(0f, 1f, 0f);
    // *************************************************************************
    // fields

    /**
     * camera being controlled (not null)
     */
    final private Camera camera;
    /**
     * angular rate for orbiting (in radians/second, default=1)
     */
    private float angularRate = 1f;
    /**
     * center of the orbit in the X-Z plane (in world coordinates, not null,
     * default=0,0)
     */
    private ReadXZ centerXZ = VectorXZ.zero;
    /**
     * name of the signal to orbit in the counter-clockwise (+Y) direction
     */
    final private String ccwSignalName;
    /**
     * name of the signal to orbit in the clockwise (-Y) direction
     */
    final private String cwSignalName;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled AppState.
     *
     * @param camera which camera to control (not null)
     * @param ccwSignalName the signal name to orbit in the +Y direction (not
     * null, not empty)
     * @param cwSignalName the signal name to orbit in the -Y direction (not
     * null, not empty)
     */
    public CameraOrbitAppState(
            Camera camera, String ccwSignalName, String cwSignalName) {
        super(InitialState.Enabled);
        Validate.nonNull(camera, "camera");
        Validate.nonEmpty(ccwSignalName, "counter-clockwise signal name");
        Validate.nonEmpty(cwSignalName, "clockwise signal name");

        this.camera = camera;
        this.ccwSignalName = ccwSignalName;
        this.cwSignalName = cwSignalName;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the signal name for orbiting in the counter-clockwise (+Y)
     * direction.
     *
     * @return the name (not null, not empty)
     */
    public String ccwSignalName() {
        assert ccwSignalName != null;
        assert !ccwSignalName.isEmpty();
        return ccwSignalName;
    }

    /**
     * Determine the center location.
     *
     * @return a location vector (not null)
     */
    public ReadXZ center() {
        assert centerXZ != null;
        return centerXZ;
    }

    /**
     * Determine the signal name for orbiting in the clockwise (-Y) direction.
     *
     * @return the name (not null, not empty)
     */
    public String cwSignalName() {
        assert cwSignalName != null;
        assert !cwSignalName.isEmpty();
        return cwSignalName;
    }

    /**
     * Determine the rate of orbiting.
     *
     * @return the angular rate (in radians/second, &ge;0, default=1)
     */
    public float rate() {
        assert angularRate >= 0f : angularRate;
        return angularRate;
    }

    /**
     * Alter the center of the orbit.
     *
     * @param desiredCenter the desired center (in world coordinates, not null,
     * default=0,0)
     */
    public void setCenter(ReadXZ desiredCenter) {
        Validate.nonNull(desiredCenter, "desired center");
        this.centerXZ = desiredCenter;
    }

    /**
     * Alter the rate of orbiting.
     *
     * @param desiredRate the desired angular rate (in radians/second, &ge;0)
     */
    public void setRate(float desiredRate) {
        Validate.nonNegative(desiredRate, "desired rate");
        this.angularRate = desiredRate;
    }
    // *************************************************************************
    // AcorusAppState methods

    /**
     * Callback invoked once per frame while the state is attached and enabled.
     *
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        float orbitAngle = 0f;
        Signals uiSignals = getSignals();
        if (uiSignals.test(ccwSignalName)) {
            orbitAngle += tpf;
        }
        if (uiSignals.test(cwSignalName)) {
            orbitAngle -= tpf;
        }
        if (orbitAngle != 0f) {
            float appSpeed = getSpeed();
            orbitAngle *= angularRate / appSpeed;
            orbitCamera(orbitAngle);
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Orbit the camera around the vertical axis.
     *
     * @param angle the amount to orbit (counter-clockwise, in radians)
     */
    private void orbitCamera(float angle) {
        Quaternion rotate = new Quaternion();
        rotate.fromAngles(0f, angle, 0f);

        Vector3f location = camera.getLocation().clone();
        Vector3f center = centerXZ.toVector3f(location.y);
        Vector3f xzOffset = location.subtract(center);
        MyQuaternion.rotate(rotate, xzOffset, xzOffset);
        center.add(xzOffset, location);
        camera.setLocation(location);

        Vector3f camDirection = camera.getDirection();
        MyQuaternion.rotate(rotate, camDirection, camDirection);
        camera.lookAtDirection(camDirection, unitY);
    }
}
