package com.staticgame.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.staticgame.entities.Player;

public class CameraManager {

    private final OrthographicCamera cam;
    private float minX,minY,maxX,maxY;
    private float targetX,targetY;
    private float shakeX,shakeY,shakeMag,shakeDur,shakeElapsed;
    private static final float LERP = 5.5f;

    public CameraManager(float vw, float vh, float worldW, float worldH) {
        cam = new OrthographicCamera();
        cam.setToOrtho(false, vw, vh);
        clamp(vw,vh,worldW,worldH);
        targetX=minX; targetY=minY;
        cam.position.set(targetX,targetY,0); cam.update();
    }

    private void clamp(float vw,float vh,float ww,float wh) {
        minX=vw/2f; minY=vh/2f;
        maxX=Math.max(minX,ww-vw/2f); maxY=Math.max(minY,wh-vh/2f);
    }

    public void update(Player p, float delta) {
        float dx=p.getX()+p.getWidth()/2f, dy=p.getY()+p.getHeight()/2f;
        // look-ahead up when rising
        if (!p.isGrounded() && p.getVelocityY()>0) dy+=35f*(p.getVelocityY()/520f);
        targetX=MathUtils.lerp(targetX,dx,LERP*delta);
        targetY=MathUtils.lerp(targetY,dy,LERP*delta);
        float cx=MathUtils.clamp(targetX,minX,maxX);
        float cy=MathUtils.clamp(targetY,minY,maxY);

        if (shakeElapsed<shakeDur) {
            shakeElapsed+=delta;
            float rem=shakeMag*(1f-shakeElapsed/shakeDur);
            shakeX=MathUtils.random(-rem,rem); shakeY=MathUtils.random(-rem,rem);
        } else { shakeX=0; shakeY=0; }

        cam.position.set(cx+shakeX, cy+shakeY, 0);
        cam.update();
    }

    public void shake(float mag, float dur) { shakeMag=mag; shakeDur=dur; shakeElapsed=0; }
    public void resize(float vw,float vh,float ww,float wh) { cam.setToOrtho(false,vw,vh); clamp(vw,vh,ww,wh); cam.update(); }

    public OrthographicCamera getCamera() { return cam; }
    public float getCentreX()             { return cam.position.x; }
    public float getCentreY()             { return cam.position.y; }
}
