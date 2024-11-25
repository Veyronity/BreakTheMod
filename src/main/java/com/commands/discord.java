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
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.utils.Prefix;
import com.utils.fetch;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import java.util.concurrent.CompletableFuture;



public class discord {
    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder
                .<FabricClientCommandSource>literal("discordLinked")
                .then(RequiredArgumentBuilder
                    .<FabricClientCommandSource, String>argument("username", StringArgumentType.string())
                    .executes(context -> {
                        String username = StringArgumentType.getString(context, "username");
                        MinecraftClient client = MinecraftClient.getInstance();

                        if (client.player == null) {
                            LOGGER.error("Player instance is null, cannot send feedback.");
                            return 0;
                        }

                        fetch Fetch = new fetch();

                        // Async task for processing
                        CompletableFuture.runAsync(() -> {
                            try {
                                // Fetch Mojang data
                                String mojangResponse = Fetch.Fetch(
                                    "https://api.mojang.com/users/profiles/minecraft/" + username, 
                                    null
                                );
                                JsonObject mojangData = JsonParser.parseString(mojangResponse).getAsJsonObject();
                                String minecraftUUID = mojangData.get("id").getAsString();
                                String formattedUUID = minecraftUUID.substring(0, 8) + "-" + minecraftUUID.substring(8, 12) + "-" 
                                    + minecraftUUID.substring(12, 16) + "-" + minecraftUUID.substring(16, 20) + "-" 
                                    + minecraftUUID.substring(20);

                                if (!formattedUUID.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                                    client.execute(() -> sendMessage(client, Text.literal("Error: Invalid Minecraft UUID format.")
                                        .setStyle(Style.EMPTY.withColor(Formatting.RED))));
                                    return;
                                }

                                // Prepare EarthMC query
                                JsonObject payload = new JsonObject();
                                JsonArray queryArray = new JsonArray();
                                JsonObject queryItem = new JsonObject();

                                queryItem.addProperty("type", "minecraft");
                                queryItem.addProperty("target", formattedUUID);

                                queryArray.add(queryItem);
                                payload.add("query", queryArray);

                                // Fetch EarthMC data
                                String emcResponse = Fetch.Fetch(
                                    "https://api.earthmc.net/v3/aurora/discord?query=", 
                                    payload.toString()
                                );
                                JsonArray earthMCData = JsonParser.parseString(emcResponse).getAsJsonArray();

                                if (earthMCData.size() > 0) {
                                    JsonObject discordData = earthMCData.get(0).getAsJsonObject();
                                    String discordID = discordData.get("id").getAsString();

                                    // Build and send success message
                                    client.execute(() -> {
                                        Text result = Text.literal("Click Here")
                                                .setStyle(Style.EMPTY
                                                .withColor(Formatting.BLUE)
                                                .withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.com/users/" + discordID)
                                                )

                                        );
                                        client.player.sendMessage(result, false);
                                    });
                                } else {
                                    client.execute(() -> sendMessage(client, Text.literal("No Discord ID linked with the provided Minecraft username.")
                                        .setStyle(Style.EMPTY.withColor(Formatting.RED))));
                                }
                            } catch (Exception e) {
                                LOGGER.error("Command has exited with an exception: " + e.getMessage());
                                client.execute(() -> sendMessage(client, Text.literal("An error occurred while processing the command.")
                                    .setStyle(Style.EMPTY.withColor(Formatting.RED))));
                            }
                        });

                        return 1;
                    })
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
