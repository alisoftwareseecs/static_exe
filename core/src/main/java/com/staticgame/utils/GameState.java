package com.staticgame.utils;
public class GameState {
    public boolean isGlitched     = false;
    public int     shiftCount     = 0;
    public float   glitchIntensity = 0f;
    public int     truthsFound    = 0;
    public boolean accusedSelf    = false;
    public boolean heardHamza     = false;
    public boolean readNadiaNote  = false;
    public boolean finalMemory    = false;
    public int     currentMap     = 0;
    public boolean[] mapComplete  = { false, false, false };
    public int     endingChoice   = 0;
    public void reset() {
        isGlitched=false; shiftCount=0; glitchIntensity=0f;
        truthsFound=0; accusedSelf=false; heardHamza=false;
        readNadiaNote=false; finalMemory=false;
        currentMap=0; mapComplete=new boolean[]{false,false,false}; endingChoice=0;
    }
    public void recordShift() { shiftCount++; glitchIntensity=Math.min(1f,shiftCount*0.07f); }
    public void foundTruth()  { truthsFound=Math.min(5,truthsFound+1); }
    public void determineEnding() {
        if (shiftCount>=15)                         endingChoice=3;
        else if (truthsFound>=4 && !accusedSelf)    endingChoice=2;
        else                                        endingChoice=1;
    }
}
