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
package com.utils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.utils.fetch;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class staff {


    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");

    // Register client-side commands
     public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder
            .<FabricClientCommandSource>literal("staff")
            .executes(context -> {
                MinecraftClient client = MinecraftClient.getInstance();

                if (client.player == null) {
                    LOGGER.error("Player instance is null, cannot send feedback.");
                }

                try {
                    fetch Fetch = new fetch();

                    JsonObject staff = JsonParser.parseString(Fetch.Fetch("https://raw.githubusercontent.com/jwkerr/staff/master/staff.json", null)).getAsJsonObject();
                    JsonArray staffList = new JsonArray();
                    String[] roles = {"owner", "admin", "developer", "staffmanager", "moderator", "helper"};

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

                    // Create the payload to send to the second API
                    JsonObject payload = new JsonObject();
                    payload.add("query", staffList);
                    JsonObject template = new JsonObject();
                    template.addProperty("name", true);
                    template.addProperty("uuid", true);
                    template.addProperty("status", true);
                    payload.add("template", template);

                    JsonArray response = JsonParser.parseString(Fetch.Fetch("https://api.earthmc.net/v3/aurora/players", payload.toString())).getAsJsonArray();

                    List<String> onlineStaff = new ArrayList<>();

                    for (JsonElement playerElement : response) {
                        if (playerElement.isJsonObject()) {
                            JsonObject playerObj = playerElement.getAsJsonObject();
                            String name = playerObj.get("name").getAsString();
                            if (playerObj.has("status") && playerObj.get("status").isJsonObject()) {
                                JsonObject statsObj = playerObj.getAsJsonObject("status");
                    
                                if (statsObj.has("isOnline") && statsObj.get("isOnline").isJsonPrimitive() && statsObj.get("isOnline").getAsJsonPrimitive().isBoolean()) {
                                    boolean status = statsObj.get("isOnline").getAsBoolean();
                                    
                                    if (status) {
                                        onlineStaff.add(name);
                                    }
                                } else {
                                    LOGGER.error("Missing or invalid 'status' for player: " + name);
                                }
                            } else {
                                LOGGER.error("Missing or invalid 'stats' for player: " + name);
                            }
                        }
                    }

                    if (!onlineStaff.isEmpty()) {
                        client.player.sendMessage(Text.of("Online staff: " + String.join(", ", onlineStaff)), false);
                    } else {
                        client.player.sendMessage(Text.of("No staff members are online."), false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    client.player.sendMessage(Text.of("Unable to fetch staff list"), false);
                    LOGGER.error("Fetch has exited with an exception: " + e.getMessage());
                    return 0;
                }
                return 1;
            });

            dispatcher.register(command);  
        });
    }   
}