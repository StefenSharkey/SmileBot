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

package com.stefensharkey.smilebot.listeners;

import com.stefensharkey.smilebot.Ban;
import com.stefensharkey.smilebot.Games;
import com.stefensharkey.smilebot.SmileBot;
import com.stefensharkey.smilebot.Utils;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.HashSet;

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        if (SmileBot.BAN_MODE) {
            Ban.processBans(event.getJDA().getGuilds());
        }

        Collection<Long> searchedMembers = new HashSet<>();
        String[] dateTime = Utils.getDateTime();

        try {
            Statement statement = SmileBot.getStatement();

            statement.executeUpdate(Games.DEFAULT_TABLE);

            statement.getConnection().prepareStatement("UPDATE Games " +
                    "SET enddate = '" + dateTime[0] + "', " +
                    "endtime = '" + dateTime[1] + "' " +
                    "WHERE enddate IS NULL OR endtime IS NULL").executeUpdate();
        } catch (SQLException e) {
            SmileBot.LOG.error(e.getMessage());
            e.printStackTrace();
        }

        for (var guild : event.getJDA().getGuilds()) {
            for (var member : guild.getMembers()) {
                // If the member is already in the set, ignore them.
                if (searchedMembers.add(member.getUser().getIdLong())) {
                    Games.updateGames(member);
                }
            }
        }
    }
}
