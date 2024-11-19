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
import net.minecraft.text.ClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.concurrent.CompletableFuture;

public class locate {
    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder
                .<FabricClientCommandSource>literal("locate")
                .then(RequiredArgumentBuilder
                    .<FabricClientCommandSource, String>argument("name", StringArgumentType.string())
                    .then(RequiredArgumentBuilder
                        .<FabricClientCommandSource, String>argument("type", StringArgumentType.string())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            String type = StringArgumentType.getString(context, "type").toLowerCase();
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
                                    queryArray.add(name);
                                    payload.add("query", queryArray);  
                                    JsonObject template = new JsonObject();
                                    template.addProperty("coordinates", true);  
                                    payload.add("template", template);

                                    String apiUrl = "";
                                    if ("town".equals(type)) {
                                        apiUrl = "https://api.earthmc.net/v3/aurora/towns";
                                    } else if ("nation".equals(type)) {
                                        apiUrl = "https://api.earthmc.net/v3/aurora/nations";
                                    } else {
                                        client.player.sendMessage(Text.literal("Invalid type! Use 'town' or 'nation'.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                                        return;
                                    }

                                    JsonArray response = JsonParser.parseString(FetchInstance.Fetch(apiUrl, payload.toString())).getAsJsonArray();

                                    if (response.size() > 0) {
                                        JsonObject coordinates = response.get(0).getAsJsonObject().get("coordinates").getAsJsonObject().get("spawn").getAsJsonObject();
                                        int x = coordinates.get("x").getAsInt();
                                        int z = coordinates.get("z").getAsInt();

                                        String hyperlink = String.format("https://map.earthmc.net/?world=minecraft_overworld&zoom=3&x=%d&z=%d", x, z);
                                        Text message = Text.literal(String.format("%s is located at X: %d, Z: %d. ", name, x, z))
                                            .append(Text.literal("Click Here").formatted(Formatting.AQUA).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, hyperlink))));
                                        
                                        client.player.sendMessage(message, false);
                                    } else {
                                        client.player.sendMessage(Text.literal("Location not found.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    client.player.sendMessage(Text.literal("Command exited with an exception.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
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
}
