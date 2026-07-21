package Skaimbauer0401;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
            dispatcher.register(Commands.literal("spawnelytra")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.literal("radius")
                            .then(Commands.argument("radius", IntegerArgumentType.integer(1, 1000))
                                    .executes(context -> {
                                        int radius = IntegerArgumentType.getInteger(context, "radius");
                                        START_RADIUS = radius;
                                        saveConfig();
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§aStart-Radius set to:" + radius),
                                                true
                                        );
                                        return 1;
                                    })
                            )
                    )
                    .then(Commands.literal("booststrength")
                            .then(Commands.argument("strength", DoubleArgumentType.doubleArg(0.01, 100.0))
                                    .executes(context -> {
                                        double strength = DoubleArgumentType.getDouble(context, "strength");
                                        BOOST_MULTIPLIER = strength;
                                        saveConfig();
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§aBoost-Strength set to: " + strength + ""),
                                                true
                                        );
                                        return 1;
                                    })
                            )
                    )
                    .then(Commands.literal("center")
                            .then(Commands.argument("position", BlockPosArgument.blockPos())
                                    .executes(context -> {
                                        CENTER_POS = BlockPosArgument.getBlockPos(context, "position");
                                        saveConfig();
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§aCenter set to: " + CENTER_POS.toShortString()),
                                                true
                                        );
                                        return 1;
                                    })


                            )

                    )
                    .then(Commands.literal("default")
                            .executes(context -> {
                                START_RADIUS = 30;
                                BOOST_MULTIPLIER = 2.5;
                                CENTER_POS = null;
                                saveConfig();
                                context.getSource().sendSuccess(
                                        () -> Component.literal("§aSettings reset!"),
                                        true
                                );
                                return 1;
                            })
                    )
                    .then(Commands.literal("info")
                            .executes(context -> {
                                String centerInfo = CENTER_POS != null ?
                                        CENTER_POS.getX() + ", " + CENTER_POS.getY() + ", " + CENTER_POS.getZ() :
                                        "World-Spawn";
                                context.getSource().sendSuccess(
                                        () -> Component.literal("§6=== Spawnelytra Settings ===\n" +
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
        if (server.getPlayerList().getPlayers().isEmpty()) {
            playerList.clear();
        } else {
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                boolean found = false;

                for (int j = 0; j < playerList.size(); j++) {
                    if (serverPlayer.getUUID().equals(playerList.get(j).getPlayer().getUUID())) {
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
            Level world = playerSaver.getPlayer().level();
            BlockPos center = CENTER_POS != null ? CENTER_POS : world.getRespawnData().pos();

            double distance = playerSaver.getPlayer().position().distanceTo(center.getCenter());
            boolean inSpawnRadius = distance <= START_RADIUS;

            if (playerSaver.getPlayer().onGround()) {
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

                if (playerSaver.getDoubleSpaceCounter() >= 4 && playerSaver.getPlayer().getLastClientInput().jump() && inSpawnRadius && playerSaver.getPlayer().gameMode() == GameType.SURVIVAL) {
                    playerSaver.setDoubleSpaceCounter(0);
                    playerSaver.getPlayer().startFallFlying();
                    playerSaver.setFlying(true);
                    playerSaver.setBoostable(true);
                    playerSaver.setGroundTicks(LANDING_INVULNERABILITY_TICKS);
                }
                playerSaver.setDoubleSpaceCounter(playerSaver.getDoubleSpaceCounter() + 1);
            }

            if (playerSaver.isFlying()) {
                if (playerSaver.isBoostable()) {
                    playerSaver.getPlayer().sendOverlayMessage(Component.literal("Press Space for Boost"));
                } else {
                    playerSaver.getPlayer().sendOverlayMessage(Component.literal("Boost already used"));
                }
                playerSaver.getPlayer().startFallFlying();
                playerSaver.setFlyingTicks(playerSaver.getFlyingTicks() + 1);
                playerSaver.getPlayer().setInvulnerable(true);

                if (playerSaver.isBoostable() && playerSaver.getPlayer().getLastClientInput().jump() && playerSaver.getFlyingTicks() >= 15) {

                    Vec3 lookDirection = playerSaver.getPlayer().getLookAngle();
                    Vec3 boostVelocity = lookDirection.scale(BOOST_MULTIPLIER);

                    playerSaver.getPlayer().setDeltaMovement(boostVelocity);
                    playerSaver.getPlayer().connection.send(new ClientboundSetEntityMotionPacket(playerSaver.getPlayer()));


                    playerSaver.setBoostable(false);
                }
            }

            if (inSpawnRadius) {
                playerSaver.getPlayer().setHealth(playerSaver.getPlayer().getMaxHealth());
                playerSaver.getPlayer().getFoodData().setFoodLevel(20);
                playerSaver.getPlayer().getFoodData().setSaturation(20);
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
                System.out.println("[Spawnelytra] Config loaded: Radius=" + START_RADIUS +
                        ", Boost=" + BOOST_MULTIPLIER + ", Center=" +
                        (CENTER_POS != null ? CENTER_POS.toShortString() : "World-Spawn"));
            }
        } catch (IOException e) {
            System.err.println("[Spawnelytra] Error loading config: " + e.getMessage());
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
                System.out.println("[Spawnelytra] Config saved");
            }
        } catch (IOException e) {
            System.err.println("[Spawnelytra] Error saving config: " + e.getMessage());
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


