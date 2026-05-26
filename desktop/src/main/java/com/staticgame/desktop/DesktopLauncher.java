package com.staticgame.desktop;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.staticgame.StaticGame;
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Static.exe");
        cfg.setWindowedMode(960, 540);
        cfg.setForegroundFPS(60);
        cfg.setResizable(false);
        new Lwjgl3Application(new StaticGame(), cfg);
    }
}
