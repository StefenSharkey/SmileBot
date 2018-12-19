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
import com.stefensharkey.smilebot.SmileBot;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class UserListener extends ListenerAdapter {

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        User user = event.getUser();

        if (SmileBot.BAN_MODE) {
            Ban.processBans(user, user.getMutualGuilds());
        }
    }
}
