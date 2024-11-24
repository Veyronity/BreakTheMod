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
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.utils.Prefix;
import com.utils.fetch;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.text.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.arguments.DoubleArgumentType;


public class coords {
    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder
                .<FabricClientCommandSource>literal("coords")
                .then(RequiredArgumentBuilder
                    .<FabricClientCommandSource, Double>argument("x", DoubleArgumentType.doubleArg())
                    .then(RequiredArgumentBuilder
                        .<FabricClientCommandSource, Double>argument("z", DoubleArgumentType.doubleArg())
                        .executes(context -> {
                            double x = DoubleArgumentType.getDouble(context, "x");
                            double z = DoubleArgumentType.getDouble(context, "z");
                            MinecraftClient client = MinecraftClient.getInstance();

                            if (client.player == null) {
                                LOGGER.error("Player instance is null, cannot send feedback.");
                                return 0;
                            }

                            CompletableFuture.runAsync(() -> {
                                try {
                                    fetch FetchInstance = new fetch();

                                    JsonObject payload = new JsonObject();
                                    JsonArray queryArray = new JsonArray();
                                    JsonArray coordinatesArray = new JsonArray();
                                    coordinatesArray.add(x);
                                    coordinatesArray.add(z);
                                    queryArray.add(coordinatesArray);
                                    
                                    payload.add("query", queryArray);

                                    String apiUrl = "https://api.earthmc.net/v3/aurora/location";

                                    JsonArray locationData = JsonParser.parseString(FetchInstance.Fetch(apiUrl, payload.toString())).getAsJsonArray();

                                    if (locationData != null && locationData.size() == 1 && locationData.get(0).isJsonObject()) {
                                        JsonObject data = locationData.get(0).getAsJsonObject();

                                        if (data.get("isWilderness").getAsBoolean()) {
                                            sendMessage(client,Text.literal("Location is Wilderness").setStyle(Style.EMPTY.withColor(Formatting.AQUA)));
                                        } else {
                                            JsonObject town = data.has("town") ? data.get("town").getAsJsonObject() : new JsonObject();
                                            String townName = town.has("name") ? town.get("name").getAsString() : "Unknown";

                                            JsonObject nation = data.has("nation") ? data.get("nation").getAsJsonObject() : new JsonObject();
                                            String nationName = nation.has("name") ? nation.get("name").getAsString() : null;

                                            if (nationName != null) {
                                                client.execute(()->{
                                                    sendMessage(client,Text.literal(String.format("Location is at town %s, part of %s", townName, nationName)).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                                                });
                                            } else {
                                                client.execute(()->{
                                                    sendMessage(client,Text.literal(String.format("Location is at town %s, not part of any nation", townName)).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                                                });
                                            }
                                        }
                                    } else {
                                        client.execute(()->{
                                            sendMessage(client,Text.literal("Unexpected API response format.").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    client.execute(()->{
                                        sendMessage(client, Text.literal("Command exited with an exception.").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                    });
                                    LOGGER.error("Command exited with an exception: " + e.getMessage());
                                }
                            });

                            return 1;  // Command executed successfully
                        })
                    )
                );

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