package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

/**
 * Created by kamran.shamloo on 2016-08-10.
 */
/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/



import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class MyCameraInputController extends GestureDetector {
    /** The button for rotating the camera. */
    public int rotateButton = Buttons.LEFT;
    /** The angle to rotate when moved the full width or height of the screen. */
    public float rotateAngle = 360f;
    /** The button for translating the camera along the up/right plane */
    public int translateButton = Buttons.RIGHT;
    /** The units to translate the camera when moved the full width or height of the screen. */
    public float translateUnits = 10f; // FIXME auto calculate this based on the target
    /** The button for translating the camera along the direction axis */
    public int forwardButton = Buttons.MIDDLE;
    /** The key which must be pressed to activate rotate, translate and forward or 0 to always activate. */
    public int activateKey = 0;
    /** Indicates if the activateKey is currently being pressed. */
    protected boolean activatePressed;
    /** Whether scrolling requires the activeKey to be pressed (false) or always allow scrolling (true). */
    public boolean alwaysScroll = true;
    /** The weight for each scrolled amount. */
    public float scrollFactor = -0.1f;
    /** World units per screen size */
    public float pinchZoomFactor = 10f;
    /** Whether to update the camera after it has been changed. */
    public boolean autoUpdate = true;
    /** The target to rotate around. */
    public Vector3 target = new Vector3();
    /** Whether to update the target on translation */
    public boolean translateTarget = true;
    /** Whether to update the target on forward */
    public boolean forwardTarget = true;
    /** Whether to update the target on scroll */
    public boolean scrollTarget = false;
    public int forwardKey = Keys.W;
    protected boolean forwardPressed;
    public int backwardKey = Keys.S;
    protected boolean backwardPressed;
    public int rotateRightKey = Keys.A;
    protected boolean rotateRightPressed;
    public int rotateLeftKey = Keys.D;
    protected boolean rotateLeftPressed;
    /** The camera. */
    public Camera camera;
    /** The current (first) button being pressed. */
    protected int button = -1;

    private float startX, startY;
    private final Vector3 tmpV1 = new Vector3();
    private final Vector3 tmpV2 = new Vector3();

    protected static class MyCameraGestureListener extends GestureAdapter {
        public MyCameraInputController controller;
        private float previousZoom;
        private float previousAngle;

        @Override
        public boolean touchDown (float x, float y, int pointer, int button) {
            previousZoom = 0;
            previousAngle = 0;
            return false;
        }

        @Override
        public boolean tap (float x, float y, int count, int button) {
            return false;
        }

        @Override
        public boolean longPress (float x, float y) {
            return false;
        }

        @Override
        public boolean fling (float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan (float x, float y, float deltaX, float deltaY) {
            return false;
        }

        @Override
        public boolean zoom (float initialDistance, float distance) {
            float newZoom = distance - initialDistance;
            float amount = newZoom - previousZoom;
            previousZoom = newZoom;
            float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
            return controller.pinchZoom(amount / ((w > h) ? h : w));
        }

        @Override
        public boolean pinch (Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {

            float totalRotationAngle;
            Vector2 v1 = new Vector2(initialPointer2.x - initialPointer1.x,
                    initialPointer2.y - initialPointer1.y);
            Vector2 v2 = new Vector2(pointer2.x - pointer1.x, pointer2.y - pointer1.y);

            double initialAngle = Math.atan2(v1.y, v1.x);
            double currentAngle = Math.atan2(v2.y, v2.x);
            totalRotationAngle = (float)Math.toDegrees(initialAngle - currentAngle);

            //Vector3 v3 = new Vector3(0,0, v1.crs(v2));// v1.crs(v2) = v1 * v2 = k(v1.x*v2.y - v1.y * v2.x);

            //totalRotationAngle = (float)Math.asin(v3/(v1.len()*v2.len()))*180.0f/(float)Math.PI;
            //totalRotationAngle = 180.0f/(float)Math.PI*(float)Math.atan(v3.len()/Vector2.dot(v1.x,v1.y,v2.x,v2.y));
             //totalRotationAngle = 180.0f/(float)Math.PI*(float)Math.atan2(v3.len(),v2.dot(v2));

            float rotationAngle = (totalRotationAngle - previousAngle);
//            if (angle < -180.f) angle += 360.0f;
//            if (angle > 180.f) angle -= 360.0f;
            previousAngle = totalRotationAngle;
            return controller.rotate(rotationAngle);
        }
    };

    protected final MyCameraGestureListener gestureListener;

    protected MyCameraInputController (final MyCameraGestureListener gestureListener, final Camera camera) {
        super(gestureListener);
        this.gestureListener = gestureListener;
        this.gestureListener.controller = this;
        this.camera = camera;
    }

    public MyCameraInputController (final Camera camera) {
        this(new MyCameraGestureListener(), camera);
    }

    public void update () {
        if (rotateRightPressed || rotateLeftPressed || forwardPressed || backwardPressed) {
            final float delta = Gdx.graphics.getDeltaTime();
            if (rotateRightPressed) camera.rotate(camera.up, -delta * rotateAngle);
            if (rotateLeftPressed) camera.rotate(camera.up, delta * rotateAngle);
            if (forwardPressed) {
                camera.translate(tmpV1.set(camera.direction).scl(delta * translateUnits));
                if (forwardTarget) target.add(tmpV1);
            }
            if (backwardPressed) {
                camera.translate(tmpV1.set(camera.direction).scl(-delta * translateUnits));
                if (forwardTarget) target.add(tmpV1);
            }
            if (autoUpdate) camera.update();
        }
    }

    private int touched;
    private boolean multiTouch;

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        touched |= (1 << pointer);
        multiTouch = !MathUtils.isPowerOfTwo(touched);
        if (multiTouch)
            this.button = -1;
        else if (this.button < 0 && (activateKey == 0 || activatePressed)) {
            startX = screenX;
            startY = screenY;
            this.button = button;
        }
        return super.touchDown(screenX, screenY, pointer, button) || (activateKey == 0 || activatePressed);
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        touched &= -1 ^ (1 << pointer);
        multiTouch = !MathUtils.isPowerOfTwo(touched);
        if (button == this.button) this.button = -1;
        return super.touchUp(screenX, screenY, pointer, button) || activatePressed;
    }

    protected boolean process (float deltaX, float deltaY, int button) {
        if (button == rotateButton) {
            tmpV1.set(camera.direction).crs(camera.up);//.y = 0f;

            camera.rotateAround(target, tmpV1.nor(), deltaY * rotateAngle);//originally it was tmpV1.nor() instead of Vector3.X
            camera.rotateAround(target, camera.up, deltaX * -rotateAngle); // originally Vector3.y
        } else if (button == translateButton) {
            camera.translate(tmpV1.set(camera.direction).crs(camera.up).nor().scl(-deltaX * translateUnits));
            camera.translate(tmpV2.set(camera.up).scl(-deltaY * translateUnits));
            if (translateTarget) target.add(tmpV1).add(tmpV2);
        } else if (button == forwardButton) {
            camera.translate(tmpV1.set(camera.direction).scl(deltaY * translateUnits));
            if (forwardTarget) target.add(tmpV1);
        }
        if (autoUpdate) camera.update();
        return true;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        boolean result = super.touchDragged(screenX, screenY, pointer);
        if (result || this.button < 0) return result;
        final float deltaX = (screenX - startX) / Gdx.graphics.getWidth();
        final float deltaY = (startY - screenY) / Gdx.graphics.getHeight();
        startX = screenX;
        startY = screenY;
        return process(deltaX, deltaY, button);
    }

    @Override
    public boolean scrolled (int amount) {
        return zoom(amount * scrollFactor * translateUnits);
    }

    public boolean zoom (float amount) {
        if (!alwaysScroll && activateKey != 0 && !activatePressed) return false;
        camera.translate(tmpV1.set(camera.direction).scl(amount));
        if (scrollTarget) target.add(tmpV1);
        if (autoUpdate) camera.update();
        return true;
    }

    protected boolean pinchZoom (float amount) {
        return zoom(pinchZoomFactor * amount);
    }
    public boolean rotate(float angle) {
            if (!alwaysScroll && activateKey != 0 && !activatePressed) return false;
            camera.rotateAround(Vector3.Zero, camera.direction, angle);
            //camera.rotateAround(Vector3.Zero, camera.direction, 1); // rotates around z opposite to right-hand rule.
            if (autoUpdate) camera.update();
            return true;

    }

    @Override
    public boolean keyDown (int keycode) {
        if (keycode == activateKey) activatePressed = true;
        if (keycode == forwardKey)
            forwardPressed = true;
        else if (keycode == backwardKey)
            backwardPressed = true;
        else if (keycode == rotateRightKey)
            rotateRightPressed = true;
        else if (keycode == rotateLeftKey) rotateLeftPressed = true;
        return false;
    }

    @Override
    public boolean keyUp (int keycode) {
        if (keycode == activateKey) {
            activatePressed = false;
            button = -1;
        }
        if (keycode == forwardKey)
            forwardPressed = false;
        else if (keycode == backwardKey)
            backwardPressed = false;
        else if (keycode == rotateRightKey)
            rotateRightPressed = false;
        else if (keycode == rotateLeftKey) rotateLeftPressed = false;
        return false;
    }
}

