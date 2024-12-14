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
 * along with BreakTheMod. If not, see <https://www.gnu.org/licenses/>.
 */

package com.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class timestamps {

    public List<Long> parseTimestamp(long timestamp){
        long current = Instant.now().toEpochMilli();
        long diff = current - timestamp;
        Duration duration = Duration.ofMillis(diff);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        List<Long> list = new ArrayList<>();
        list.add(days);
        list.add(hours);
        list.add(minutes);
        return list;
    }

}
