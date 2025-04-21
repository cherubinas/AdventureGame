import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.Random;

public class GamePanel extends JPanel implements KeyListener, ActionListener, MouseListener {
    private final Player player;
    private Timer gameLoop;
    private List<Platform> platforms;
    private Rectangle floor;
    public List<Enemy> enemies;
    private boolean gameOver = false;
    private ImageIcon backgroundGif; // Background image
    private GameOverScreen gameOverScreen; // Separate Game Over screen
    private boolean isPaused = false;
    private PauseMenu pauseMenu;
    private JButton resumeButton;
    private Runnable resumeAction;
    private MusicPlayer backgroundMusicPlayer;
    private int worldWidth = 9000; // Width of the entire game world
    private int worldHeight = 600;
    private int cameraX = 0; // How much the view is offset horizontally
    private NPC npc;
    private boolean ePressed = false;
    private int enemyKillCount = 0;
    private boolean armorDropped = false;
    private boolean armorEquipped = false;
    private BufferedImage armorImage;
    private Rectangle armorBounds;
    private boolean showArmorPickupPrompt = false;
    private boolean armorAlreadyDropped = false;
    private HashMap<String, AnimationLoader.AnimationData> animationMap;
    private int enemiesKilledForPotion = 0;
    private boolean potionDropped = false;
    private BufferedImage healthPotionImage;
    private Rectangle healthPotionBounds;
    private boolean showPotionPickupPrompt = false;
    private int enemiesKilledForSword = 0;
    private boolean swordDropped = false;
    private BufferedImage swordImage;
    private Rectangle swordBounds;
    private boolean showSwordPickupPrompt = false;
    boolean swordEquipped = false;
    private YouWonScreen youWonScreen;
    private boolean keyDropped = false;
    private boolean keyCollected = false;
    private BufferedImage keyImage;
    private Rectangle keyBounds;
    private boolean showKeyPickupPrompt = false;
    private int enemiesKilledForKey = 0;// Track if the stronger sword is equipped

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        backgroundMusicPlayer = new MusicPlayer("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\music\\kim-lightyear-leave-the-world-tonight-chiptune-edit-loop-132102.mp3"); // Adjust path as needed
        backgroundMusicPlayer.play(); // Start background music
        animationMap = AnimationLoader.loadAllAnimations();


        setLayout(null); // Allows manual positioning of components
        // Load background image
        setDoubleBuffered(true);
        backgroundGif = new ImageIcon("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\j1IX2Y.png");
        // Initialize player
        player = new Player(100, 300, this);
        player.setWorldWidth(worldWidth);
        npc = new NPC(500, 430, "C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\Mushroom-idle.png");


        // Initialize platforms
        platforms = new ArrayList<>();
        platforms.add(new Platform(100, 400, 200, 20));
        platforms.add(new Platform(350, 300, 200, 20));
        platforms.add(new Platform(600, 200, 200, 20));

        // Initialize floor
        floor = new Rectangle(0, 500, worldWidth, 50);


        // Initialize enemies
        enemies = new ArrayList<>(); // 👈 initialize FIRST

        resizePlatforms(worldWidth, worldHeight);

        // Initialize game loop
        gameLoop = new Timer(16, this); // Approximately 60 FPS
        gameLoop.start();
        youWonScreen = new YouWonScreen(this);
        youWonScreen.setBounds(0, 0, getWidth(), getHeight());
        youWonScreen.setPreferredSize(new Dimension(getWidth(), getHeight()));
        add(youWonScreen);
        youWonScreen.setVisible(false); // Hide it initially
       // youWonScreen.moveToFront();
        this.repaint();

