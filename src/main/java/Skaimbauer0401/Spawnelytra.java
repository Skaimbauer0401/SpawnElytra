package Skaimbauer0401;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;


public class Spawnelytra implements ModInitializer {

    private static final int SPAWN_RADIUS = 30;
    private static final int LANDING_INVULNERABILITY_TICKS = 20;
    private final int multiplyValue = 30;

    private final List<MySaver> playerList = new ArrayList<>();


    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
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
                    playerList.add(new MySaver(serverPlayer, 0, false, false, 0,0));
                }
            }
        }

        for (MySaver playerSaver : playerList) {
            World world = playerSaver.getPlayer().getEntityWorld();
            BlockPos spawn = world.getSpawnPoint().getPos();

            double distance = playerSaver.getPlayer().getEntityPos().distanceTo(spawn.toCenterPos());
            boolean inSpawnRadius = distance <= SPAWN_RADIUS;

            if (playerSaver.getPlayer().isOnGround()) {
                playerSaver.getPlayer().sendMessage(Text.literal("onGround"), true);

                playerSaver.setDoubleSpaceCounter(0);
                playerSaver.setFlying(false);
                playerSaver.setBoostable(false);
            } else {

                if (playerSaver.getDoubleSpaceCounter() >= 4 && playerSaver.getPlayer().getPlayerInput().jump() && inSpawnRadius && playerSaver.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    playerSaver.getPlayer().sendMessage(Text.literal("Press Space to boost"), true);
                    playerSaver.setDoubleSpaceCounter(0);
                    playerSaver.getPlayer().startGliding();
                    playerSaver.setFlying(true);
                    playerSaver.setBoostable(true);
                }
                playerSaver.setDoubleSpaceCounter(playerSaver.getDoubleSpaceCounter() + 1);
            }

            if (playerSaver.isFlying()) {
                if(playerSaver.isBoostable()){
                    playerSaver.getPlayer().sendMessage(Text.literal("flying boost with space"), true);
                } else {
                    playerSaver.getPlayer().sendMessage(Text.literal("flying"), true);
                }
                playerSaver.getPlayer().startGliding();
                playerSaver.setFlyingTicks(playerSaver.getFlyingTicks() + 1);
                playerSaver.getPlayer().setInvulnerable(true);

                if (playerSaver.isBoostable() && playerSaver.getPlayer().getPlayerInput().jump() && playerSaver.getFlyingTicks() >= 15) {
                    playerSaver.getPlayer().sendMessage(Text.literal("boosted"), true);
                    playerSaver.getPlayer().setVelocity(playerSaver.getPlayer().getVelocity().multiply(multiplyValue, 1, 1));
                    playerSaver.setBoostable(false);
                }
            }

            if (inSpawnRadius) {
                playerSaver.getPlayer().setHealth(playerSaver.getPlayer().getMaxHealth());
                playerSaver.getPlayer().getHungerManager().setFoodLevel(20);
                playerSaver.getPlayer().getHungerManager().setSaturationLevel(20);
            }
            if(inSpawnRadius && !playerSaver.isFlying()){
                playerSaver.getPlayer().setInvulnerable(true);
            } else if(!inSpawnRadius && !playerSaver.isFlying()){
                playerSaver.getPlayer().setInvulnerable(false);
            }
        }
    }
}


