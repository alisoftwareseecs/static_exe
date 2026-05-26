package com.staticgame.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.staticgame.utils.GameState;

public class BackgroundRenderer {

    private static final int NR=80, NP=40, NB=12, NPJ=5;

    private float[] rainX=new float[NR],rainY=new float[NR],rainSpd=new float[NR],rainLen=new float[NR];
    private float[] pX=new float[NP],pY=new float[NP],pSpd=new float[NP],pSz=new float[NP];
    private float[] bX=new float[NB],bW=new float[NB],bH=new float[NB];
    private float[] prX=new float[NPJ],prY=new float[NPJ],prW=new float[NPJ],prH=new float[NPJ],prSpd=new float[NPJ];

    private Texture pix;
    private float time=0,camX=0;
    private int mapIdx=0;

    public BackgroundRenderer(){
        Pixmap pm=new Pixmap(1,1,Pixmap.Format.RGBA8888);
        pm.setColor(1,1,1,1);pm.fill();pix=new Texture(pm);pm.dispose();
        seed();
    }

    private void seed(){
        for(int i=0;i<NR;i++){rainX[i]=MathUtils.random(0f,960f);rainY[i]=MathUtils.random(0f,540f);rainSpd[i]=MathUtils.random(200f,460f);rainLen[i]=MathUtils.random(8f,22f);}
        for(int i=0;i<NP;i++){pX[i]=MathUtils.random(0f,960f);pY[i]=MathUtils.random(0f,540f);pSpd[i]=MathUtils.random(8f,32f);pSz[i]=MathUtils.random(1f,3f);}
        float x=20f;
        for(int i=0;i<NB;i++){bW[i]=MathUtils.random(40f,110f);bH[i]=MathUtils.random(55f,200f);bX[i]=x;x+=bW[i]+MathUtils.random(10f,50f);}
        for(int i=0;i<NPJ;i++){prX[i]=MathUtils.random(0f,800f);prY[i]=MathUtils.random(80f,380f);prW[i]=MathUtils.random(80f,260f);prH[i]=MathUtils.random(50f,170f);prSpd[i]=MathUtils.random(-18f,18f);}
    }

    public void update(float worldCamX,float delta,boolean gl,int mapIndex){
        camX=worldCamX;mapIdx=mapIndex;time+=delta;
        for(int i=0;i<NR;i++){rainY[i]-=rainSpd[i]*delta*(gl?2f:1f);rainX[i]+=(gl?MathUtils.random(-1f,1f):0.3f);if(rainY[i]<-30f){rainY[i]=560f;rainX[i]=MathUtils.random(0f,960f);}}
        for(int i=0;i<NP;i++){pY[i]+=pSpd[i]*delta*(gl?2.5f:1f);if(gl)pX[i]+=MathUtils.random(-1.2f,1.2f);if(pY[i]>560f){pY[i]=-10f;pX[i]=MathUtils.random(0f,960f);}}
        for(int i=0;i<NPJ;i++){prX[i]+=prSpd[i]*delta;if(prX[i]>960f||prX[i]<-prW[i])prSpd[i]=-prSpd[i];}
    }

    public void render(SpriteBatch b,int sw,int sh,boolean gl,GameState gs){
        switch(mapIdx){
            case 0:apartment(b,sw,sh,gl);break;
            case 1:university(b,sw,sh,gl);break;
            case 2:hospital(b,sw,sh,gl);break;
        }
    }

