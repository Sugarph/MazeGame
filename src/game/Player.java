package game;

public class Player {
    public double x, y, angle, deltaX, deltaY, pitch, health;
    public boolean moved;


    public Player(double startX, double startY, double startAngle) {
        health = 100;
        x = startX;
        y = startY;
        angle = startAngle;
        deltaX = Math.cos(Math.toRadians(angle));
        deltaY = -Math.sin(Math.toRadians(angle));
        moved = false;
    }

    public void hurt(double damage) {
        health -= damage;
    }

    public void updatePitch(double deltaPitch) {
        pitch += deltaPitch;
        pitch = Math.max(-120, Math.min(160, pitch));
    }

    public void updateAngle(double deltaAngle) {
        angle += deltaAngle;
        if (angle > 360) angle -= 360;
        if (angle < 0) angle += 360;
        deltaX = Math.cos(Math.toRadians(angle));
        deltaY = -Math.sin(Math.toRadians(angle));
    }

    public void moveForward(double speed) {
        x += deltaX * speed;
        y += deltaY * speed;
        moved = true;
    }

    public void moveBackward(double speed) {
        x -= deltaX * speed;
        y -= deltaY * speed;
        moved = true;
    }

    public void moveSideway(double speed) {
        x += deltaY * speed;
        y -= deltaX * speed;
        moved = true;
    }

}
