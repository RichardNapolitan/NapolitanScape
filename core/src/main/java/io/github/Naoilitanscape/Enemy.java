package io.github.Naoilitanscape;

public class Enemy {

    private float x;
    private float y;
    private float speedX;
    private float speedY;
    private int health;
    private int damage;
    private boolean dying;
    private float deathTimer;
    private float orbCooldown;

    public Enemy(float x, float y, float speedX, float speedY, int health, int damage) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.health = health;
        this.damage = damage;
        this.dying = false;
        this.deathTimer = 0f;
        this.orbCooldown = 2f;
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public float getSpeedX() { return speedX; }
    public void setSpeedX(float speedX) { this.speedX = speedX; }

    public float getSpeedY() { return speedY; }
    public void setSpeedY(float speedY) { this.speedY = speedY; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }

    public boolean isDying() { return dying; }
    public void setDying(boolean dying) { this.dying = dying; }

    public float getDeathTimer() { return deathTimer; }
    public void setDeathTimer(float deathTimer) { this.deathTimer = deathTimer; }

    public float getOrbCooldown() { return orbCooldown; }
    public void setOrbCooldown(float orbCooldown) { this.orbCooldown = orbCooldown; }

    public void takeDamage(int amount) {
        health -= amount;
    }

    public boolean isDefeated() {
        return health <= 0;
    }

    @Override
    public String toString() {
        return "Enemy{" +
                "x=" + x +
                ", y=" + y +
                ", speedX=" + speedX +
                ", speedY=" + speedY +
                ", health=" + health +
                ", damage=" + damage +
                ", dying=" + dying +
                ", orbCooldown=" + orbCooldown +
                '}';
    }
}
