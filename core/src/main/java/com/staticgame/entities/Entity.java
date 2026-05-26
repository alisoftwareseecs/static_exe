package com.staticgame.entities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {
    protected float   x, y, width, height;
    protected Texture texture;
    protected boolean isGlitched = false;
    protected float   velocityX  = 0, velocityY = 0;

    public Entity(float x, float y, float w, float h) {
        this.x=x; this.y=y; this.width=w; this.height=h;
    }

    public abstract void update(float delta);
    public abstract void render(SpriteBatch batch);

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    protected Texture colorTex(int r, int g, int b) {
        Pixmap pm = new Pixmap(1,1,Pixmap.Format.RGBA8888);
        pm.setColor(r/255f,g/255f,b/255f,1f); pm.fill();
        Texture t = new Texture(pm); pm.dispose(); return t;
    }

    public void setGlitched(boolean v)       { isGlitched = v; }
    public void setPosition(float x, float y){ this.x=x; this.y=y; }
    public float getX()      { return x; }
    public float getY()      { return y; }
    public float getWidth()  { return width; }
    public float getHeight() { return height; }
    public void  dispose()   { if (texture!=null) texture.dispose(); }
}
