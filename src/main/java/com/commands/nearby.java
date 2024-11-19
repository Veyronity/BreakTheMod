/*
 * This file is part of BreakTheMod.
 *
 * BreakTheMod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * BreakTheMod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BreakTheMod.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For more information, please visit <https://discord.gg/kwvrgt6jH5>.
 */
package com.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.utils.fetch;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.text.Style;
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
                            List<PlayerEntity> players = new ArrayList<>();

                            // Fetching players who are not under a full block above them
                            for (Entity entity : client.world.getEntities()) {
                                if (entity instanceof PlayerEntity) {
                                    PlayerEntity player = (PlayerEntity) entity;
                                    BlockPos playerPos = player.getBlockPos();
                                    BlockPos blockAbovePos = playerPos.up(2);  // Get the block 2 positions up (above the player's head)
                                    BlockState blockStateAbove = player.getWorld().getBlockState(blockAbovePos);  // Use getWorld() instead of world
                                    
                                    boolean isUnderFullBlock = blockStateAbove.isOpaqueFullCube(player.getWorld(), blockAbovePos);
                                
                                    if (!isUnderFullBlock) {
                                        players.add(player);
                                    }
                                }
                            }

                            if (players.isEmpty()) {
                                // No players found
                                client.execute(() -> sendMessage(client, Text.literal("There are no players nearby").setStyle(Style.EMPTY.withColor(Formatting.RED))));
                                return;
                            }

                            JsonArray playerData = new JsonArray();
                            for (PlayerEntity player : players) {
                                JsonObject playerInfo = new JsonObject();
                                playerInfo.addProperty("name", player.getName().getString()); // Add player name
                                playerInfo.addProperty("x", player.getX()); // X coordinate
                                playerInfo.addProperty("y", player.getY()); // Y coordinate
                                playerInfo.addProperty("z", player.getZ()); // Z coordinate

                                playerData.add(playerInfo);
                            }

                            // Send message with the list of players
                            client.execute(() -> sendMessage(client, Text.literal("Players nearby: " + playerData.toString()).setStyle(Style.EMPTY.withColor(Formatting.RED))));

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
