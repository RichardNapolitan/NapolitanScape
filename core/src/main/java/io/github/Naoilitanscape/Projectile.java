package io.github.Naoilitanscape;

public class Projectile {

    // I use these strings to tell which kind of projectile it is.
    public static final String PLAYER = "PLAYER";
    public static final String ORB = "ORB";
    public static final String COMET = "COMET";

    private float x;
    private float y;
    private float speedX;
    private float speedY;
    private int damage;
    private String type;

    public Projectile(float x, float y, float speedX, int damage) {
        this(x, y, speedX, 0f, damage, PLAYER);
    }

    public Projectile(float x, float y, float speedX, float speedY, int damage, String type) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.damage = damage;
        this.type = type;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getSpeedX() {
        return speedX;
    }

    public void setSpeedX(float speedX) {
        this.speedX = speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public void setSpeedY(float speedY) {
        this.speedY = speedY;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Projectile{" +
            "x=" + x +
            ", y=" + y +
            ", speedX=" + speedX +
            ", speedY=" + speedY +
            ", damage=" + damage +
            ", type='" + type + '\'' +
            '}';
    }
}
