package Skaimbauer0401;

import net.minecraft.server.network.ServerPlayerEntity;

public class MySaver {
    private ServerPlayerEntity player;
    private int flyingTicks;
    private boolean boostable;
    private boolean flying;
    private int doubleSpaceCounter;
    private int groundTicks;

    public MySaver(ServerPlayerEntity player, int flyingTicks, boolean boostable, boolean flying, int doubleSpaceCounter, int groundTicks) {
        this.player = player;
        this.flyingTicks = flyingTicks;
        this.boostable = boostable;
        this.flying = flying;
        this.doubleSpaceCounter = doubleSpaceCounter;
        this.groundTicks = groundTicks;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public int getFlyingTicks() {
        return flyingTicks;
    }

    public void setFlyingTicks(int flyingTicks) {
        this.flyingTicks = flyingTicks;
    }

    public boolean isBoostable() {
        return boostable;
    }

    public void setBoostable(boolean boostable) {
        this.boostable = boostable;
    }

    public int getDoubleSpaceCounter() {
        return doubleSpaceCounter;
    }

    public void setDoubleSpaceCounter(int doubleSpaceCounter) {
        this.doubleSpaceCounter = doubleSpaceCounter;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public int getGroundTicks() {
        return groundTicks;
    }

    public void setGroundTicks(int groundTicks) {
        this.groundTicks = groundTicks;
    }
}