    private void apartment(SpriteBatch b,int sw,int sh,boolean gl){
        if(gl){b.setColor(0.01f,0.06f,0.04f,1f);b.draw(pix,0,sh/2,sw,sh/2);b.setColor(0f,0.04f,0.03f,1f);b.draw(pix,0,0,sw,sh/2);}
        else  {b.setColor(0.08f,0.07f,0.13f,1f);b.draw(pix,0,sh/2,sw,sh/2);b.setColor(0.05f,0.04f,0.09f,1f);b.draw(pix,0,0,sw,sh/2);}
        float pOff=(camX*0.13f)%(sw+300f);
        for(int i=0;i<NB;i++){
            float bx=bX[i]-pOff;
            while(bx>sw+20)bx-=sw+320f; while(bx<-bW[i])bx+=sw+320f;
            float by=sh*0.14f;
            b.setColor(gl?0.04f:0.07f,gl?0.22f:0.06f,gl?0.15f:0.10f,0.9f);
            b.draw(pix,bx,by,bW[i],bH[i]);
            int rows=(int)(bH[i]/18f),cols=(int)(bW[i]/12f);
            for(int wr=0;wr<rows;wr++) for(int wc=0;wc<cols;wc++){
                float flk=MathUtils.sin(time*1.2f+bX[i]*0.1f+wr*0.7f+wc*0.5f);
                if(flk>0.1f){
                    if(gl)b.setColor(0.1f,0.9f+flk*0.1f,0.65f,0.5f+flk*0.3f);
                    else  b.setColor(0.9f+flk*0.1f,0.55f+flk*0.2f,0.1f,0.45f+flk*0.25f);
                    b.draw(pix,bx+4f+wc*12f,by+4f+wr*18f,6f,9f);
                }
            }
        }
        b.setColor(gl?0.1f:0.5f,gl?0.9f:0.55f,gl?0.65f:0.75f,0.18f);
        for(int i=0;i<NR;i++)b.draw(pix,rainX[i],rainY[i],1f,rainLen[i]);
        b.setColor(0.4f,0.35f,0.55f,0.16f);b.draw(pix,0,sh*0.10f,sw,sh*0.05f);
        particles(b,gl);b.setColor(Color.WHITE);
    }

    private void university(SpriteBatch b,int sw,int sh,boolean gl){
        if(gl){b.setColor(0.01f,0.05f,0.04f,1f);}else{b.setColor(0.08f,0.09f,0.10f,1f);}
        b.draw(pix,0,0,sw,sh);
        float ff=MathUtils.sin(time*7.3f)>0.8f?0.3f:1f;
        b.setColor(0.85f,0.9f,0.75f,(gl?0.06f:0.12f)*ff);b.draw(pix,0,sh-48,sw,36);
        for(int i=0;i<NPJ;i++){float a=0.04f+0.04f*MathUtils.sin(time*0.8f+i);b.setColor(gl?0.1f:0.8f,gl?0.85f:0.85f,gl?0.6f:0.7f,a);b.draw(pix,prX[i],prY[i],prW[i],prH[i]);}
        float lOff=(camX*0.08f)%(sw+200f);
        for(int i=0;i<16;i++){float lx=i*65f-lOff;while(lx>sw+10)lx-=sw+200f;while(lx<-60)lx+=sw+200f;b.setColor(gl?0.05f:0.12f,gl?0.15f:0.14f,gl?0.1f:0.16f,gl?0.06f:0.10f);b.draw(pix,lx,sh*0.15f,52f,sh*0.5f);}
        particles(b,gl);b.setColor(Color.WHITE);
    }

    private void hospital(SpriteBatch b,int sw,int sh,boolean gl){
        if(gl){b.setColor(0.02f,0.07f,0.05f,1f);}else{b.setColor(0.12f,0.14f,0.13f,1f);}
        b.draw(pix,0,0,sw,sh);
        float gA=gl?0.12f:0.06f;
        b.setColor(gl?0.1f:0.4f,gl?1f:0.55f,gl?0.6f:0.45f,gA);
        for(int y=0;y<sh;y+=40)b.draw(pix,0,y,sw,1);
        for(int x=0;x<sw;x+=40)b.draw(pix,x,0,1,sh);
        if(gl){for(int i=0;i<4;i++){float gx=80f+i*230f-(camX*0.05f)%sw;float ga=0.04f+0.03f*MathUtils.sin(time*1.4f+i*2.1f);b.setColor(0.3f,1f,0.65f,ga);b.draw(pix,gx,sh*0.2f,18f,sh*0.5f);b.draw(pix,gx-6f,sh*0.56f,30f,20f);}}
        float lf=MathUtils.sin(time*11f)>0.7f?0.02f:0.08f;
        b.setColor(0.85f,0.9f,0.8f,gl?lf*0.4f:lf);
        for(int i=0;i<6;i++)b.draw(pix,i*170f,sh-40,120f,28f);
        particles(b,gl);b.setColor(Color.WHITE);
    }

    private void particles(SpriteBatch b,boolean gl){
        for(int i=0;i<NP;i++){float a=0.2f+0.35f*MathUtils.sin(time*1.8f+i);if(gl){if(i%2==0)b.setColor(0.15f,1f,0.65f,a);else b.setColor(1f,0.15f,0.8f,a*0.65f);}else b.setColor(0.5f,0.45f,0.75f,a*0.4f);b.draw(pix,pX[i],pY[i],pSz[i],pSz[i]);}
    }

    public void dispose(){if(pix!=null)pix.dispose();}
}
