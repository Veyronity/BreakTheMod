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
import com.mojang.brigadier.arguments.StringArgumentType;
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

                        CompletableFuture.runAsync(() -> {
                            try {
                                String mojangResponse = Fetch.Fetch("https://api.mojang.com/users/profiles/minecraft/" + username, null);
                                JsonObject mojangData = JsonParser.parseString(mojangResponse).getAsJsonObject();

                                String minecraftUUID = mojangData.get("id").getAsString();
                                JsonArray formattedUUID = new JsonArray();
                                
                                formattedUUID.add(minecraftUUID.substring(0, 8) + "-" + minecraftUUID.substring(8, 12) + "-" 
                                + minecraftUUID.substring(12, 16) + "-" + minecraftUUID.substring(16, 20) + "-" 
                                + minecraftUUID.substring(20));


                                if (formattedUUID.get(0).getAsString().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {

                                    JsonObject payload = new JsonObject();
                                    JsonArray Minecraft = new JsonArray();
                                    JsonObject type = new JsonObject();

                                    type.addProperty("type","minecraft");
                                    type.addProperty("target", formattedUUID.get(0).getAsString());

                                    Minecraft.add(type);
                                    

                                    payload.add("query", Minecraft);
                                    String emcResponse = Fetch.Fetch("https://api.earthmc.net/v3/aurora/discord?query=", payload.toString());

                                    JsonArray earthMCData = JsonParser.parseString(emcResponse).getAsJsonArray();

                                    if (earthMCData.size() > 0) {
                                        JsonObject discordData = earthMCData.get(0).getAsJsonObject();
                                        String discordID = discordData.get("id").getAsString();

                                        // Send result to player
                                        client.execute(() -> {
                                            Text result = Text.literal("Discord info for Username '" + username + "':\n")
                                                .append(Text.literal("Discord ID: " + discordID).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                                            client.player.sendMessage(result, false);
                                        });
                                    } else {
                                        client.execute(() -> client.player.sendMessage(Text.literal("No Discord ID linked with the provided Minecraft username.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false));
                                    }
                                } else {
                                    client.execute(() -> client.player.sendMessage(Text.literal("Error: Invalid Minecraft UUID format.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                client.execute(() -> client.player.sendMessage(Text.literal("An error occurred while processing the command.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false));
                                LOGGER.error("Command has exited with an exception: " + e.getMessage());
                            }
                        });

                        return 1;
                    })
                );

            dispatcher.register(command);
        });
    }
}