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
import com.utils.Prefix;
import com.utils.fetch;
import com.utils.timestamps;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class lastSeen {
    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder
                .<FabricClientCommandSource>literal("lastSeen")
                .then(RequiredArgumentBuilder
                    .<FabricClientCommandSource, String>argument("username", StringArgumentType.string())
                    .executes(context -> {
                        String username = StringArgumentType.getString(context, "username");
                        MinecraftClient client = MinecraftClient.getInstance();

                        if (client.player == null) {
                            LOGGER.error("Player instance is null, cannot send feedback.");
                            return 0;
                        }

                        CompletableFuture.runAsync(() -> {
                            try {
                                JsonObject payload = new JsonObject();
                                JsonObject template = new JsonObject();
                                template.addProperty("timestamps", true);
                                template.addProperty("status", true);
                                JsonArray queryArray = new JsonArray();
                                queryArray.add(username);
                                payload.add("query", queryArray);
                                
                                payload.add("template", template);

                                String response = new fetch().Fetch("https://api.earthmc.net/v3/aurora/players", payload.toString());

                                JsonElement element = JsonParser.parseString(response);
                                if (!element.isJsonArray()) {
                                    LOGGER.error("Expected JSON Array but got: " + response);
                                    client.player.sendMessage(Text.literal("Unexpected response format").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                                    return;
                                }
                                JsonArray parsedResponse = element.getAsJsonArray();

                                JsonObject timestamps = parsedResponse.get(0).getAsJsonObject().get("timestamps").getAsJsonObject();
                                long lastOnline = timestamps.get("lastOnline").getAsLong();

                                String statusMessage;
                                List<Long> offlineDate = new timestamps().parseTimestamp(lastOnline);
                                JsonElement online = parsedResponse.get(0).getAsJsonObject().get("status").getAsJsonObject().get("isOnline");
                                if (!online.getAsBoolean()) {
                                    statusMessage = String.format("%s has been offline for %d days, %d hours, and %d minutes.", username, offlineDate.get(0), offlineDate.get(1), offlineDate.get(2));
                                    Text formattedMessage = Text.literal(statusMessage).formatted(Formatting.RED);
                                    client.execute(()->{
                                        sendMessage(client, formattedMessage);
                                    });
                                } else {
                                    statusMessage = String.format("%s is currently online, for %d days, %d hours, and %d minutes.", username, offlineDate.get(0), offlineDate.get(1), offlineDate.get(2));
                                    Text formattedMessage = Text.literal(statusMessage).formatted(Formatting.GREEN);
                                    client.execute(()->{
                                        sendMessage(client, formattedMessage);
                                    });
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                client.player.sendMessage(Text.literal("Command has exited with an exception").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                                LOGGER.error("Command has exited with an exception: " + e.getMessage());
                            }
                        });

                        return 1; // Command executed successfully
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
