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

import net.dv8tion.jda.core.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GuildVoiceListener extends ListenerAdapter {

    private final static double AIRHORN_CHANCE = 0.04;

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        summonAirhorn(event);
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getChannelJoined() != event.getGuild().getAfkChannel()) {
            summonAirhorn(event);
        }
    }

    private void summonAirhorn(GenericGuildVoiceEvent event) {
        // If the user is not a bot, and the random double is less than or equal to the airhorn chance, continue.
        if (!event.getMember().getUser().isBot() && new Random().nextDouble() <= AIRHORN_CHANCE) {
            AudioManager manager = event.getGuild().getAudioManager();
//            SmileBot.connectToVoiceChannel(manager, event.getMember().getVoiceState().getChannel());

            // Use a new thread to execute this so that we don't hang this one.
            new Thread(() -> {
                try {
                    // While the current thread is not interrupted, and the audio manager is not connected, sleep 250ms.
                    while (!Thread.currentThread().isInterrupted() && !manager.isConnected()) {
                        TimeUnit.MILLISECONDS.sleep(250L);
                    }

                    event.getGuild().getTextChannelsByName("admin_chat", true).get(0).sendMessage("!airhorn").queue();
//                    SmileBot.leaveVoiceChannel(manager);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}
