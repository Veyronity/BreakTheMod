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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class overclaimable {
    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");
    private static final timestamps timeUtil = new timestamps();

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder
                .<FabricClientCommandSource>literal("overclaimable")
                .then(RequiredArgumentBuilder
                    .<FabricClientCommandSource, String>argument("town", StringArgumentType.string())
                    .executes(context -> {
                        String town = StringArgumentType.getString(context, "town");
                        MinecraftClient client = MinecraftClient.getInstance();

                        if (client.player == null) {
                            LOGGER.error("Player instance is null, cannot send feedback.");
                            return 0;
                        }

                        CompletableFuture.runAsync(() -> {
                            try {
                                fetch fetchInstance = new fetch();
                                JsonObject payload = new JsonObject();
                                JsonArray queryArray = new JsonArray();
                                queryArray.add(town);
                                payload.add("query", queryArray);

                                String apiUrl = "https://api.earthmc.net/v3/aurora/towns";
                                JsonArray response = JsonParser.parseString(fetchInstance.Fetch(apiUrl, payload.toString())).getAsJsonArray();

                                if (response.size() > 0) {
                                    JsonObject townData = response.get(0).getAsJsonObject();
                                    boolean isOverclaimed = townData.get("status").getAsJsonObject().get("isOverClaimed").getAsBoolean();

                                    if (isOverclaimed) {
                                        sendMessage(client, Text.literal(town + " is already overclaimed.")
                                                .setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                        return;
                                    }

                                    JsonArray residents = townData.get("residents").getAsJsonArray();
                                    List<Long> inactiveResidents = new ArrayList<>();
                                    long soonestOverclaimableDays = Long.MAX_VALUE;  // Track the minimum number of days left
                                    JsonArray Residents = JsonParser.parseString(fetchInstance.Fetch(
                                        "https://api.earthmc.net/v3/aurora/players", payload.toString()
                                    )).getAsJsonArray();

                                    for (JsonElement residentElement : Residents) {
                                        JsonObject resident = residentElement.getAsJsonObject();
                                        long lastOnline = resident.get("timestamps")
                                                                  .getAsJsonObject()
                                                                  .get("lastOnline")
                                                                  .getAsLong();
                                        List<Long> parsedLastOnline = timeUtil.parseTimestamp(lastOnline);

                                        if (parsedLastOnline.get(0) > 42) {
                                            inactiveResidents.add(lastOnline);
                                        } else {
                                            // Calculate days left until 42 days are reached
                                            long daysInactive = parsedLastOnline.get(0);
                                            long daysLeft = 42 - daysInactive;
                                            soonestOverclaimableDays = Math.min(soonestOverclaimableDays, daysLeft);
                                        }
                                    }

                                    int totalResidents = residents.size();
                                    int neededInactiveResidents = (totalResidents / 2) + 1;  // Example logic: 50% + 1 must be inactive

                                    if (inactiveResidents.size() >= neededInactiveResidents) {
                                        sendMessage(client, Text.literal(town + " is now overclaimable!")
                                                .setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                                    } else {
                                        int remaining = neededInactiveResidents - inactiveResidents.size();
                                        if (soonestOverclaimableDays != Long.MAX_VALUE) {
                                            sendMessage(client, Text.literal(town + " is not overclaimable yet. " +
                                                    remaining + " more residents need to be inactive. " +
                                                    "The town will be overclaimable in " + soonestOverclaimableDays + " days.")
                                                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
                                        } else {
                                            sendMessage(client, Text.literal(town + " is not overclaimable yet, and no residents are close to becoming inactive.")
                                                    .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
                                        }
                                    }

                                } else {
                                    sendMessage(client, Text.literal("No data found for the town.")
                                            .setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                }

                            } catch (Exception e) {
                                LOGGER.error("Command exited with an exception: ", e);
                                sendMessage(client, Text.literal("Command exited with an exception.")
                                        .setStyle(Style.EMPTY.withColor(Formatting.RED)));
                            }
                        });

                        return 1;
                    })
                );

            dispatcher.register(command);
        });
    }
    
    private static void sendMessage(MinecraftClient client, Text message) {
        client.execute(() -> client.player.sendMessage(message, false));
    }
}
