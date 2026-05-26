package com.staticgame.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.staticgame.utils.GameState;
import com.staticgame.world.World;

public class RealityShiftSystem {

    private boolean glitched = false;

    private float flashTimer  = 0f;
    private float tearTimer   = 0f;
    private float burstTimer  = 0f;
    private float shakeTimer  = 0f;
    private float vhsPhase    = 0f;

    private static final float FLASH_DUR = 0.28f;
    private static final float TEAR_DUR  = 0.75f;
    private static final float BURST_DUR = 0.32f;

    private float shakeX, shakeY;

    private static final int TEARS = 14;
    private float[] tearY=new float[TEARS], tearH=new float[TEARS], tearOff=new float[TEARS];

    private float[] noiseY = new float[35];
    private float   noiseTick = 0f;

    private Texture white;
    private World   world;

    public RealityShiftSystem(World world) {
        this.world = world;
        Pixmap pm  = new Pixmap(1,1,Pixmap.Format.RGBA8888);
        pm.setColor(1,1,1,1); pm.fill();
        white = new Texture(pm); pm.dispose();
        shuffleNoise();
    }

    private void shuffleNoise() {
        int sh = Math.max(1, Gdx.graphics.getHeight());
        for (int i=0;i<noiseY.length;i++) noiseY[i]=MathUtils.random(0,sh);
    }

    public void shift(GameState gs) {
        glitched = !glitched;
        gs.isGlitched = glitched;
        gs.recordShift();
        world.shiftState(glitched);

        flashTimer = FLASH_DUR;
        tearTimer  = TEAR_DUR;
        burstTimer = BURST_DUR;
        shakeTimer = 0.5f;

        int sh = Gdx.graphics.getHeight();
        for (int i=0;i<TEARS;i++) {
            tearY[i]   = MathUtils.random(0, sh);
            tearH[i]   = MathUtils.random(3, 22);
            tearOff[i] = MathUtils.random(-70f, 70f);
        }
    }

    public void update(float delta) {
        if (flashTimer>0) flashTimer-=delta;
        if (tearTimer >0) tearTimer -=delta;
        if (burstTimer>0) burstTimer-=delta;
        vhsPhase += delta*2.1f;
        noiseTick += delta;
        if (noiseTick>0.07f) { noiseTick=0; shuffleNoise(); }

        if (shakeTimer>0) {
            shakeTimer-=delta;
            float m=12f*(shakeTimer/0.5f);
            shakeX=MathUtils.random(-m,m); shakeY=MathUtils.random(-m,m);
        } else { shakeX=0; shakeY=0; }
    }

    public void renderEffects(SpriteBatch batch, GameState gs) {
        int sw=Gdx.graphics.getWidth(), sh=Gdx.graphics.getHeight();

        // ── Pixel burst + chromatic aberration ───────────────────────────────
        if (burstTimer>0) {
            float t=burstTimer/BURST_DUR;
            // R layer left-shifted
            batch.setColor(1f,0f,0f,t*0.55f); batch.draw(white,-8,0,sw,sh);
            // G layer centred
            batch.setColor(0f,1f,0f,t*0.45f); batch.draw(white, 0,0,sw,sh);
            // B layer right-shifted
            batch.setColor(0f,0f,1f,t*0.5f);  batch.draw(white, 8,0,sw,sh);
            batch.setColor(Color.WHITE);

            // Pixel static burst
            int px=(int)(1200*t);
            for (int i=0;i<px;i++) {
                batch.setColor(MathUtils.random(),MathUtils.random(),MathUtils.random(),t*0.85f);
                batch.draw(white,MathUtils.random(0,sw),MathUtils.random(0,sh),
                           MathUtils.random(1f,6f),MathUtils.random(1f,6f));
            }
        }

        // ── Flash ─────────────────────────────────────────────────────────────
        if (flashTimer>0) {
            float ft=flashTimer/FLASH_DUR;
            if (glitched) batch.setColor(0.1f,1f,0.72f,ft*0.5f);
            else          batch.setColor(1f,1f,1f,ft*0.42f);
            batch.draw(white,0,0,sw,sh);
        }

        // ── Screen tears ──────────────────────────────────────────────────────
        if (tearTimer>0) {
            float str=tearTimer/TEAR_DUR;
            for (int i=0;i<TEARS;i++) {
                float a=str*MathUtils.random(0.25f,0.8f);
                if (glitched) batch.setColor(0.1f,0.88f,0.68f,a*0.55f);
                else          batch.setColor(0.88f,0.75f,1f,a*0.45f);
                batch.draw(white, tearOff[i]*str, tearY[i], sw, tearH[i]);
            }
            batch.setColor(Color.WHITE);
        }

        // ── Persistent glitch overlay while in glitch state ───────────────────
        if (glitched) {
            float intensity = 0.035f + gs.glitchIntensity*0.065f;

            // Scanlines
            batch.setColor(0f,0.65f,0.42f,0.05f);
            for (int y=0;y<sh;y+=3) batch.draw(white,0,y,sw,1);

            // VHS tracking wave
            float waveAmp = 6f + gs.glitchIntensity*22f;
            for (int y=0;y<sh;y+=6) {
                float wx=MathUtils.sin(vhsPhase+y*0.038f)*waveAmp;
                if (Math.abs(wx)>2f) {
                    batch.setColor(0.15f,1f,0.65f,0.055f);
                    batch.draw(white, wx, y, sw, 2);
                }
            }

            // Random noise lines
            for (float ny : noiseY) {
                if (MathUtils.random()<gs.glitchIntensity*0.35f+0.08f) {
                    float nw=MathUtils.random(sw*0.08f,sw*0.85f);
                    float nx=MathUtils.random(0,sw-nw);
                    batch.setColor(MathUtils.random(0.1f,1f),MathUtils.random(0f,0.4f),
                                   MathUtils.random(0.4f,1f),MathUtils.random(0.04f,0.22f));
                    batch.draw(white,nx,ny,nw,MathUtils.random(1,4));
                }
            }

            // Glitch block
            if (MathUtils.random()<gs.glitchIntensity*0.12f+0.04f) {
                float bw=MathUtils.random(28f,sw*0.32f), bh=MathUtils.random(4f,28f);
                batch.setColor(MathUtils.random(0f,0.25f),MathUtils.random(0.65f,1f),
                               MathUtils.random(0.45f,1f),MathUtils.random(0.22f,0.52f));
                batch.draw(white,MathUtils.random(0,sw-bw),MathUtils.random(0,sh-bh),bw,bh);
            }

            // Base tint
            batch.setColor(0.04f,0.88f,0.58f,intensity);
            batch.draw(white,0,0,sw,sh);
        }

        batch.setColor(Color.WHITE);
    }

    public float   getShakeX()  { return shakeX; }
    public float   getShakeY()  { return shakeY; }
    public boolean isGlitched() { return glitched; }
    public void    dispose()    { if (white!=null) white.dispose(); }
}
