package com.allianceering.client;

import com.allianceering.client.gui.AllianceRingScreen;
import net.minecraft.client.Minecraft;

/**
 * Small client-only helper used to open the Alliance / Ring screen.
 * Keeping this in its own class (separate from the Item class) ensures
 * client-only classes like Minecraft/Screen are never touched on a dedicated server.
 */
public final class AllianceRingClientHandler {
    private AllianceRingClientHandler() {}

    public static void openAllianceRingScreen() {
        Minecraft.getInstance().setScreen(new AllianceRingScreen());
    }
}
