package game;

public class Player {
    public double x, y, angle, deltaX, deltaY, pitch;


    public Player(double startX, double startY, double startAngle) {
        this.x = startX;
        this.y = startY;
        this.angle = startAngle;
        this.deltaX = Math.cos(Math.toRadians(angle));
        this.deltaY = -Math.sin(Math.toRadians(angle));
    }

    public void updatePitch(double deltaPitch) {
        pitch += deltaPitch;
        pitch = Math.max(-120, Math.min(160, pitch));  // Limit the pitch angle between -90 and 90 degrees
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
    }

    public void moveBackward(double speed) {
        x -= deltaX * speed;
        y -= deltaY * speed;
    }

    public void moveSideway(double speed) {
        x += deltaY * speed;
        y -= deltaX * speed;
    }

}
