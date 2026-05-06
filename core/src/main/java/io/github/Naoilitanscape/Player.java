package io.github.Naoilitanscape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Player {

    private float x;
    private float y;
    private float width;
    private float height;
    private float speed;
    private boolean facingRight;
    private boolean moving;

    public Player(float x, float y, float width, float height, float speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.facingRight = true;
        this.moving = false;
    }

    // This checks the arrow keys and moves the player around the screen.
    public void move(float delta, FitViewport viewport) {
        moving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= speed * delta;
            facingRight = false;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += speed * delta;
            facingRight = true;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            y += speed * delta;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            y -= speed * delta;
            moving = true;
        }

        x = MathUtils.clamp(x, 0, viewport.getWorldWidth() - width);
        y = MathUtils.clamp(y, 0, viewport.getWorldHeight() - height);
    }

    // Used when the player gets hit or the game restarts.
    public void reset(float x, float y) {
        this.x = x;
        this.y = y;
        this.facingRight = true;
        this.moving = false;
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

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    @Override
    public String toString() {
        return "Player{" +
            "x=" + x +
            ", y=" + y +
            ", width=" + width +
            ", height=" + height +
            ", speed=" + speed +
            ", facingRight=" + facingRight +
            ", moving=" + moving +
            '}';
    }
}
