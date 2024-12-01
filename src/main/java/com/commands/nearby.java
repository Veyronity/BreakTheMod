/*
 * This file is part of BreakTheMod.
 *
 * BreakTheMod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BreakTheMod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BreakTheMod. If not, see <https://www.gnu.org/licenses/>.
 */
package com.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.text.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;
import com.utils.Prefix;
import net.minecraft.util.Identifier;

public class nearby {
    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder
                .<FabricClientCommandSource>literal("nearby")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();

                    if (client.player == null) {
                        LOGGER.error("Player instance is null, cannot send feedback.");
                        return 0;
                    }

                    CompletableFuture.runAsync(() -> {
                        try {
                            List<String> playerInfoList = new ArrayList<>();

                            for (Entity entity : client.world.getEntities()) {
                                if (entity instanceof PlayerEntity && entity != client.player) {
                                    PlayerEntity otherPlayer = (PlayerEntity) entity;

                                    // Skip invisible players
                                    if (otherPlayer.isInvisible()) {
                                        continue;
                                    }

                                    // Check for the conditions to hide the player
                                    boolean inRiptide = isPlayerInRiptideAnimation(otherPlayer);
                                    boolean inNether = isInNether(otherPlayer);
                                    boolean inVehicle = isInVehicle(otherPlayer);
                                    boolean sneaking = isSneaking(otherPlayer);

                                    // Skip the player if they are sneaking, in a vehicle, or in the Nether
                                    if (sneaking || inVehicle || inNether || inRiptide) {
                                        continue;
                                    }

                                    Vec3d otherPlayerPos = otherPlayer.getPos();
                                    BlockPos playerBlockPos = new BlockPos(
                                        (int) Math.floor(otherPlayerPos.getX()),
                                        (int) Math.floor(otherPlayerPos.getY()),
                                        (int) Math.floor(otherPlayerPos.getZ())
                                    );

                                    boolean isUnderAnyBlock = false;
                                    for (int y = playerBlockPos.getY() + 1; y <= client.world.getTopY(); y++) { 
                                        BlockPos checkPos = new BlockPos(playerBlockPos.getX(), y, playerBlockPos.getZ());
                                        BlockState blockStateAbove = client.world.getBlockState(checkPos);

                                        if (!blockStateAbove.isAir()) { 
                                            isUnderAnyBlock = true;
                                            break;
                                        }
                                    }

                                    if (!isUnderAnyBlock) { 
                                        double distance = client.player.getPos().distanceTo(otherPlayerPos);
                                        String otherPlayerName = otherPlayer.getName().getString();
                                        int x = (int) otherPlayerPos.getX();
                                        int z = (int) otherPlayerPos.getZ();

                                        String status = "";
                                        playerInfoList.add(String.format("- %s (%d, %d) distance: %.1f blocks %s",
                                            otherPlayerName, x, z, distance, status));
                                    }
                                }
                            }

                            if (playerInfoList.isEmpty()) {
                                client.execute(() -> sendMessage(client, Text.literal("There are no players nearby").setStyle(Style.EMPTY.withColor(Formatting.RED))));
                                return;
                            }

                            MutableText header = Text.literal("Players nearby:\n").setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
                            MutableText playersText = Text.literal("");

                            for (String playerInfo : playerInfoList) {
                                playersText.append(Text.literal(playerInfo + "\n").setStyle(Style.EMPTY.withColor(Formatting.AQUA)));
                            }

                            client.execute(() -> sendMessage(client, header.append(playersText)));

                        } catch (Exception e) {
                            e.printStackTrace();
                            client.execute(() -> sendMessage(client, Text.literal("Command has exited with an exception").setStyle(Style.EMPTY.withColor(Formatting.RED))));
                            LOGGER.error("Fetch has exited with an exception: " + e.getMessage());
                        }
                    });

                    return 1;
                });

            dispatcher.register(command);
        });
    }

    private static boolean isPlayerInRiptideAnimation(PlayerEntity player) {
        return player.getActiveItem().getItem().toString().contains("riptide");
    }

    private static boolean isInNether(PlayerEntity player) {
        // Check if the player is in the Nether based on dimension ID
        return player.getWorld().getRegistryKey().getValue().equals(new Identifier("minecraft", "nether"));
    }

    private static boolean isInVehicle(PlayerEntity player) {
        return player.getVehicle() != null && 
               (player.getVehicle() instanceof net.minecraft.entity.vehicle.BoatEntity ||
                player.getVehicle() instanceof net.minecraft.entity.vehicle.MinecartEntity);
    }

    private static boolean isSneaking(PlayerEntity player) {
        return player.isSneaking();
    }

    private static void sendMessage(MinecraftClient client, Text message) {
        client.execute(() -> {
            if (client.player != null) {
                Text prefix = Prefix.getPrefix();
                Text chatMessage = Text.literal("").append(prefix).append(message);
                client.player.sendMessage(chatMessage, false);
            }
        });
    }
}
