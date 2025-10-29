package Skaimbauer0401;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Spawnelytra implements ModInitializer {

    private static int START_RADIUS = 30;
    private static final int LANDING_INVULNERABILITY_TICKS = 20;
    private static double BOOST_MULTIPLIER = 2.5;
    private static BlockPos CENTER_POS;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "spawnelytra.json"
    );

    private final List<MySaver> playerList = new ArrayList<>();


    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        loadConfig();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("spawnelytra")
                    .requires(source -> source.hasPermissionLevel(2)) // Nur für Ops
                    .then(CommandManager.literal("radius")
                            .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, 1000))
                                    .executes(context -> {
                                        int radius = IntegerArgumentType.getInteger(context, "radius");
                                        START_RADIUS = radius;
                                        saveConfig();
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("§aStart-Radius set to:" + radius),
                                                true
                                        );
                                        return 1;
                                    })
                            )
                    )
                    .then(CommandManager.literal("booststrength")
                            .then(CommandManager.argument("strength", DoubleArgumentType.doubleArg(0.01, 100.0))
                                    .executes(context -> {
                                        double strength = DoubleArgumentType.getDouble(context, "strength");
                                        BOOST_MULTIPLIER = strength;
                                        saveConfig();
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("§aBoost-Strength set to: " + strength + ""),
                                                true
                                        );
                                        return 1;
                                    })
                            )
                    )
                    .then(CommandManager.literal("center")
                            .then(CommandManager.argument("position", BlockPosArgumentType.blockPos())
                                    .executes(context -> {
                                        CENTER_POS = BlockPosArgumentType.getBlockPos(context, "position");
                                        saveConfig();
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("§aCenter set to: " + CENTER_POS.toShortString()),
                                                true
                                        );
                                        return 1;
                                    })


                            )

                    )
                    .then(CommandManager.literal("default")
                            .executes(context -> {
                                START_RADIUS = 30;
                                BOOST_MULTIPLIER = 2.5;
                                CENTER_POS = null;
                                saveConfig();
                                context.getSource().sendFeedback(
                                        () -> Text.literal("§aAlle Einstellungen auf Standardwerte zurückgesetzt!"),
                                        true
                                );
                                return 1;
                            })
                    )
                    .then(CommandManager.literal("info")
                            .executes(context -> {
                                String centerInfo = CENTER_POS != null ?
                                        CENTER_POS.getX() + ", " + CENTER_POS.getY() + ", " + CENTER_POS.getZ() :
                                        "World-Spawn";
                                context.getSource().sendFeedback(
                                        () -> Text.literal("§6=== Spawnelytra Settings ===\n" +
                                                "§eStart-Radius: §f" + START_RADIUS + "\n" +
                                                "§eBoost-Strength: §f" + BOOST_MULTIPLIER + "\n" +
                                                "§eCenter: §f" + centerInfo + "\n"),
                                        false
                                );
                                return 1;
                            })
                    )
            );
        });
    }

    private void onServerTick(MinecraftServer server) {
        if (server.getPlayerManager().getPlayerList().isEmpty()) {
            playerList.clear();
        } else {
            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                boolean found = false;

                for (int j = 0; j < playerList.size(); j++) {
                    if (serverPlayer.getUuid().equals(playerList.get(j).getPlayer().getUuid())) {
                        playerList.get(j).setPlayer(serverPlayer);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    playerList.add(new MySaver(serverPlayer, 0, false, false, 0, 0));
                }
            }
        }

        for (MySaver playerSaver : playerList) {
            World world = playerSaver.getPlayer().getEntityWorld();
            BlockPos center = CENTER_POS != null ? CENTER_POS : world.getSpawnPoint().getPos();

            double distance = playerSaver.getPlayer().getEntityPos().distanceTo(center.toCenterPos());
            boolean inSpawnRadius = distance <= START_RADIUS;

            if (playerSaver.getPlayer().isOnGround()) {
                playerSaver.getPlayer().sendMessage(Text.literal("onGround"), true);

                if (playerSaver.isFlying()) {
                    playerSaver.setFlying(false);
                    playerSaver.setGroundTicks(0);
                    playerSaver.setFlyingTicks(0);
                }

                playerSaver.setDoubleSpaceCounter(0);
                playerSaver.setBoostable(false);

                if (playerSaver.getGroundTicks() < LANDING_INVULNERABILITY_TICKS) {
                    playerSaver.getPlayer().setInvulnerable(true);
                    playerSaver.setGroundTicks(playerSaver.getGroundTicks() + 1);
                } else {
                    playerSaver.getPlayer().setInvulnerable(false);
                }

            } else {

                if (playerSaver.getDoubleSpaceCounter() >= 4 && playerSaver.getPlayer().getPlayerInput().jump() && inSpawnRadius && playerSaver.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    playerSaver.getPlayer().sendMessage(Text.literal("Press Space to boost"), true);
                    playerSaver.setDoubleSpaceCounter(0);
                    playerSaver.getPlayer().startGliding();
                    playerSaver.setFlying(true);
                    playerSaver.setBoostable(true);
                    playerSaver.setGroundTicks(LANDING_INVULNERABILITY_TICKS);
                }
                playerSaver.setDoubleSpaceCounter(playerSaver.getDoubleSpaceCounter() + 1);
            }

            if (playerSaver.isFlying()) {
                if (playerSaver.isBoostable()) {
                    playerSaver.getPlayer().sendMessage(Text.literal("Press Space for Boost"), true);
                } else {
                    playerSaver.getPlayer().sendMessage(Text.literal("Boost already used"), true);
                }
                playerSaver.getPlayer().startGliding();
                playerSaver.setFlyingTicks(playerSaver.getFlyingTicks() + 1);
                playerSaver.getPlayer().setInvulnerable(true);

                if (playerSaver.isBoostable() && playerSaver.getPlayer().getPlayerInput().jump() && playerSaver.getFlyingTicks() >= 15) {

                    Vec3d lookDirection = playerSaver.getPlayer().getRotationVector();
                    Vec3d boostVelocity = lookDirection.multiply(BOOST_MULTIPLIER);

                    playerSaver.getPlayer().setVelocity(boostVelocity);
                    playerSaver.getPlayer().velocityModified = true;

                    playerSaver.setBoostable(false);
                }
            }

            if (inSpawnRadius) {
                playerSaver.getPlayer().setHealth(playerSaver.getPlayer().getMaxHealth());
                playerSaver.getPlayer().getHungerManager().setFoodLevel(20);
                playerSaver.getPlayer().getHungerManager().setSaturationLevel(20);
                playerSaver.getPlayer().setInvulnerable(true);
            }

        }
    }

    private static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            saveConfig();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Config config = GSON.fromJson(reader, Config.class);
            if (config != null) {
                START_RADIUS = config.startRadius;
                BOOST_MULTIPLIER = config.boostMultiplier;
                if (config.centerX != null && config.centerY != null && config.centerZ != null) {
                    CENTER_POS = new BlockPos(config.centerX, config.centerY, config.centerZ);
                } else {
                    CENTER_POS = null;
                }
                System.out.println("[Spawnelytra] Config geladen: Radius=" + START_RADIUS +
                        ", Boost=" + BOOST_MULTIPLIER + ", Center=" +
                        (CENTER_POS != null ? CENTER_POS.toShortString() : "World-Spawn"));
            }
        } catch (IOException e) {
            System.err.println("[Spawnelytra] Fehler beim Laden der Config: " + e.getMessage());
        }
    }

    private static void saveConfig() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                Config config = new Config();
                config.startRadius = START_RADIUS;
                config.boostMultiplier = BOOST_MULTIPLIER;
                if (CENTER_POS != null) {
                    config.centerX = CENTER_POS.getX();
                    config.centerY = CENTER_POS.getY();
                    config.centerZ = CENTER_POS.getZ();
                }
                GSON.toJson(config, writer);
                System.out.println("[Spawnelytra] Config gespeichert");
            }
        } catch (IOException e) {
            System.err.println("[Spawnelytra] Fehler beim Speichern der Config: " + e.getMessage());
        }
    }

    private static class Config {
        public int startRadius = 30;
        public double boostMultiplier = 2.5;
        public Integer centerX = null;
        public Integer centerY = null;
        public Integer centerZ = null;
    }
}


