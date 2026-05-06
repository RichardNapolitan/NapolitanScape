package io.github.Naoilitanscape;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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

import java.util.ArrayList;

/** Main game class */
public class Main implements ApplicationListener {

    // images
    private Texture backgroundImage;
    private Texture playerWalkSheet;
    private Texture playerShootSheet;
    private Texture playerDieSheet;
    private Texture enemyFloatSheet;
    private Texture enemyDieSheet;
    private Texture laserImage;
    private Texture laserPowerShotImage;
    private Texture enemyOrbImage;
    private Texture cometImage;
    private Texture collectibleImage;

    // drawing
    private Sprite background;
    private Sprite collectible;
    private SpriteBatch batch;
    private FitViewport viewport;

    // sounds
    private Music backgroundMusic;
    private Sound laserSound;

    // animations
    private Animation<TextureRegion> playerWalkAnimation;
    private Animation<TextureRegion> playerShootAnimation;
    private Animation<TextureRegion> playerDieAnimation;
    private Animation<TextureRegion> enemyFloatAnimation;
    private Animation<TextureRegion> enemyDieAnimation;

    private float playerStateTime;
    private float enemyStateTime;
    private float shootTimer;

    // UI
    private BitmapFont font;
    private Matrix4 hudMatrix;

    // object lists
    private ArrayList<Enemy> enemies;
    private ArrayList<Projectile> projectiles;

    // player values
    private float playerX;
    private float playerY;
    private float playerWidth;
    private float playerHeight;
    private float playerSpeed;
    private boolean playerFacingRight;
    private boolean playerMoving;

    // enemy values
    private float enemyWidth;
    private float enemyHeight;
    private float enemySpeedBoost;
    private boolean enemiesCanShoot;

    // projectile sizes
    private float playerProjectileWidth;
    private float playerProjectileHeight;
    private float enemyOrbSize;
    private float cometWidth;
    private float cometHeight;
    private float shootCooldown;

    // power shot
    private boolean powerShotActive;
    private float powerShotTimer;

    // comet timer
    private float cometTimer;
    private final float cometScheduleSeconds = 45f;

    // game values
    private int score;
    private int lives;
    private int nextDifficultyScore;
    private float collisionWait;
    private boolean gameOver;

    @Override
    public void create() {
        // load images
        backgroundImage = new Texture("background-space.png");
        playerWalkSheet = new Texture("player-walk-clean-strip.png");
        playerShootSheet = new Texture("player-shoot-clean-strip.png");
        playerDieSheet = new Texture("player-die-clean-strip.png");
        enemyFloatSheet = new Texture("enemy-float-clean-strip.png");
        enemyDieSheet = new Texture("enemy-die-clean-strip.png");
        laserImage = new Texture("Laser-shot.png");
        laserPowerShotImage = new Texture("laser_powershot.png");
        enemyOrbImage = new Texture("enemy-orb-projectile.png");
        cometImage = new Texture("comet.png");
        collectibleImage = createCollectibleTexture();

        // keep pixel art sharp
        setNearest(backgroundImage);
        setNearest(playerWalkSheet);
        setNearest(playerShootSheet);
        setNearest(playerDieSheet);
        setNearest(enemyFloatSheet);
        setNearest(enemyDieSheet);
        setNearest(laserImage);
        setNearest(laserPowerShotImage);
        setNearest(enemyOrbImage);
        setNearest(cometImage);

        background = new Sprite(backgroundImage);
        collectible = new Sprite(collectibleImage);

        batch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        // animation strips
        playerWalkAnimation = new Animation<>(0.10f, makeStrip(playerWalkSheet, 5));
        playerShootAnimation = new Animation<>(0.08f, makeStrip(playerShootSheet, 4));
        playerDieAnimation = new Animation<>(0.15f, makeStrip(playerDieSheet, 5));
        enemyFloatAnimation = new Animation<>(0.12f, makeStrip(enemyFloatSheet, 6));
        enemyDieAnimation = new Animation<>(0.14f, makeStrip(enemyDieSheet, 6));

        playerStateTime = 0f;
        enemyStateTime = 0f;
        shootTimer = 0f;

        // load sounds
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background-music.ogg"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.25f);
        backgroundMusic.play();

        laserSound = Gdx.audio.newSound(Gdx.files.internal("laser-shot.mp3"));

        // UI
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        hudMatrix = new Matrix4();

        // background
        background.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        background.setPosition(0, 0);

        // player
        playerWidth = 0.75f;
        playerHeight = 0.95f;
        playerSpeed = 3.5f;

        // enemies
        enemyWidth = 0.8f;
        enemyHeight = 1.0f;

        // projectile sizes
        playerProjectileWidth = 0.35f;
        playerProjectileHeight = 0.15f;
        enemyOrbSize = 0.28f;
        cometWidth = 0.7f;
        cometHeight = 0.4f;

        // collectible
        collectible.setSize(0.35f, 0.35f);

        enemies = new ArrayList<>();
        projectiles = new ArrayList<>();

        resetGame();
    }

