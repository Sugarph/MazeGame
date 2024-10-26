package game;

public class Shadow {
    public double x, y;
    public boolean seen;
    private Renderer renderer;

    public Shadow(double x, double y, Renderer renderer) {
        this.x = x;
        this.y = y;
        seen = false;
        this.renderer = renderer;
    }

    public void reactToPlayer(SoundManager soundManager) {
        System.out.println("Shadow spotted!");
        soundManager.playSound("shadow", false, true);
        renderer.startHallucination();
    }

}
