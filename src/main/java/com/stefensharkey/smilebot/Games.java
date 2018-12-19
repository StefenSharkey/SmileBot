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

import net.dv8tion.jda.core.entities.Member;

import java.sql.SQLException;
import java.sql.Statement;

public class Games {

    public static final String DEFAULT_TABLE = "CREATE TABLE IF NOT EXISTS Games (" +
            "userid INTEGER, " +
            "gametype INTEGER, " +
            "gamename STRING, " +
            "startdate DATE, " +
            "starttime TIME, " +
            "enddate DATE, " +
            "endtime TIME)";

    public static void updateGames(Member member) {
        String[] dateTime = Utils.getDateTime();

        try {
            Statement statement = SmileBot.getStatement();

            statement.executeUpdate(DEFAULT_TABLE);

            // If the user is currently playing a game, start it.
            if (!(member.getGame() == null)) {
                statement.getConnection().prepareStatement("INSERT INTO Games VALUES (" +
                        member.getUser().getIdLong() + ", " +
                        member.getGame().getType().getKey() + ", " +
                        '\'' + SmileBot.ASTERISK.matcher(member.getGame().getName()).replaceAll("''") + "', " +
                        '\'' + dateTime[0] + "', " +
                        '\'' + dateTime[1] + "', " +
                        "NULL, " +
                        "NULL)").executeUpdate();
            }
        } catch (SQLException e) {
            SmileBot.LOG.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
