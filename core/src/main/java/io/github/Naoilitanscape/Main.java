package io.github.Naoilitanscape;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Main game class */
public class Main implements ApplicationListener {

    // textures
    private Texture dogImage;
    private Texture backgroundImage;
    private Texture ballImage;
    private Texture enemySheet;

    // sprites
    private Sprite dog;
    private Sprite background;
    private Sprite ball;
    private Sprite enemy;

    private SpriteBatch batch;
    private FitViewport viewport;

    // sounds
    private Sound barkSound;
    private Sound demonVoiceSound;

    // animation
    private Animation<TextureRegion> enemyAnimation;
    private float stateTime;

    // UI
    private BitmapFont font;
    private Matrix4 hudMatrix;

    // game values
    private int score;
    private float playerSpeed;
    private float enemySpeedX;
    private float enemySpeedY;
    private float ballSpeedX;
    private float ballSpeedY;
    private float collisionWait;

    // jump physics
    private float velocityY;
    private final float gravity = -12f;
    private final float jumpVelocity = 5.5f;
    private boolean isGrounded;

    @Override
    public void create() {
        // load images
        dogImage = new Texture("dog.png");
        backgroundImage = new Texture("background-clouds.jpg");
        ballImage = new Texture("ball.png");
        enemySheet = new Texture("attack - sword.png");

        // create sprites
        dog = new Sprite(dogImage);
        background = new Sprite(backgroundImage);
        ball = new Sprite(ballImage);

        // split enemy sprite sheet into frames
        TextureRegion[][] enemyFrames2D = TextureRegion.split(enemySheet, 64, 64);
        TextureRegion[] enemyFrames = enemyFrames2D[0]; // pick a row
        enemyAnimation = new Animation<>(0.12f, enemyFrames);
        stateTime = 0f;

        enemy = new Sprite(enemyFrames[0]);

        batch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        // load sounds
        barkSound = Gdx.audio.newSound(Gdx.files.internal("dragon-studio-dog-bark-sound-450454.mp3"));
        demonVoiceSound = Gdx.audio.newSound(Gdx.files.internal("phatphrogstudio-demon-voice-smell-flesh-no-ai-479322.mp3"));

        // font for score
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        hudMatrix = new Matrix4();

        // player setup
        dog.setSize(1f, 1f);
        dog.setPosition(1f, 0f);

        // background fills screen
        background.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        background.setPosition(0, 0);

        // ball setup
        ball.setSize(0.55f, 0.55f);
        ball.setPosition(5.5f, 3f);

        // enemy setup
        enemy.setSize(1f, 1f);
        enemy.setPosition(6f, 1.3f);

        // starting values
        score = 0;
        playerSpeed = 3f;
        enemySpeedX = -2f;
        enemySpeedY = 1.4f;

        randomizeBallVelocity();

        collisionWait = 0f;
        velocityY = 0f;
        isGrounded = true;

        updateWindowTitle();
    }

    @Override
    public void resize(int width, int height) {
        // ignore bad resize values
        if(width <= 0 || height <= 0) return;

        viewport.update(width, height, true);
        hudMatrix.setToOrtho2D(0, 0, width, height);
    }

    @Override
    public void render() {

        float delta = Gdx.graphics.getDeltaTime();

        // update animation
        stateTime += delta;
        enemy.setRegion(enemyAnimation.getKeyFrame(stateTime, true));

        // update game logic
        input(delta);
        applyJumpPhysics(delta);
        moveEnemy(delta);
        moveBall(delta);
        checkCollisions(delta);

        ScreenUtils.clear(Color.BLACK);

        // draw game world
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        background.draw(batch);
        ball.draw(batch);
        enemy.draw(batch);
        dog.draw(batch);
        batch.end();

        // draw UI (screen space)
        batch.setProjectionMatrix(hudMatrix);
        batch.begin();
        font.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
        batch.end();
    }

    private void input(float delta) {
        // left / right movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dog.translateX(-playerSpeed * delta);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dog.translateX(playerSpeed * delta);
        }

        // jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isGrounded) {
            velocityY = jumpVelocity;
            isGrounded = false;
        }

        // keep player in bounds
        dog.setX(MathUtils.clamp(dog.getX(), 0, viewport.getWorldWidth() - dog.getWidth()));
    }

    private void applyJumpPhysics(float delta) {
        velocityY += gravity * delta;
        dog.translateY(velocityY * delta);

        // ground check
        if (dog.getY() <= 0) {
            dog.setY(0);
            velocityY = 0;
            isGrounded = true;
        }

        // ceiling clamp
        if (dog.getY() > viewport.getWorldHeight() - dog.getHeight()) {
            dog.setY(viewport.getWorldHeight() - dog.getHeight());
            velocityY = 0;
        }
    }

    private void moveEnemy(float delta) {
        enemy.translate(enemySpeedX * delta, enemySpeedY * delta);

        // bounce off edges
        if (enemy.getX() < 0 || enemy.getX() > viewport.getWorldWidth() - enemy.getWidth()) {
            enemySpeedX = -enemySpeedX;
        }
        if (enemy.getY() < 0 || enemy.getY() > viewport.getWorldHeight() - enemy.getHeight()) {
            enemySpeedY = -enemySpeedY;
        }

        enemy.setX(MathUtils.clamp(enemy.getX(), 0, viewport.getWorldWidth() - enemy.getWidth()));
        enemy.setY(MathUtils.clamp(enemy.getY(), 0, viewport.getWorldHeight() - enemy.getHeight()));
    }

    private void moveBall(float delta) {
        ball.translate(ballSpeedX * delta, ballSpeedY * delta);

        // bounce off edges
        if (ball.getX() < 0 || ball.getX() > viewport.getWorldWidth() - ball.getWidth()) {
            ballSpeedX = -ballSpeedX;
        }
        if (ball.getY() < 0 || ball.getY() > viewport.getWorldHeight() - ball.getHeight()) {
            ballSpeedY = -ballSpeedY;
        }

        ball.setX(MathUtils.clamp(ball.getX(), 0, viewport.getWorldWidth() - ball.getWidth()));
        ball.setY(MathUtils.clamp(ball.getY(), 0, viewport.getWorldHeight() - ball.getHeight()));
    }

    private void checkCollisions(float delta) {
        Rectangle dogHitbox = dog.getBoundingRectangle();
        Rectangle ballHitbox = ball.getBoundingRectangle();
        Rectangle enemyHitbox = enemy.getBoundingRectangle();

        // cooldown timer
        if (collisionWait > 0) {
            collisionWait -= delta;
        }

        // collect ball
        if (dogHitbox.overlaps(ballHitbox)) {
            score++;
            barkSound.play(0.35f);
            moveBallToRandomPosition();
            randomizeBallVelocity();
            updateWindowTitle();
        }

        // hit enemy
        if (collisionWait <= 0 && dogHitbox.overlaps(enemyHitbox)) {
            score--;
            demonVoiceSound.play(0.45f);
            dog.setPosition(1f, 0f);
            velocityY = 0f;
            isGrounded = true;
            collisionWait = 1f;
            updateWindowTitle();
        }
    }

    private void moveBallToRandomPosition() {
        float randomX = MathUtils.random(0f, viewport.getWorldWidth() - ball.getWidth());
        float randomY = MathUtils.random(0.5f, viewport.getWorldHeight() - ball.getHeight());
        ball.setPosition(randomX, randomY);
    }

    private void randomizeBallVelocity() {
        ballSpeedX = MathUtils.random(1.5f, 3f);
        ballSpeedY = MathUtils.random(1.5f, 3f);

        if (MathUtils.randomBoolean()) ballSpeedX = -ballSpeedX;
        if (MathUtils.randomBoolean()) ballSpeedY = -ballSpeedY;
    }

    private void updateWindowTitle() {
        Gdx.graphics.setTitle("NapolitanScape - Score: " + score);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        barkSound.dispose();
        demonVoiceSound.dispose();
        dogImage.dispose();
        backgroundImage.dispose();
        ballImage.dispose();
        enemySheet.dispose();
    }
}
