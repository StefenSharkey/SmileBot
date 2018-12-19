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
import com.stefensharkey.smilebot.BanReason;
import com.stefensharkey.smilebot.Games;
import com.stefensharkey.smilebot.SmileBot;
import com.stefensharkey.smilebot.Utils;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

public class GameListener extends ListenerAdapter {

    @Override
    public void onUserUpdateGame(UserUpdateGameEvent event) {
        updateGames(event);

        if (SmileBot.BAN_MODE) {
            // Check if game is banned.
            Game game = event.getNewGame();

            if (game != null && Utils.BANNED_EXPRESSIONS.parallelStream().anyMatch(game.getName()::contains)) {
                User user = event.getUser();
                Ban.banNoInviteUser(user, event.getJDA().getMutualGuilds(user), Collections.singletonList(BanReason.SPAM_BOT_GAME));
            }
        }
    }

    private void updateGames(UserUpdateGameEvent event) {
        var dateTime = Utils.getDateTime();

        try {
            Statement statement = SmileBot.getStatement();
            statement.executeUpdate(Games.DEFAULT_TABLE);

            // If the user was previously playing a game, end it.
            if (!(event.getOldGame() == null)) {
                statement.getConnection().prepareStatement("UPDATE Games " +
                        "SET enddate = '" + dateTime[0] + "', " +
                        "endtime = '" + dateTime[1] + "' " +
                        "WHERE (userid = " + event.getUser().getIdLong() + ") " +
                        "AND (gamename = '" + SmileBot.ASTERISK.matcher(event.getOldGame().getName()).replaceAll("''") + "') " +
                        "AND (enddate IS NULL OR endtime IS NULL)").executeUpdate();
            }
        } catch (SQLException e) {
            SmileBot.LOG.error(e.getMessage());
            e.printStackTrace();
        }

        Games.updateGames(event.getMember());
    }
}
