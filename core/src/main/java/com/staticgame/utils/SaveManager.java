package com.staticgame.utils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
public class SaveManager {
    private static final String PREFS = "static_exe_v2";
    private final Preferences p;
    public SaveManager() { p = Gdx.app.getPreferences(PREFS); }
    public void saveRun(GameState gs) {
        p.putInteger("sessions", p.getInteger("sessions",0)+1);
        p.putFloat("volume", p.getFloat("volume",0.6f));
        if (gs.endingChoice==2) p.putBoolean("ending_truth",true);
        if (gs.endingChoice==1) p.putBoolean("ending_guilt",true);
        if (gs.endingChoice==3) p.putBoolean("ending_fracture",true);
        p.putInteger("max_truths", Math.max(p.getInteger("max_truths",0), gs.truthsFound));
        p.flush();
    }
    public void saveVolume(float v) { p.putFloat("volume",v); p.flush(); }
    public float loadVolume()   { return p.getFloat("volume",0.6f); }
    public int   loadSessions() { return p.getInteger("sessions",0); }
    public int   endingsFound() {
        int n=0;
        if(p.getBoolean("ending_truth",false))    n++;
        if(p.getBoolean("ending_guilt",false))    n++;
        if(p.getBoolean("ending_fracture",false)) n++;
        return n;
    }
    public String getSummary() {
        return "Runs: "+loadSessions()+"   Endings found: "+endingsFound()+"/3   Truths: "+p.getInteger("max_truths",0)+"/5";
    }
}
