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
public class help {
    private static final Logger LOGGER = LoggerFactory.getLogger("breakthemod");

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder
                .<FabricClientCommandSource>literal("whereIs")
                    .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) {
                        LOGGER.error("Player instance is null, cannot send feedback.");
                        return 0;
                    }

                    CompletableFuture.runAsync(() -> {
                        try {
                        
                            if (client == null || client.world == null) {
                                throw new IllegalStateException("Minecraft client or world is null.");
                            }
                            Text GoTo = Text.literal("goto: Tells you how to get to a town that is not public");
                            Text Coords = Text.literal("coords: Gives you infromation about a location");
                            Text Locate = Text.literal("locate: It gives you a towns coords");
                            Text Nearby = Text.literal("nearby: Tells you all nearby users, if they are visible on the map so its legal");
                            Text WhereIs = Text.literal("whereIs: Tells you where a player is if they are visible on the mapd");
                            Text Staff = Text.literal("onlineStaff: Tells you which staff are online");
                            Text Discord = Text.literal("discordLinked: Shows you a players discord account if they have linked it");
                            Text Townless = Text.literal("townless: It shows you townless players");
                            client.execute(()->{
                                sendMessage(client, Text.literal("Commands:").setStyle(Style.EMPTY.withBold(true)));
                                sendMessage(client, GoTo);
                                sendMessage(client, Townless);
                                sendMessage(client, Coords);
                                sendMessage(client, Discord);
                                sendMessage(client, Staff);
                                sendMessage(client, WhereIs);
                                sendMessage(client, Locate);
                                sendMessage(client, Nearby);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            client.execute(() -> sendMessage(client, Text.literal("Command has exited with an exception").setStyle(Style.EMPTY.withColor(Formatting.RED))));
                            LOGGER.error("Command has exited with an exception: " + e.getMessage());
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
