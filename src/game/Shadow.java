package game;

import javax.swing.*;

public class Shadow {
    public double x, y;
    public boolean seen;
    public boolean visible;
    private Renderer renderer;

    public Shadow(double x, double y, Renderer renderer) {
        this.x = x;
        this.y = y;
        visible = true;
        seen = false;
        this.renderer = renderer;
    }

    public void reactToPlayer(SoundManager soundManager, int duration) {
        System.out.println("Shadow spotted!");
        soundManager.playSound("shadow", false, true);
        renderer.startHallucination(duration, true);
        Timer timer = new Timer(duration + 1000, e -> {
            visible = false;
            ((Timer) e.getSource()).stop();
        });
        timer.start();
    }

}
