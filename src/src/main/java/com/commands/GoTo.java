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
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GoTo {
    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                LiteralArgumentBuilder.<FabricClientCommandSource>literal("goto")
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("name", StringArgumentType.string())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            return handleGoToCommand(name, MinecraftClient.getInstance());
                        })
                    )
            );
        });
    }

    private static int handleGoToCommand(String townName, MinecraftClient client) {
        if (client.player == null) {
            LOGGER.error("Player instance is null, cannot send feedback.");
            return 0;
        }
    
        CompletableFuture.runAsync(() -> {
            try {
                int radius = 500;
                int maxAttempts = 3;
                

                while (maxAttempts > 0) {
                    fetch fetchInstance = new fetch();
                    JsonObject payload = new JsonObject();
                    JsonArray queryArray = new JsonArray();
                    JsonObject query = new JsonObject();
    
                    query.addProperty("target_type", "TOWN");
                    query.addProperty("target", townName);
                    query.addProperty("search_type", "TOWN");
                    query.addProperty("radius", radius);
                    queryArray.add(query);
                    payload.add("query", queryArray);
    
                    String apiUrl = "https://api.earthmc.net/v3/aurora/nearby";
                    String UnparsedResponse = fetchInstance.Fetch(apiUrl, payload.toString());
                    LOGGER.debug(UnparsedResponse);
                    JsonArray response = JsonParser.parseString(UnparsedResponse).getAsJsonArray();
    
                    List<JsonObject> validTowns = new ArrayList<>();
                    JsonArray towns = new JsonArray();
                    Integer j = 0;

                    for (JsonElement element : response) {
                        JsonObject town = element.getAsJsonArray().get(j).getAsJsonObject();
                        String townNameFromResponse = town.get("name").getAsString(); 
    
                        towns.add(townNameFromResponse);
                    }
    
                    JsonObject townDetailsPayload = new JsonObject();
                    townDetailsPayload.add("query", towns);
    
                    String townDetailsUrl = "https://api.earthmc.net/v3/aurora/towns/";
                    String townDetailsResponse = fetchInstance.Fetch(townDetailsUrl, townDetailsPayload.toString());
                    JsonArray townDetailsArray = JsonParser.parseString(townDetailsResponse).getAsJsonArray();
    
                    for (JsonElement townDetailElement : townDetailsArray) {
                        JsonObject townDetail = townDetailElement.getAsJsonObject();
                        JsonObject status = townDetail.get("status").getAsJsonObject();
                        LOGGER.debug(status.getAsString());
                        if (status.get("isPublic").getAsBoolean() && status.get("canOutsidersSpawn").getAsBoolean()) {
                            validTowns.add(townDetail);

                        } else if (status.get("isCapital").getAsBoolean()) {
                            String nationUuid = townDetail.get("nation").getAsJsonObject().get("uuid").getAsString();
                            JsonObject nationDetailsPayload = new JsonObject();
                            JsonArray nationQueryArray = new JsonArray();
                            nationQueryArray.add(nationUuid);
                            nationDetailsPayload.add("query", nationQueryArray);
    
                            String nationDetailsUrl = "https://api.earthmc.net/v3/aurora/nations/";
                            JsonObject nationDetails = JsonParser.parseString(fetchInstance.Fetch(nationDetailsUrl, nationDetailsPayload.toString())).getAsJsonObject();
    
                            if (nationDetails.has("isPublic") && nationDetails.get("status").getAsJsonObject().get("isPublic").getAsBoolean()) {
                                validTowns.add(townDetail);
                            }
                        }
                    }
    
                    if (validTowns.size() >= 1) {
                        for (JsonObject validTown : validTowns) {
                            client.player.sendMessage(
                                Text.literal("Found suitable spawn in: " + validTown.get("name").getAsString())
                                .setStyle(Style.EMPTY.withColor(Formatting.GREEN)), false);
                        }
                        return;
                    }
    
                    radius += 500;
                    maxAttempts--;
                }
    
                client.player.sendMessage(Text.literal("No suitable spawns found.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            } catch (Exception e) {
                e.printStackTrace();
                client.player.sendMessage(Text.literal("Command exited with an exception.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                LOGGER.error("Command exited with an exception: " + e.getMessage());
            }
        });
    
        return 1;
    }
    
}