        // Initialize game over screen
        gameOverScreen = new GameOverScreen(this);
        gameOverScreen.setBounds(0, 0, getWidth(), getHeight());
        gameOverScreen.setPreferredSize(new Dimension(getWidth(), getHeight()));
        add(gameOverScreen);
        gameOverScreen.setVisible(false); // Hide it initially
        // Initialize you won screen

    }
    private static BufferedImage platformTexture;

    static {
        try {
            platformTexture = ImageIO.read(new File("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\resources\\map\\afb6872a0fbefb737c7636270f3b626b.jpg"));
        } catch (IOException e) {
            System.err.println("Failed to load floor/wall texture!");
            e.printStackTrace();
        }
    }

    public void startNewGame() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }

        gameOver = false;
        swordEquipped = false; // Reset sword state
         armorEquipped = false; // Reset armor state

        // 🧼 Clear and reset the player
        player.reset();

        // 🧼 Clear all platforms and enemies
        platforms.clear();
        enemies.clear();

        // ✅ Reinitialize all platforms (and their enemies)
        resizePlatforms(worldWidth, worldHeight);

        // 👇 Add at least one fallback enemy in case platforms fail
        if (enemies.isEmpty()) {
            enemies.add(new Enemy(300, 450, this));
        }

        gameOverScreen.setBounds(0, 0, getWidth(), getHeight());
        gameOverScreen.setPreferredSize(new Dimension(getWidth(), getHeight()));
        gameOverScreen.setVisible(false);

        gameLoop = new Timer(16, this);
        gameLoop.start();

        this.setFocusable(true);
        this.requestFocusInWindow();
        repaint();
    }
    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
        enemyKillCount++;
        System.out.println("Enemy killed. Total kills: " + enemyKillCount); // Debug

        // Armor drop logic (remains the same)
        if (enemyKillCount >= 2 && !armorAlreadyDropped) {
            System.out.println("Dropping armor..."); // Debug
            dropArmorItem(player.getX(), player.getY());
            armorAlreadyDropped = true;
        }

        // Health potion drop logic
        enemiesKilledForPotion++;
        if (enemiesKilledForPotion >= 5 && !potionDropped) {
            System.out.println("Dropping health potion...");
            dropHealthPotion(enemy.getX(), enemy.getY()); // Drop at the defeated enemy's location
            potionDropped = true;
            enemiesKilledForPotion = 0; // Reset the counter
        }
        enemiesKilledForSword++;
        if (enemiesKilledForSword >= 7 && !swordDropped) {
            System.out.println("Dropping stronger sword...");
            dropSword(enemy.getX(), enemy.getY());
            swordDropped = true;
            enemiesKilledForSword = 0; // Reset counter for potential future items
        }
        enemiesKilledForKey++;
        if (enemiesKilledForKey >= 13 && !keyDropped) {
            System.out.println("Dropping key...");
            dropKey(enemy.getX(), enemy.getY());
            keyDropped = true;
            enemiesKilledForKey = 0;
        }
    }
    private void dropKey(int x, int y) {
        AnimationLoader.AnimationData keyData = animationMap.get("item_key"); // Assuming "item_key" is your key key
        if (keyData != null && !keyData.frames.isEmpty()) {
            this.keyImage = keyData.frames.get(0);
            System.out.println("Key image retrieved successfully.");
        } else {
            System.err.println("ERROR: Could not retrieve cropped key image from animation map!");
            this.keyImage = null;
        }

        if (this.keyImage != null) {
            keyBounds = new Rectangle(x, y, this.keyImage.getWidth(), this.keyImage.getHeight());
            System.out.println("Key bounds set to: " + keyBounds);
            keyDropped = true;
            showKeyPickupPrompt = false;
            repaint();
        }
    }
    private void dropSword(int x, int y) {
        AnimationLoader.AnimationData swordData = animationMap.get("item_sword"); // Assuming "item_sword" is your sword key
        if (swordData != null && !swordData.frames.isEmpty()) {
            this.swordImage = swordData.frames.get(0);
            System.out.println("Stronger sword image retrieved successfully.");
        } else {
            System.err.println("ERROR: Could not retrieve cropped stronger sword image from animation map!");
            this.swordImage = null;
        }

        if (this.swordImage != null) {
            swordBounds = new Rectangle(x, y, this.swordImage.getWidth(), this.swordImage.getHeight());
            System.out.println("Stronger sword bounds set to: " + swordBounds);
            swordDropped = true;
            showSwordPickupPrompt = false;
            repaint();
        }
    }
    private void dropArmorItem(int x, int y) {
        System.out.println("Attempting to drop armor at: " + x + ", " + y);
        AnimationLoader.AnimationData armorData = animationMap.get("item_armor"); // Assuming your map is called animationMap
        if (armorData != null && !armorData.frames.isEmpty()) {
            this.armorImage = armorData.frames.get(0); // Get the first (and only) frame of the item animation
            System.out.println("Cropped armor image retrieved successfully.");
        } else {
            System.err.println("ERROR: Could not retrieve cropped armor image from animation map!");
            this.armorImage = null; // Ensure armorImage is null if retrieval fails
        }

        if (this.armorImage != null) {
            armorBounds = new Rectangle(x, y, 40, 40); // Use a fixed size for the dropped item
            System.out.println("Armor bounds set to: " + armorBounds);
            armorDropped = true;
            showArmorPickupPrompt = false;
            repaint();
        }
    }
    private void dropHealthPotion(int x, int y) {
        AnimationLoader.AnimationData potionData = animationMap.get("item_health_potion");
        if (potionData != null && !potionData.frames.isEmpty()) {
            this.healthPotionImage = potionData.frames.get(0);
            System.out.println("Health potion image retrieved successfully.");
        } else {
            System.err.println("ERROR: Could not retrieve cropped health potion image from animation map!");
            this.healthPotionImage = null;
        }

        if (this.healthPotionImage != null) {
            healthPotionBounds = new Rectangle(x, y, this.healthPotionImage.getWidth(), this.healthPotionImage.getHeight());
            System.out.println("Health potion bounds set to: " + healthPotionBounds);
            potionDropped = true;
            showPotionPickupPrompt = false; // Reset prompt flag
            repaint();
        }
    }



    public GameState createGameState() {
        List<Enemy.EnemyState> enemyStates = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemyStates.add(enemy.getState());
        }

        List<Platform.PlatformState> platformStates = new ArrayList<>();
        for (Platform platform : platforms) {
            platformStates.add(platform.getState());
        }

        return new GameState(player.getHealth(), player.getX(), player.getY(), enemyStates, platformStates,swordEquipped, armorEquipped);
    }
    public void loadGameState(GameState state) {
        player.setHealth(state.getPlayerHealth());
        player.setPosition(state.getPlayerX(), state.getPlayerY());
        swordEquipped = state.isSwordEquipped(); // ✅ Load sword state
        armorEquipped = state.isArmorEquipped(); // ✅ Load armor state
        loadEnemies(state.getEnemyPositions());
        loadPlatforms(state.getPlatformStates());
        repaint();
    }
    public void loadPlatforms(List<Platform.PlatformState> platformStates) {
        platforms.clear();
        enemies.clear(); // Optional: clear enemies too before placing new ones

        for (Platform.PlatformState state : platformStates) {
            Platform platform = new Platform(state.getX(), state.getY(), state.getWidth(), state.getHeight(), state.isMoving());
            platforms.add(platform);
        }
    }

    public void loadEnemies(List<Enemy.EnemyState> enemyStates) {
        enemies.clear();
        for (Enemy.EnemyState enemyState : enemyStates) {
            Enemy enemy = new Enemy(enemyState.getX(), enemyState.getY(), this);
            enemy.setState(enemyState.getX(), enemyState.getY(), enemyState.getHealth());
            if (enemyState.isDead()) {

                enemy.die();
            }
            enemies.add(enemy);
        }
    }

    private boolean armorRendered = false;


        // Debugging armorImage state
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw background FIRST
            if (backgroundGif != null) {
                g.drawImage(backgroundGif.getImage(), 0, 0, getWidth(), getHeight(), this);
            }

            // Handle game over screen
            if (gameOver) {
                gameOverScreen.setVisible(true);
                return;
            }

            // Draw left wall
            if (platformTexture != null) {
                int sliceWidth = 20;
                int tileH = platformTexture.getHeight();
                BufferedImage wallSlice = platformTexture.getSubimage(0, 0, sliceWidth, tileH);
                for (int y = 0; y < getHeight(); y += tileH) {
                    g.drawImage(wallSlice, 0 - cameraX, y, null);
                }
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0 - cameraX, 0, 20, getHeight());
            }

            // Render NPC
            npc.render(g, cameraX);

            // Draw floor
            if (platformTexture != null) {
                int tileW = platformTexture.getWidth();
                int tileH = 30;
                for (int x = floor.x; x < floor.x + floor.width; x += tileW) {
                    g.drawImage(platformTexture, x - cameraX, floor.y, null);
                }
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(floor.x - cameraX, floor.y, floor.width, floor.height);
            }

            // Render player and world offset
            player.render(g, cameraX);

            // Render platforms
            for (Platform platform : platforms) {
                platform.render(g, cameraX);
            }

            // Render enemies
            for (Enemy enemy : enemies) {
                enemy.render(g, cameraX);
            }

            // Render the dropped armor at a smaller size
            if (armorDropped && armorBounds != null && armorImage != null) {
                int renderX = armorBounds.x - cameraX;
                int renderY = armorBounds.y;
                int scaledWidth = 25;  // Adjust this value to your desired smaller width
                int scaledHeight = 25; // Adjust this value to your desired smaller height

                if (renderX >= 0 && renderX <= getWidth() && renderY >= 0 && renderY <= getHeight()) {
                    g.drawImage(armorImage, renderX, renderY, scaledWidth, scaledHeight, null); // Specify width and height
                } else {
                    System.err.println("Armor image is outside the visible area!");
                }
            }
            if (potionDropped && healthPotionBounds != null && healthPotionImage != null) {
                int renderX = healthPotionBounds.x - cameraX;
                int renderY = healthPotionBounds.y;
                int potionScaledWidth = 30; // Adjust size as needed
                int potionScaledHeight = 30; // Adjust size as needed
                if (renderX >= 0 && renderX <= getWidth() && renderY >= 0 && renderY <= getHeight()) {
                    g.drawImage(healthPotionImage, renderX, renderY, potionScaledWidth, potionScaledHeight, null);
                } else {
                    System.err.println("Health potion is outside the visible area!");
                }
            }
            if (keyDropped && keyBounds != null && keyImage != null) {
                int renderX = keyBounds.x - cameraX;
                int renderY = keyBounds.y;
                int keyScaledWidth = 30; // Adjust size as needed
                int keyScaledHeight = 30; // Adjust size as needed
                if (renderX >= 0 && renderX <= getWidth() && renderY >= 0 && renderY <= getHeight()) {
                    g.drawImage(keyImage, renderX, renderY, keyScaledWidth, keyScaledHeight, null);
                } else {
                    System.err.println("Key is outside the visible area!");
                }
            }

            // Render the dropped sword
            if (swordDropped && swordBounds != null && swordImage != null) {
                int renderX = swordBounds.x - cameraX;
                int renderY = swordBounds.y;
                int swordScaledWidth = 30; // Adjust size as needed
                int swordScaledHeight = 30; // Adjust size as needed
                if (renderX >= 0 && renderX <= getWidth() && renderY >= 0 && renderY <= getHeight()) {
                    g.drawImage(swordImage, renderX, renderY, swordScaledWidth, swordScaledHeight, null);
                } else {
                    System.err.println("Stronger sword is outside the visible area!");
                }
            }


            // Show armor pickup prompt in a text box like the NPC's
            if (showArmorPickupPrompt && !armorEquipped) {
                drawPickupPromptTextBox(g, player.getX() - cameraX, player.getY() - 60, "You got armor!", "Press E to equip");
            }

            // Show health potion pickup prompt
            if (showPotionPickupPrompt && healthPotionBounds != null && player.getBounds().intersects(healthPotionBounds)) {
                drawPickupPromptTextBox(g, player.getX() - cameraX, player.getY() - 90, "Health Potion!", "Press E to use");
            }

            // Show sword pickup prompt
            if (showSwordPickupPrompt && swordBounds != null && player.getBounds().intersects(swordBounds) && !swordEquipped) {
                drawPickupPromptTextBox(g, player.getX() - cameraX, player.getY() - 120, "Stronger Sword!", "Press E to equip");
            }
            if (showKeyPickupPrompt && keyBounds != null && player.getBounds().intersects(keyBounds) && !keyCollected) {
                drawPickupPromptTextBox(g, player.getX() - cameraX, player.getY() - 150, "Mysterious Key!", "Press E to collect");
            }


            // Temporary debug rectangle (you can remove this now if you want)
            // if (armorBounds != null) {
            //     g.setColor(Color.YELLOW);
            //     g.drawRect(armorBounds.x - cameraX, armorBounds.y, armorBounds.width, armorBounds.height);
            // }
        }

    private void drawPickupPromptTextBox(Graphics g, int x, int y, String line1, String line2) {
        int boxPadding = 10;
        int lineHeight = 20;
        Font pixelFont = null;
        try {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/pixel.ttf")).deriveFont(16f);
        } catch (Exception e) {
            pixelFont = new Font("Monospaced", Font.BOLD, 16);
        }
        g.setFont(pixelFont);

        FontMetrics fm = g.getFontMetrics(pixelFont);
        int width1 = fm.stringWidth(line1);
        int width2 = fm.stringWidth(line2);
        int boxWidth = Math.max(width1, width2) + 2 * boxPadding;
        int boxHeight = (lineHeight * 2) + 2 * boxPadding;

        int boxX = x - boxWidth / 2;
        int boxY = y - boxHeight;

        g.setColor(Color.WHITE);
        g.fillRect(boxX - 2, boxY - 2, boxWidth + 4, boxHeight + 4);
        g.setColor(Color.BLACK);
        g.fillRect(boxX, boxY, boxWidth, boxHeight);
        g.setColor(Color.WHITE);
        g.drawString(line1, boxX + boxPadding, boxY + boxPadding + lineHeight);
        g.drawString(line2, boxX + boxPadding, boxY + boxPadding + 2 * lineHeight);
    }
    public void openPauseMenu() {
        if (isPaused) return;

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        pauseMenu = new PauseMenu(

                frame,
                () -> SaveManager.saveGame(createGameState()),  // Save callback
                () -> System.exit(0),                          // Quit callback
                () -> closePauseMenu(frame)                         // Resume callback
        );
        pauseMenu.setMusicPlayer(backgroundMusicPlayer); // Connects MusicPlayer instance

        frame.setContentPane(pauseMenu);
        frame.revalidate();
        frame.repaint();
        isPaused = true;
    }

    public void closePauseMenu(JFrame frame) {
        frame.setContentPane(this); // restores GamePanel as the main content pane
        frame.revalidate();
        frame.repaint();

        this.setFocusable(true);
        this.requestFocusInWindow(); // <-- This is the magic line
        isPaused = false;
    }

    public void checkPlayerAttack() {
        for (Enemy enemy : enemies) {
            if (player.getBounds().intersects(enemy.getBounds())) {
                player.attack(enemies);
            }
        }
    }
    public boolean isArmorEquipped() {
        return armorEquipped;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {

            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> player.moveLeft();
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> player.moveRight();
            case KeyEvent.VK_SPACE -> player.jump();
            case KeyEvent.VK_SHIFT -> player.dash();
            case KeyEvent.VK_Y -> {
                System.out.println("Y key pressed - FORCING YouWonScreen!");
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.setContentPane(youWonScreen);
                frame.revalidate();
                frame.repaint();
                youWonScreen.setVisible(true); // Ensure it's visible
            }

            case KeyEvent.VK_K -> {
                if (!enemies.isEmpty()) {
                    Enemy enemy = enemies.get(0); // 👈 kill the first enemy in the list
                    removeEnemy(enemy);
                    System.out.println("Enemy removed for debug.");
                } else {
                    System.out.println("No enemies left to remove.");
                }
            }
            case KeyEvent.VK_E -> {
                ePressed = true;
                if (player.getBounds().intersects(npc.getBounds())) {
                    npc.handleMouseClick(); // Advance the dialogue or exit when finished
                }
            }

            case KeyEvent.VK_F -> player.attack(enemies);
            case KeyEvent.VK_H -> player.takeHit(10); // Press H to test player taking damage
            case KeyEvent.VK_ESCAPE -> {
                if (!isPaused) {
                    openPauseMenu(); // Only pauses the game
                }
                // No else! Resume only happens via button
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_D) player.stopMoving();
        if (key == KeyEvent.VK_E) ePressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}


    @Override
    public void actionPerformed(ActionEvent e) {
        if (armorDropped && armorBounds != null && player.getBounds().intersects(armorBounds)) {
            showArmorPickupPrompt = true;
            if (ePressed) {
                armorEquipped = true;
                armorDropped = false;
                showArmorPickupPrompt = false;
            }
        }
        // Key pickup logic
        if (keyDropped && keyBounds != null && player.getBounds().intersects(keyBounds) && !keyCollected) {
            showKeyPickupPrompt = true;
            if (ePressed) {
                System.out.println("E pressed while overlapping key!");
                keyCollected = true;
                keyDropped = false;
                showKeyPickupPrompt = false;
                System.out.println("Key collected!");
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.setContentPane(youWonScreen);
                frame.revalidate();
                frame.repaint();
                youWonScreen.setVisible(true); // Ensure it's visible
            }
        }
        if (potionDropped && healthPotionBounds != null && player.getBounds().intersects(healthPotionBounds)) {
            showPotionPickupPrompt = true;
            if (ePressed) {
                player.heal(50); // Call the heal method in the Player class
                potionDropped = false;
                showPotionPickupPrompt = false;
                System.out.println("Player used health potion. Current health: " + player.getHealth());
            }
        }
        if (swordDropped && swordBounds != null && player.getBounds().intersects(swordBounds) && !swordEquipped) {
            showSwordPickupPrompt = true;
            if (ePressed) {
                swordEquipped = true;
                swordDropped = false;
                showSwordPickupPrompt = false;
                System.out.println("Player equipped the stronger sword!");
            }
        }
        if (!gameOver) {
            player.update(platforms, floor);
            for (Enemy enemy : enemies) {
                enemy.update(player);
            }
            for (Platform platform : platforms) {
                platform.update(); // only moves if it's a moving one
            }
            npc.update();
            npc.checkInteraction(player.getBounds(), ePressed);

            // 👇 CAMERA follows player
            cameraX = player.getX() - getWidth() / 2;
            cameraX = Math.max(0, Math.min(cameraX, worldWidth - getWidth()));

            repaint();
        }
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        player.handleMouseInput(e, enemies);


        // Check if the player intersects with the NPC and handle the mouse click
        if (player.getBounds().intersects(npc.getBounds())) {
            npc.handleMouseClick(); // Advance the dialogue on mouse click
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        player.handleMouseInput(e, enemies);
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        floor = new Rectangle(0, height - 50, width, 50); // Resize floor
        resizePlatforms(width, height); // Recalculate platform positions
    }

    private void resizePlatforms(int width, int height) {
        platforms.clear();
        enemies.clear();

        Random rand = new Random();
        int currentX = 1100;
        int minY = 240;
        int maxY = 440;
        int minGapX = 140;
        int maxGapX = 220;

        while (currentX < worldWidth - 200) {
            int baseY = rand.nextInt(maxY - minY) + minY;
            int baseWidth = rand.nextInt(60) + 140;
            boolean baseMoving = rand.nextDouble() < 0.15;

            Platform basePlatform = new Platform(currentX, baseY, baseWidth, 20, baseMoving);
            platforms.add(basePlatform);

            // Enemy spawn chance scales with progress
            float progress = (float) currentX / worldWidth;
            float enemySpawnChance = 0.4f + progress * 0.9f; // 30% to 80%

            if (!baseMoving && rand.nextFloat() < enemySpawnChance) {
                int enemyX = currentX + baseWidth / 2 - 10;
                int enemyY = baseY - 40;
                enemies.add(new Enemy(enemyX, enemyY, this));
            }

            // Stack vertically
            int stackCount = rand.nextInt(4) + 1;

            for (int i = 1; i < stackCount; i++) {
                int stackY = baseY - i * 80;
                if (stackY < 180) break;

                int xOffset = rand.nextInt(100) - 50;
                int stackWidth = Math.max(80, baseWidth - (i * 20));

                Platform stackedPlatform = new Platform(currentX + xOffset, stackY, stackWidth, 20, false);
                platforms.add(stackedPlatform);

                if (progress > 0.4 && rand.nextFloat() < (0.1f + progress * 0.2f)) {
                    int enemyX = currentX + xOffset + stackWidth / 2 - 10;
                    int enemyY = stackY - 40;
                    enemies.add(new Enemy(enemyX, enemyY, this));
                }
            }

            int gap = rand.nextInt(maxGapX - minGapX) + minGapX;
            currentX += baseWidth + gap;
        }
    }


    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (gameOver) {

            gameOverScreen.setBounds(0, 0, getWidth(), getHeight()); // Make sure it resizes
            // 👈 handles the sound + repaint
            gameOverScreen.setVisible(true);
            gameOverScreen.showGameOverScreen();


        }
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (gameOverScreen != null) {
                    gameOverScreen.setBounds(0, 0, getWidth(), getHeight());
                    gameOverScreen.revalidate();
                    gameOverScreen.repaint();
                }
            }
        });
    }


}
