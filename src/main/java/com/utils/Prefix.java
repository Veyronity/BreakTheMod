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
package com.utils;
import net.minecraft.text.*;

public class Prefix{

    public static TextColor getColorFromHex(String hex) {
        try {
            if (!hex.startsWith("#")) {
                throw new IllegalArgumentException("Invalid hex color format. Must start with #.");
            }
            int color = Integer.parseInt(hex.substring(1), 16); 
            return TextColor.fromRgb(color);  
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex color format. Ensure it is in #RRGGBB format.", e);
        }
    }
    
    public static Text getPrefix(){
        MutableText prefix = Text.empty()
        .append(Text.literal("B").setStyle(Style.EMPTY.withColor(getColorFromHex("#EAEAEA"))))
        .append(Text.literal("r").setStyle(Style.EMPTY.withColor(getColorFromHex("#EAEAEA"))))
        .append(Text.literal("e").setStyle(Style.EMPTY.withColor(getColorFromHex("#EAEAEA"))))
        .append(Text.literal("a").setStyle(Style.EMPTY.withColor(getColorFromHex("#EAEAEA"))))
        .append(Text.literal("k").setStyle(Style.EMPTY.withColor(getColorFromHex("#EAEAEA"))))
        .append(Text.literal("T").setStyle(Style.EMPTY.withColor(getColorFromHex("#4B56FF"))))
        .append(Text.literal("h").setStyle(Style.EMPTY.withColor(getColorFromHex("#4B56FF"))))
        .append(Text.literal("e").setStyle(Style.EMPTY.withColor(getColorFromHex("#4B56FF"))))
        .append(Text.literal("M").setStyle(Style.EMPTY.withColor(getColorFromHex("#FF8C1A"))))
        .append(Text.literal("o").setStyle(Style.EMPTY.withColor(getColorFromHex("#FF8C1A"))))
        .append(Text.literal("d").setStyle(Style.EMPTY.withColor(getColorFromHex("#FF8C1A"))))
        .append(Text.literal(">> ").setStyle(Style.EMPTY.withColor(getColorFromHex("#FFFFFF")))); // Separator
        return prefix;
    }
}