    private void setNearest(Texture texture) {
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private TextureRegion[] makeStrip(Texture texture, int frameCount) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        int frameWidth = texture.getWidth() / frameCount;
        int frameHeight = texture.getHeight();

        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(texture, i * frameWidth, 0, frameWidth, frameHeight);
        }

        return frames;
    }

    private Texture createCollectibleTexture() {
        // simple glowing pickup
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.CYAN);
        pixmap.fillCircle(16, 16, 14);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(16, 16, 6);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;

        viewport.update(width, height, true);
        hudMatrix.setToOrtho2D(0, 0, width, height);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        playerStateTime += delta;
        enemyStateTime += delta;

        if (shootTimer > 0) shootTimer -= delta;
        if (shootCooldown > 0) shootCooldown -= delta;
        if (collisionWait > 0) collisionWait -= delta;

        updatePowerShot(delta);

        if (!gameOver) {
            input(delta);
            moveEnemies(delta);
            updateEnemyDeaths(delta);
            updateEnemyOrbShooting(delta);
            updateCometSystem(delta);
            moveProjectiles(delta);
            checkCollisions();
            increaseDifficulty();
        }

        ScreenUtils.clear(Color.BLACK);

        // draw world
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        background.draw(batch);
        collectible.draw(batch);
        drawEnemies();
        drawProjectiles();
        drawPlayer();
        batch.end();

        // draw HUD
        batch.setProjectionMatrix(hudMatrix);
        batch.begin();
        font.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "Lives: " + lives, 20, Gdx.graphics.getHeight() - 45);
        font.draw(batch, "Enemies: " + activeEnemyCount(), 20, Gdx.graphics.getHeight() - 70);
        font.draw(batch, "Arrows = Move   Space = Shoot", 20, Gdx.graphics.getHeight() - 95);

        if (powerShotActive) {
            font.draw(batch, "Power Shot: " + Math.round(powerShotTimer) + "s", 20, Gdx.graphics.getHeight() - 120);
        }

        if (score >= 10 && !gameOver) {
            font.draw(batch, "Comets in: " + Math.round(cometTimer) + "s", 20, Gdx.graphics.getHeight() - 145);
        }

        if (gameOver) {
            font.draw(batch, "GAME OVER - Press R to Restart", 20, Gdx.graphics.getHeight() - 170);
        }

        batch.end();

        if (gameOver && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetGame();
        }
    }

    private void updatePowerShot(float delta) {
        if (powerShotActive) {
            powerShotTimer -= delta;

            if (powerShotTimer <= 0) {
                powerShotActive = false;
                powerShotTimer = 0f;
                enemiesCanShoot = true;
            }
        }
    }

    private void drawPlayer() {
        TextureRegion frame;

        if (gameOver) {
            frame = playerDieAnimation.getKeyFrame(playerStateTime, false);
        } else if (shootTimer > 0 && !playerMoving) {
            // standing shot pose
            frame = playerShootAnimation.getKeyFrame(0, false);
        } else {
            // walking stays animated
            frame = playerWalkAnimation.getKeyFrame(playerStateTime, true);
        }

        drawFacing(frame, playerX, playerY, playerWidth, playerHeight, playerFacingRight);
    }

    private void drawEnemies() {
        TextureRegion floatFrame = enemyFloatAnimation.getKeyFrame(enemyStateTime, true);

        for (Enemy enemy : enemies) {
            TextureRegion frame;

            if (enemy.isDying()) {
                frame = enemyDieAnimation.getKeyFrame(enemy.getDeathTimer(), false);
            } else {
                frame = floatFrame;
            }

            boolean enemyFacesRight = playerX > enemy.getX();
            drawFacing(frame, enemy.getX(), enemy.getY(), enemyWidth, enemyHeight, enemyFacesRight);
        }
    }

    private void drawProjectiles() {
        for (Projectile projectile : projectiles) {
            if (projectile.getType().equals(Projectile.PLAYER)) {
                Texture laserTexture = projectile.getDamage() > 1 ? laserPowerShotImage : laserImage;
                drawTextureProjectile(laserTexture, projectile, playerProjectileWidth, playerProjectileHeight);
            } else if (projectile.getType().equals(Projectile.ORB)) {
                batch.draw(enemyOrbImage, projectile.getX(), projectile.getY(), enemyOrbSize, enemyOrbSize);
            } else if (projectile.getType().equals(Projectile.COMET)) {
                drawTextureProjectile(cometImage, projectile, cometWidth, cometHeight);
            }
        }
    }

    private void drawTextureProjectile(Texture texture, Projectile projectile, float width, float height) {
        if (projectile.getSpeedX() < 0) {
            batch.draw(texture, projectile.getX() + width, projectile.getY(), -width, height);
        } else {
            batch.draw(texture, projectile.getX(), projectile.getY(), width, height);
        }
    }

    private void drawFacing(TextureRegion frame, float x, float y, float width, float height, boolean facingRight) {
        if (facingRight) {
            batch.draw(frame, x, y, width, height);
        } else {
            batch.draw(frame, x + width, y, -width, height);
        }
    }

    private void input(float delta) {
        playerMoving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= playerSpeed * delta;
            playerFacingRight = false;
            playerMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerX += playerSpeed * delta;
            playerFacingRight = true;
            playerMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerY += playerSpeed * delta;
            playerMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            playerY -= playerSpeed * delta;
            playerMoving = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && shootCooldown <= 0) {
            shoot();
        }

        playerX = MathUtils.clamp(playerX, 0, viewport.getWorldWidth() - playerWidth);
        playerY = MathUtils.clamp(playerY, 0, viewport.getWorldHeight() - playerHeight);
    }

    private void shoot() {
        float shotSpeed = playerFacingRight ? 7f : -7f;
        float shotX = playerFacingRight ? playerX + playerWidth : playerX - playerProjectileWidth;
        float shotY = playerY + 0.42f;
        int damage = powerShotActive ? 2 : 1;

        projectiles.add(new Projectile(shotX, shotY, shotSpeed, 0f, damage, Projectile.PLAYER));

        laserSound.play(0.5f);
        shootCooldown = 0.25f;
        shootTimer = 0.35f;
        playerStateTime = 0f;
    }

    private void moveEnemies(float delta) {
        for (Enemy enemy : enemies) {
            if (enemy.isDying()) continue;

            float directionX = playerX - enemy.getX();
            float directionY = playerY - enemy.getY();

            float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);

            if (length != 0) {
                directionX /= length;
                directionY /= length;
            }

            float baseSpeed = 0.65f;

            enemy.setSpeedX(directionX * baseSpeed * enemySpeedBoost);
            enemy.setSpeedY(directionY * baseSpeed * enemySpeedBoost);

            enemy.setX(enemy.getX() + enemy.getSpeedX() * delta);
            enemy.setY(enemy.getY() + enemy.getSpeedY() * delta);

            enemy.setX(MathUtils.clamp(enemy.getX(), 0, viewport.getWorldWidth() - enemyWidth));
            enemy.setY(MathUtils.clamp(enemy.getY(), 0, viewport.getWorldHeight() - enemyHeight));
        }
    }

    private void updateEnemyDeaths(float delta) {
        float deathLength = 0.14f * 6f;

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);

            if (enemy.isDying()) {
                enemy.setDeathTimer(enemy.getDeathTimer() + delta);

                if (enemy.getDeathTimer() >= deathLength) {
                    enemies.remove(i);
                }
            }
        }
    }

    private void updateEnemyOrbShooting(float delta) {
        if (!enemiesCanShoot) return;

        for (Enemy enemy : enemies) {
            if (enemy.isDying()) continue;

            enemy.setOrbCooldown(enemy.getOrbCooldown() - delta);

            if (enemy.getOrbCooldown() <= 0) {
                fireEnemyOrb(enemy);
                enemy.setOrbCooldown(MathUtils.random(2.5f, 4.0f));
            }
        }
    }

    private void fireEnemyOrb(Enemy enemy) {
        float startX = enemy.getX() + enemyWidth / 2f;
        float startY = enemy.getY() + enemyHeight / 2f;

        float directionX = playerX + playerWidth / 2f - startX;
        float directionY = playerY + playerHeight / 2f - startY;

        float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);

        if (length != 0) {
            directionX /= length;
            directionY /= length;
        }

        float speed = 2.4f;
        projectiles.add(new Projectile(startX, startY, directionX * speed, directionY * speed, 1, Projectile.ORB));
    }

    private void updateCometSystem(float delta) {
        if (score < 10) return;

        cometTimer -= delta;

        if (cometTimer <= 0) {
            spawnCometWave();
            cometTimer = cometScheduleSeconds;
        }
    }

    private void spawnCometWave() {
        for (int i = 0; i < 4; i++) {
            spawnOneComet();
        }
    }

    private void spawnOneComet() {
        int edge = MathUtils.random(0, 3);

        float startX;
        float startY;

        if (edge == 0) {
            startX = -cometWidth;
            startY = MathUtils.random(0f, viewport.getWorldHeight() - cometHeight);
        } else if (edge == 1) {
            startX = viewport.getWorldWidth() + cometWidth;
            startY = MathUtils.random(0f, viewport.getWorldHeight() - cometHeight);
        } else if (edge == 2) {
            startX = MathUtils.random(0f, viewport.getWorldWidth() - cometWidth);
            startY = viewport.getWorldHeight() + cometHeight;
        } else {
            startX = MathUtils.random(0f, viewport.getWorldWidth() - cometWidth);
            startY = -cometHeight;
        }

        float targetX = MathUtils.random(0f, viewport.getWorldWidth());
        float targetY = MathUtils.random(0f, viewport.getWorldHeight());

        float directionX = targetX - startX;
        float directionY = targetY - startY;
        float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);

        if (length != 0) {
            directionX /= length;
            directionY /= length;
        }

        float speed = 3.3f;
        projectiles.add(new Projectile(startX, startY, directionX * speed, directionY * speed, 999, Projectile.COMET));
    }

    private void moveProjectiles(float delta) {
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);

            projectile.setX(projectile.getX() + projectile.getSpeedX() * delta);
            projectile.setY(projectile.getY() + projectile.getSpeedY() * delta);

            if (projectile.getX() > viewport.getWorldWidth() + 1f ||
                    projectile.getX() < -1f ||
                    projectile.getY() > viewport.getWorldHeight() + 1f ||
                    projectile.getY() < -1f) {
                projectiles.remove(i);
            }
        }
    }

    private void checkCollisions() {
        Rectangle playerHitbox = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        Rectangle collectibleHitbox = collectible.getBoundingRectangle();

        if (playerHitbox.overlaps(collectibleHitbox)) {
            score++;
            powerShotActive = true;
            powerShotTimer = 10f;
            enemiesCanShoot = false;
            moveCollectibleToRandomPosition();
            updateWindowTitle();
        }

        // enemy body hits player
        for (Enemy enemy : enemies) {
            if (enemy.isDying()) continue;

            Rectangle enemyHitbox = new Rectangle(enemy.getX(), enemy.getY(), enemyWidth, enemyHeight);

            if (collisionWait <= 0 && playerHitbox.overlaps(enemyHitbox)) {
                lives -= enemy.getDamage();
                playerX = 0.5f;
                playerY = 2f;
                collisionWait = 1f;
                updateWindowTitle();

                if (lives <= 0) {
                    gameOver = true;
                    playerStateTime = 0f;
                }

                break;
            }
        }

        // projectile collisions
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            Rectangle projectileHitbox = getProjectileHitbox(projectile);

            if (projectile.getType().equals(Projectile.ORB)) {
                if (playerHitbox.overlaps(projectileHitbox)) {
                    lives -= projectile.getDamage();
                    projectiles.remove(i);
                    updateWindowTitle();

                    if (lives <= 0) {
                        gameOver = true;
                        playerStateTime = 0f;
                    }
                }
            } else if (projectile.getType().equals(Projectile.COMET)) {
                if (playerHitbox.overlaps(projectileHitbox)) {
                    lives = 0;
                    gameOver = true;
                    playerStateTime = 0f;
                    projectiles.remove(i);
                    updateWindowTitle();
                    continue;
                }

                for (Enemy enemy : enemies) {
                    if (enemy.isDying()) continue;

                    Rectangle enemyHitbox = new Rectangle(enemy.getX(), enemy.getY(), enemyWidth, enemyHeight);

                    if (projectileHitbox.overlaps(enemyHitbox)) {
                        triggerEnemyDeath(enemy);
                        projectiles.remove(i);
                        break;
                    }
                }
            } else if (projectile.getType().equals(Projectile.PLAYER)) {
                for (Enemy enemy : enemies) {
                    if (enemy.isDying()) continue;

                    Rectangle enemyHitbox = new Rectangle(enemy.getX(), enemy.getY(), enemyWidth, enemyHeight);

                    if (projectileHitbox.overlaps(enemyHitbox)) {
                        enemy.takeDamage(projectile.getDamage());
                        projectiles.remove(i);

                        if (enemy.isDefeated()) {
                            score += 2;
                            triggerEnemyDeath(enemy);
                            addRandomEnemy();
                            updateWindowTitle();
                        }

                        break;
                    }
                }
            }
        }
    }

    private Rectangle getProjectileHitbox(Projectile projectile) {
        if (projectile.getType().equals(Projectile.ORB)) {
            return new Rectangle(projectile.getX(), projectile.getY(), enemyOrbSize, enemyOrbSize);
        }

        if (projectile.getType().equals(Projectile.COMET)) {
            return new Rectangle(projectile.getX(), projectile.getY(), cometWidth, cometHeight);
        }

        return new Rectangle(projectile.getX(), projectile.getY(), playerProjectileWidth, playerProjectileHeight);
    }

    private void triggerEnemyDeath(Enemy enemy) {
        enemy.setDying(true);
        enemy.setDeathTimer(0f);
        enemy.setSpeedX(0f);
        enemy.setSpeedY(0f);
    }

    private void increaseDifficulty() {
        if (score >= nextDifficultyScore) {
            enemySpeedBoost += 0.20f;
            addRandomEnemy();
            nextDifficultyScore += 10;
        }
    }

    private void addRandomEnemy() {
        float x = MathUtils.random(5f, viewport.getWorldWidth() - enemyWidth);
        float y = MathUtils.random(0f, viewport.getWorldHeight() - enemyHeight);

        Enemy enemy = new Enemy(x, y, 1.2f, 1.2f, 3, 1);
        enemy.setOrbCooldown(MathUtils.random(2.5f, 4.0f));
        enemies.add(enemy);
    }

    private int activeEnemyCount() {
        int count = 0;

        for (Enemy enemy : enemies) {
            if (!enemy.isDying()) {
                count++;
            }
        }

        return count;
    }

    private void moveCollectibleToRandomPosition() {
        float randomX = MathUtils.random(0.5f, viewport.getWorldWidth() - collectible.getWidth());
        float randomY = MathUtils.random(0.5f, viewport.getWorldHeight() - collectible.getHeight());

        collectible.setPosition(randomX, randomY);
    }

    private void resetGame() {
        score = 0;
        lives = 5;
        enemySpeedBoost = 1f;
        nextDifficultyScore = 10;
        collisionWait = 0f;
        gameOver = false;
        powerShotActive = false;
        powerShotTimer = 0f;
        enemiesCanShoot = false;
        cometTimer = cometScheduleSeconds;

        playerX = 0.5f;
        playerY = 2f;
        playerFacingRight = true;

        projectiles.clear();
        enemies.clear();

        Enemy enemy1 = new Enemy(6f, 3f, 1.2f, 1.2f, 3, 1);
        enemy1.setOrbCooldown(3f);

        Enemy enemy2 = new Enemy(6.5f, 1f, 1.4f, 1.1f, 3, 1);
        enemy2.setOrbCooldown(4f);

        enemies.add(enemy1);
        enemies.add(enemy2);

        moveCollectibleToRandomPosition();
        updateWindowTitle();
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

        backgroundMusic.dispose();
        laserSound.dispose();

        backgroundImage.dispose();
        playerWalkSheet.dispose();
        playerShootSheet.dispose();
        playerDieSheet.dispose();
        enemyFloatSheet.dispose();
        enemyDieSheet.dispose();
        laserImage.dispose();
        laserPowerShotImage.dispose();
        enemyOrbImage.dispose();
        cometImage.dispose();
        collectibleImage.dispose();
    }
}
