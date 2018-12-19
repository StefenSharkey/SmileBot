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
import com.stefensharkey.smilebot.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GuildMemberListener extends ListenerAdapter {

    public static final String SMILING_FREAK_ACCOUNCEMENT = "One day I will create an announcement.";

    public static final String[] WELCOME_MESSAGES = {
            "%name% must love us, because here they are.",
            "%name%, to be our friend, you must answer this. Do you love us??",
            "I wonder what you smell like. Luckily, I won't have to wait long, since %name% just arri...oh my, that is rancid.",
            "My favorite spit stain, %name%, was just coughed up.",
            "Ah crap, not %name% again. MOM, GRAB THE GUN.",
    };

    public static final String[] GOODBYE_MESSAGES = {
            "Goodbye, %name%.",
            "Our love wasn't strong enough for %name%. Good night, sweet prince.",
            "I guess we'll never know what post-mortal strangulation feels like from %name%.",
            "By leaving, %name% proved %god% does listen to our prayers.",
    };


    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (SmileBot.BAN_MODE && !Ban.shouldBan(event)) {
            Guild guild = event.getGuild();
            Member member = event.getMember();

            GuildController controller = guild.getController();
            List<Role> newfreakRoles = guild.getRolesByName("newfreak", true);

            if (newfreakRoles.size() > 0) {
                controller.addSingleRoleToMember(member, newfreakRoles.get(0)).queue();
            }

            // Welcome the user.
            int messageChoice = new Random().nextInt(WELCOME_MESSAGES.length);
            String message = Utils.formatMessage(WELCOME_MESSAGES[messageChoice], member);

            Objects.requireNonNull(guild.getDefaultChannel()).sendMessage(message).queue();
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        if (SmileBot.BAN_MODE && !Ban.shouldBan(event)) {
            int messageChoice = new Random().nextInt(GOODBYE_MESSAGES.length);
            String message = Utils.formatMessage(GOODBYE_MESSAGES[messageChoice], event.getMember());
            Guild guild = event.getGuild();

            Objects.requireNonNull(guild.getDefaultChannel()).sendMessage(message).queue();
        }
    }

    @Override
    public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
        if (SmileBot.BAN_MODE) {
            Ban.processBan(event.getMember(), event.getGuild());
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        List<Role> roles = event.getRoles();
        Guild guild = event.getGuild();

        for (Role role : roles) {
            String roleMessage = "";

            switch (role.getName().toLowerCase()) {
                case "freak":
                    if (role.getName().equalsIgnoreCase("freak")) {
                        roleMessage = ":trumpet: **The Freak Ceremony ** :trumpet:\n" +
                                "%name% has been declared a %role%.\n\n";
                        roleMessage += Utils.randomizeCase(SMILING_FREAK_ACCOUNCEMENT);
                    }
                    break;
                case "stefen approved":
                    roleMessage = "Henceforth, %name% shall be known as %role%.\nBeing %role% is, of course, one of the highest honors. You must be very excited.";
                    break;
            }

            // If the role is not freak, then we do nothing.
            try {
                Objects.requireNonNull(guild.getDefaultChannel()).sendMessage(Utils.formatMessage(roleMessage, event.getMember(), role)).queue();
            } catch (IllegalArgumentException e) {
                // Do nothing.
            } catch (NullPointerException e) {
                guild.getTextChannels().get(0).sendMessage(Utils.formatMessage(roleMessage, event.getMember(), role)).queue();
            }
        }
    }
}
