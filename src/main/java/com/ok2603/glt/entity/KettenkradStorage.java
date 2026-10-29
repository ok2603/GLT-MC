package com.ok2603.glt.entity;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;

/** The Kettenkrad's cargo: 27 slots, like a chest. Open it with sneak + right-click. */
public class KettenkradStorage extends SimpleContainer {
    public static final int SIZE = 27;

    private final KettenkradEntity kettenkrad;

    public KettenkradStorage(KettenkradEntity kettenkrad) {
        super(SIZE);
        this.kettenkrad = kettenkrad;
    }

    /** Closes the screen if the Kettenkrad is gone or the player walked away. */
    @Override
    public boolean stillValid(Player player) {
        return kettenkrad.isAlive() && player.distanceToSqr(kettenkrad) < 8 * 8;
    }
}
