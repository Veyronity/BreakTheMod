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
 * along with <Your Project Name>. If not, see <https://www.gnu.org/licenses/>.
 */
package com.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.utils.*;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class staff {

    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder
                .<FabricClientCommandSource>literal("onlinestaff")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();

                    if (client.player == null) {
                        LOGGER.error("Player instance is null, cannot send feedback.");
                        return 0;
                    }

                    // Run the network request asynchronously to avoid freezing the game
                    CompletableFuture.runAsync(() -> {
                        try {
                            fetch Fetch = new fetch();

                            JsonObject staff = JsonParser.parseString(Fetch.Fetch("https://raw.githubusercontent.com/jwkerr/staff/master/staff.json", null)).getAsJsonObject();
                            JsonArray staffList = new JsonArray();
                            String[] roles = {"owner", "admin", "developer", "staffmanager", "moderator", "helper"};
                            ArrayList<String> onlineStaff = new ArrayList<>();

                            for (String role : roles) {
                                if (staff.has(role)) {
                                    JsonArray roleArray = staff.getAsJsonArray(role);
                                    for (JsonElement roleElement : roleArray) {
                                        if (roleElement.isJsonPrimitive() && roleElement.getAsJsonPrimitive().isString()) {
                                            String uuid = roleElement.getAsString();
                                            staffList.add(uuid); 
                                        } else {
                                            LOGGER.error("Unexpected element in role array for role: " + role);
                                        }
                                    }
                                }
                            }
                            int counter = 0;
                            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                                String playerName = entry.getProfile().getName();
                                if (playerName.equalsIgnoreCase(staffList.get(counter).getAsString())){
                                    onlineStaff.add(staffList.get(counter).getAsString());
                                }
                                counter++;
                            }

                            client.execute(() -> {
                                if (!onlineStaff.isEmpty()) {
                                    Text styledPart = Text.literal("").setStyle(Style.EMPTY.withColor(Formatting.AQUA));
                                    Text onlineStaffText = Text.literal(String.join(", ", onlineStaff));
                                    Text message = Text.literal("").append(styledPart).append(onlineStaffText).append(", [").append(String.valueOf(onlineStaff.size())).append("]");
                                    sendMessage(client, message);
                                } else {
                                    sendMessage(client, Text.literal("No staff online").setStyle(Style.EMPTY.withColor(Formatting.DARK_RED)));
                                }
                            });
                            
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
