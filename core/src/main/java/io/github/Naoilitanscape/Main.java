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

    // textures
    private Texture backgroundImage;
    private Texture playerWalkSheet;
    private Texture playerShootSheet;
    private Texture playerDieSheet;
    private Texture enemyFloatSheet;
    private Texture toughEnemySheet;
    private Texture bossSheet;
    private Texture laserImage;
    private Texture laserPowerShotImage;
    private Texture enemyOrbImage;
    private Texture cometImage;
    private Texture collectibleImage;

    // sprites
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
    private Animation<TextureRegion> toughEnemyAnimation;
    private Animation<TextureRegion> bossAnimation;

    private float playerStateTime;
    private float enemyStateTime;
    private float shootTimer;

    // UI
    private BitmapFont font;
    private Matrix4 hudMatrix;

    // game objects
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Projectile> projectiles;

    // enemy values
    private float enemySpeedBoost;
    private boolean enemiesCanShoot;
    private int enemiesKilled;
    private int pendingToughSpawns;
    private boolean bossSpawned;
    private boolean bossAlive;
    private boolean bossPhaseTwo;
    private float enemySpawnTimer;
    private final float enemySpawnSeconds = 8f;
    private final int maxEnemies = 10;

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

    // comet system
    private float cometTimer;
    private final float cometScheduleSeconds = 45f;

    // game values
    private int score;
    private int lives;
    private int nextDifficultyScore;
    private float collisionWait;
    private boolean gameOver;
    private boolean gameWon;

    @Override
    public void create() {
        // load images
        backgroundImage = new Texture("background-space.png");
        playerWalkSheet = new Texture("player-walk-clean-strip.png");
        playerShootSheet = new Texture("player-shoot-clean-strip.png");
        playerDieSheet = new Texture("player-die-clean-strip.png");
        enemyFloatSheet = new Texture("enemy-float-clean-strip.png");
        toughEnemySheet = new Texture("Enemy_Tougher.png");
        bossSheet = new Texture("Boss.png");
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
        setNearest(toughEnemySheet);
        setNearest(bossSheet);
        setNearest(laserImage);
        setNearest(laserPowerShotImage);
        setNearest(enemyOrbImage);
        setNearest(cometImage);

        background = new Sprite(backgroundImage);
        collectible = new Sprite(collectibleImage);

        batch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        // animation strips
        playerWalkAnimation = new Animation<>(0.10f, makeFrames(playerWalkSheet, 16, 5));
        playerShootAnimation = new Animation<>(0.08f, makeFrames(playerShootSheet, 4, 4));
        playerDieAnimation = new Animation<>(0.15f, makeFrames(playerDieSheet, 5, 5));
        enemyFloatAnimation = new Animation<>(0.12f, makeFrames(enemyFloatSheet, 16, 6));
        toughEnemyAnimation = new Animation<>(0.12f, makeFrames(toughEnemySheet, 16, 6));
        bossAnimation = new Animation<>(0.16f, makeFrames(bossSheet, 16, 3));

        playerStateTime = 0f;
        enemyStateTime = 0f;
        shootTimer = 0f;

        // sounds
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
        player = new Player(0.5f, 2f, 0.75f, 0.95f, 3.5f);

        // projectile sizes
        playerProjectileWidth = 0.35f;
        playerProjectileHeight = 0.15f;
        enemyOrbSize = 0.45f;
        cometWidth = 0.7f;
        cometHeight = 0.4f;

        // collectible
        collectible.setSize(0.35f, 0.35f);

        // lists
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

    private TextureRegion[] makeFrames(Texture texture, int preferredCount, int fallbackCount) {
        int side = (int) Math.sqrt(preferredCount);

        // Prefer 4x4/3x3 style atlas extraction for modern sprite sheets.
        if (side * side == preferredCount && texture.getWidth() % side == 0 && texture.getHeight() % side == 0) {
            TextureRegion[][] grid = TextureRegion.split(texture, texture.getWidth() / side, texture.getHeight() / side);
            TextureRegion[] frames = new TextureRegion[preferredCount];
            int frame = 0;
            for (int row = 0; row < side; row++) {
                for (int col = 0; col < side; col++) {
                    frames[frame++] = grid[row][col];
                }
            }
            return frames;
        }

        // Fallback for legacy single-row strips.
        return makeStrip(texture, fallbackCount);
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

        if (!gameOver && !gameWon) {
            input(delta);
            moveEnemies(delta);
            separateEnemies();
            updateEnemyDeaths(delta);
            updateEnemyOrbShooting(delta);
            updateCometSystem(delta);
            moveProjectiles(delta);
            checkCollisions();
            increaseDifficulty();
            processPendingSpawns();
            spawnEnemiesOverTime(delta);
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
        font.draw(batch, "Kills: " + enemiesKilled, 20, Gdx.graphics.getHeight() - 95);
        font.draw(batch, "Arrows = Move   Space = Shoot", 20, Gdx.graphics.getHeight() - 120);

        if (powerShotActive) {
            font.draw(batch, "Power Shot: " + Math.round(powerShotTimer) + "s", 20, Gdx.graphics.getHeight() - 145);
        }

        if (!gameOver) {
            font.draw(batch, "Comets in: " + Math.round(cometTimer) + "s", 20, Gdx.graphics.getHeight() - 170);
        }

        if (bossAlive) {
            font.draw(batch, bossPhaseTwo ? "BOSS PHASE 2" : "BOSS FIGHT", 20, Gdx.graphics.getHeight() - 195);
        }

        if (gameOver) {
            font.draw(batch, "GAME OVER - Press R to Restart", 20, Gdx.graphics.getHeight() - 220);
        }

        if (gameWon) {
            font.draw(batch, "VICTORY - GAME COMPLETE! Press R to Restart", 20, Gdx.graphics.getHeight() - 220);
        }

        batch.end();

        if ((gameOver || gameWon) && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
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

    private void input(float delta) {
        player.move(delta, viewport);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && shootCooldown <= 0) {
            shoot();
        }
    }

    private void shoot() {
        float shotSpeed = player.isFacingRight() ? 7f : -7f;
        float shotX = player.isFacingRight() ? player.getX() + player.getWidth() : player.getX() - playerProjectileWidth;
        float shotY = player.getY() + 0.42f;
        int damage = powerShotActive ? 2 : 1;

        projectiles.add(new Projectile(shotX, shotY, shotSpeed, 0f, damage, Projectile.PLAYER));

        laserSound.play(0.5f);
        shootCooldown = 0.25f;
        shootTimer = 0.35f;
        playerStateTime = 0f;
    }

    private void drawPlayer() {
        TextureRegion frame;

        if (gameOver) {
            frame = playerDieAnimation.getKeyFrame(playerStateTime, false);
        } else if (shootTimer > 0 && !player.isMoving()) {
            frame = playerShootAnimation.getKeyFrame(0, false);
        } else {
            frame = playerWalkAnimation.getKeyFrame(playerStateTime, true);
        }

        drawFacing(frame, player.getX(), player.getY(), player.getWidth(), player.getHeight(), player.isFacingRight());
    }

    private void drawEnemies() {
        TextureRegion normalFrame = enemyFloatAnimation.getKeyFrame(enemyStateTime, true);
        TextureRegion toughFrame = toughEnemyAnimation.getKeyFrame(enemyStateTime, true);
        TextureRegion bossFrame = bossAnimation.getKeyFrame(enemyStateTime, true);

        for (Enemy enemy : enemies) {
            if (enemy.isDying()) continue;

            TextureRegion frame;
            if (enemy.isBoss()) {
                frame = bossFrame;
            } else if (enemy.isTough()) {
                frame = toughFrame;
            } else {
                frame = normalFrame;
            }

            boolean enemyFacesRight = player.getX() > enemy.getX();
            drawFacing(frame, enemy.getX(), enemy.getY(), getEnemyWidth(enemy), getEnemyHeight(enemy), enemyFacesRight);
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

    private void moveEnemies(float delta) {
        for (Enemy enemy : enemies) {
            if (enemy.isDying()) continue;

            float directionX = player.getX() - enemy.getX();
            float directionY = player.getY() - enemy.getY();
            float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);

            if (length != 0) {
                directionX /= length;
                directionY /= length;
            }

            float baseSpeed = 0.62f;

            if (enemy.isTough()) {
                baseSpeed = 0.48f;
            }

            if (enemy.isBoss()) {
                baseSpeed = bossPhaseTwo ? 0.50f : 0.35f;
            }

            enemy.setSpeedX(directionX * baseSpeed * enemySpeedBoost);
            enemy.setSpeedY(directionY * baseSpeed * enemySpeedBoost);

            enemy.setX(enemy.getX() + enemy.getSpeedX() * delta);
            enemy.setY(enemy.getY() + enemy.getSpeedY() * delta);

            enemy.setX(MathUtils.clamp(enemy.getX(), 0, viewport.getWorldWidth() - getEnemyWidth(enemy)));
            enemy.setY(MathUtils.clamp(enemy.getY(), 0, viewport.getWorldHeight() - getEnemyHeight(enemy)));
        }
    }

    private void separateEnemies() {
        float minDistance = 0.75f;

        for (int i = 0; i < enemies.size(); i++) {
            Enemy a = enemies.get(i);
            if (a.isDying()) continue;

            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy b = enemies.get(j);
                if (b.isDying()) continue;

                float dx = a.getX() - b.getX();
                float dy = a.getY() - b.getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance > 0 && distance < minDistance) {
                    float pushX = dx / distance * 0.025f;
                    float pushY = dy / distance * 0.025f;

                    a.setX(a.getX() + pushX);
                    a.setY(a.getY() + pushY);
                    b.setX(b.getX() - pushX);
                    b.setY(b.getY() - pushY);
                }
            }
        }
    }

    private void updateEnemyDeaths(float delta) {
        float deathLength = 0.35f;

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
        if (!enemiesCanShoot || powerShotActive) return;

        for (Enemy enemy : enemies) {
            if (enemy.isDying()) continue;

            updateBossPhase(enemy);

            enemy.setOrbCooldown(enemy.getOrbCooldown() - delta);

            if (enemy.getOrbCooldown() <= 0) {
                fireEnemyOrb(enemy);

                if (enemy.isBoss()) {
                    enemy.setOrbCooldown(bossPhaseTwo ? MathUtils.random(0.15f, 0.35f) : MathUtils.random(0.35f, 0.65f));
                } else if (enemy.isTough()) {
                    enemy.setOrbCooldown(MathUtils.random(2.0f, 3.0f));
                } else {
                    enemy.setOrbCooldown(MathUtils.random(3.0f, 4.5f));
                }
            }
        }
    }

    private void updateBossPhase(Enemy enemy) {
        if (enemy.isBoss() && enemy.getHealth() <= 60 && !bossPhaseTwo) {
            bossPhaseTwo = true;
            enemy.setOrbCooldown(0.15f);
        }
    }

    private void fireEnemyOrb(Enemy enemy) {
        float startX = enemy.getX() + getEnemyWidth(enemy) / 2f;
        float startY = enemy.getY() + getEnemyHeight(enemy) / 2f;

        float baseDirX = player.getX() + player.getWidth() / 2f - startX;
        float baseDirY = player.getY() + player.getHeight() / 2f - startY;
        float length = (float) Math.sqrt(baseDirX * baseDirX + baseDirY * baseDirY);

        if (length != 0) {
            baseDirX /= length;
            baseDirY /= length;
        }

        float speed = enemy.isBoss() ? 3.0f : 2.3f;
        int damage = enemy.isBoss() ? 999 : 1;

        if (!enemy.isBoss()) {
            projectiles.add(new Projectile(startX, startY, baseDirX * speed, baseDirY * speed, damage, Projectile.ORB));
            return;
        }

        // boss shoots a spread pattern
        float spread = bossPhaseTwo ? 0.45f : 0.25f;

        projectiles.add(new Projectile(startX, startY, baseDirX * speed, baseDirY * speed, damage, Projectile.ORB));
        projectiles.add(new Projectile(startX, startY, (baseDirX + spread) * speed, (baseDirY + spread) * speed, damage, Projectile.ORB));
        projectiles.add(new Projectile(startX, startY, (baseDirX - spread) * speed, (baseDirY - spread) * speed, damage, Projectile.ORB));

        if (bossPhaseTwo) {
            projectiles.add(new Projectile(startX, startY, (baseDirX + spread) * speed, (baseDirY - spread) * speed, damage, Projectile.ORB));
            projectiles.add(new Projectile(startX, startY, (baseDirX - spread) * speed, (baseDirY + spread) * speed, damage, Projectile.ORB));
        }
    }

    private void updateCometSystem(float delta) {
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

        float speed = 2.31f;
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
        Rectangle playerHitbox = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        Rectangle collectibleHitbox = collectible.getBoundingRectangle();

        if (playerHitbox.overlaps(collectibleHitbox)) {
            score++;
            powerShotActive = true;
            powerShotTimer = 10f;
            enemiesCanShoot = false;
            moveCollectibleToRandomPosition();
            updateWindowTitle();
        }

        checkEnemyBodyHits(playerHitbox);
        checkProjectileHits(playerHitbox);
    }

    private void checkEnemyBodyHits(Rectangle playerHitbox) {
        for (Enemy enemy : enemies) {
            if (enemy.isDying()) continue;

            Rectangle enemyHitbox = new Rectangle(enemy.getX(), enemy.getY(), getEnemyWidth(enemy), getEnemyHeight(enemy));

            if (collisionWait <= 0 && playerHitbox.overlaps(enemyHitbox)) {
                lives -= enemy.getDamage();
                player.reset(0.5f, 2f);
                collisionWait = 1f;
                updateWindowTitle();

                if (lives <= 0) {
                    gameOver = true;
                    playerStateTime = 0f;
                }

                break;
            }
        }
    }

    private void checkProjectileHits(Rectangle playerHitbox) {
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            Rectangle projectileHitbox = getProjectileHitbox(projectile);

            if (projectile.getType().equals(Projectile.ORB)) {
                if (playerHitbox.overlaps(projectileHitbox)) {
                    lives = 0;
                    gameOver = true;
                    playerStateTime = 0f;
                    projectiles.remove(i);
                    updateWindowTitle();
                    return;
                }
            } else if (projectile.getType().equals(Projectile.COMET)) {
                handleCometHit(i, projectileHitbox);
            } else if (projectile.getType().equals(Projectile.PLAYER)) {
                handlePlayerShotHit(i, projectileHitbox, projectile);
            }
        }
    }

    private void handleCometHit(int projectileIndex, Rectangle projectileHitbox) {
        Rectangle playerHitbox = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        if (playerHitbox.overlaps(projectileHitbox)) {
            lives = 0;
            gameOver = true;
            playerStateTime = 0f;
            projectiles.remove(projectileIndex);
            updateWindowTitle();
        }
    }

    private void handlePlayerShotHit(int projectileIndex, Rectangle projectileHitbox, Projectile projectile) {
        for (Enemy enemy : enemies) {
            if (enemy.isDying()) continue;

            Rectangle enemyHitbox = new Rectangle(enemy.getX(), enemy.getY(), getEnemyWidth(enemy), getEnemyHeight(enemy));

            if (projectileHitbox.overlaps(enemyHitbox)) {
                enemy.takeDamage(projectile.getDamage());
                updateBossPhase(enemy);
                projectiles.remove(projectileIndex);

                if (enemy.isDefeated()) {
                    killEnemy(enemy, enemy.isBoss() ? 15 : 2);
                }

                break;
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

    private void killEnemy(Enemy enemy, int scoreAward) {
        if (enemy.isDying()) return;

        enemy.setDying(true);
        enemy.setDeathTimer(0f);
        enemy.setSpeedX(0f);
        enemy.setSpeedY(0f);

        score += scoreAward;

        if (enemy.isBoss()) {
            bossAlive = false;
            gameWon = true;
        } else {
            enemiesKilled++;

            if (enemiesKilled % 5 == 0 && !bossAlive) {
                pendingToughSpawns++;
            }
        }

        updateWindowTitle();
    }

    private void processPendingSpawns() {
        if (bossAlive || gameWon) return;

        while (pendingToughSpawns > 0) {
            addToughEnemy();
            pendingToughSpawns--;
        }
    }

    private void increaseDifficulty() {
        if (score >= 50 && !bossSpawned) {
            addBoss();
            bossSpawned = true;
            bossAlive = true;
            return;
        }

        if (bossAlive || gameWon) return;

        while (score >= nextDifficultyScore) {
            enemySpeedBoost += 0.10f;
            addRandomEnemy();
            nextDifficultyScore += 10;
        }
    }

    private void spawnEnemiesOverTime(float delta) {
        if (bossAlive || gameWon) return;

        enemySpawnTimer -= delta;

        if (enemySpawnTimer <= 0 && activeEnemyCount() < maxEnemies) {
            addRandomEnemy();

            if (score >= 20 && activeEnemyCount() < maxEnemies) {
                addRandomEnemy();
            }

            enemySpawnTimer = enemySpawnSeconds;
        }
    }

    private void addRandomEnemy() {
        float x = MathUtils.random(5f, viewport.getWorldWidth() - 0.8f);
        float y = MathUtils.random(0f, viewport.getWorldHeight() - 1.0f);

        Enemy enemy = new Enemy(x, y, 1.2f, 1.2f, 3, 1, Enemy.NORMAL);
        enemy.setOrbCooldown(MathUtils.random(2.5f, 4.0f));
        enemies.add(enemy);
    }

    private void addToughEnemy() {
        float x = MathUtils.random(5f, viewport.getWorldWidth() - 0.95f);
        float y = MathUtils.random(0f, viewport.getWorldHeight() - 1.15f);

        Enemy enemy = new Enemy(x, y, 1.0f, 1.0f, 16, 2, Enemy.TOUGH);
        enemy.setOrbCooldown(MathUtils.random(2.0f, 3.0f));
        enemies.add(enemy);
    }

    private void addBoss() {
        enemies.removeIf(enemy -> !enemy.isBoss());
        Enemy boss = new Enemy(5.8f, 2f, 0.8f, 0.8f, 120, 3, Enemy.BOSS);
        boss.setOrbCooldown(0.35f);
        enemies.add(boss);
    }

    private float getEnemyWidth(Enemy enemy) {
        if (enemy.isBoss()) return 1.45f;
        if (enemy.isTough()) return 0.95f;
        return 0.8f;
    }

    private float getEnemyHeight(Enemy enemy) {
        if (enemy.isBoss()) return 1.45f;
        if (enemy.isTough()) return 1.15f;
        return 1.0f;
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
        enemiesKilled = 0;
        pendingToughSpawns = 0;
        bossSpawned = false;
        bossAlive = false;
        bossPhaseTwo = false;
        enemySpeedBoost = 1f;
        nextDifficultyScore = 10;
        enemySpawnTimer = enemySpawnSeconds;
        collisionWait = 0f;
        gameOver = false;
        gameWon = false;
        powerShotActive = false;
        powerShotTimer = 0f;
        enemiesCanShoot = true;
        cometTimer = cometScheduleSeconds;

        player.reset(0.5f, 2f);

        projectiles.clear();
        enemies.clear();

        Enemy enemy1 = new Enemy(6f, 3f, 1.2f, 1.2f, 3, 1, Enemy.NORMAL);
        enemy1.setOrbCooldown(3f);

        Enemy enemy2 = new Enemy(6.5f, 1f, 1.4f, 1.1f, 3, 1, Enemy.NORMAL);
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

        toughEnemySheet.dispose();
        bossSheet.dispose();
        laserImage.dispose();
        laserPowerShotImage.dispose();
        enemyOrbImage.dispose();
        cometImage.dispose();
        collectibleImage.dispose();
    }
}
