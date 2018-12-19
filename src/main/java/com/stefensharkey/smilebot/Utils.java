/*
 * SmileBot - A Discord bot.
 * Copyright (C) 2018 Stefen Sharkey
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stefensharkey.smilebot;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.User;
import org.sqlite.SQLiteConfig;
import org.sqlite.date.FastDateFormat;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class Utils {

    public static final List<String> BANNED_EXPRESSIONS = Arrays.asList(
            "discord.gg",
            "discord.me",
            "paypal.me",
            "selly.gg",
            "senseibin",
            "twitch.tv",
            "twitter.com",
            "(tag)",
            "findme"
    );
    private static final Pattern NAME = Pattern.compile("%name%");
    private static final Pattern ROLE = Pattern.compile("%role%");
    private static final Pattern GOD = Pattern.compile("%god%");

    public static String formatMessage(String message, User user, Guild guild) {
        return formatMessage(message, guild.getMember(user));
    }

    public static String formatMessage(String message, IMentionable member) {
        return formatMessage(message, member, null);
    }

    public static String formatMessage(String message, @Nullable IMentionable member, IMentionable role) {
        if (member != null) {
            message = NAME.matcher(message).replaceAll(member.getAsMention());
        }

        if (role != null) {
            message = ROLE.matcher(message).replaceAll(role.getAsMention());
        }

        return GOD.matcher(message).replaceAll(SmileBot.jda.getUserById("254423536859348992").getAsMention());
    }

    public static String randomizeCase(String input) {
        StringBuilder output = new StringBuilder();

        input = input.toLowerCase();

        for (char character : input.toCharArray()) {
            boolean makeUpper = new Random().nextBoolean();

            if (makeUpper) {
                character = Character.toUpperCase(character);
            }

            output.append(character);
        }

        return output.toString();
    }

    public static String[] getDateTime() {
        return FastDateFormat.getInstance(SQLiteConfig.DEFAULT_DATE_STRING_FORMAT).format(new Date()).split(" ");
    }
}
