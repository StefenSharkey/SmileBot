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

package com.stefensharkey.smilebot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.stefensharkey.smilebot.Utils;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@CommandInfo(
        name = "Roulette",
        description = "Randomly selects a member from within the guild or other selection."
)
@Author("Stefen Sharkey")
public class RouletteCommand extends Command {

    private static final String[] ROULETTE_SUCCESS_MESSAGES = {
            "%name% must become one with the darkness.",
            "%name%, you are the chosen one.",
    };

    private static final String[] ROULETTE_FAIL_MESSAGES = {
            "See, %name%, this is why we can't have nice things.",
            "%name%, you dun goof'd.",
    };

    public RouletteCommand() {
        name = "roulette";
        help = "Randomly selects a member from within the server or other selection.";
    }

    @Override
    protected void execute(CommandEvent event) {
        // Create a HashSet since we only want unique members.
        Set<Member> members;
        String[] args = event.getArgs().split(" ");

        if (args.length > 0) {
            members = new HashSet<>();
            Message message = event.getMessage();
            Guild guild = event.getGuild();

            // Add all mentioned members.
            List<Member> mentionedMembers = message.getMentionedMembers();

            if (mentionedMembers.size() > 0) {
                members.addAll(mentionedMembers);
            }

            // Add all members mentioned by role.
            List<Role> mentionedRoles = message.getMentionedRoles();

            if (mentionedRoles.size() > 0) {
                members.addAll(guild.getMembersWithRoles(mentionedRoles));
            }

            List<IMentionable> mentionedMisc = message.getMentions(Message.MentionType.HERE);

            // For all mentioned users, remove any invalid ones.
            if (mentionedMisc.size() > 0) {
                for (Member member : members) {
                    OnlineStatus status = member.getOnlineStatus();
                    if (status != OnlineStatus.INVISIBLE && status != OnlineStatus.OFFLINE && status != OnlineStatus.UNKNOWN) {
                        members.add(member);
                    }
                }
            }

            // Remove SmileBot if only mentioned once. SmileBot will always be mentioned at least once.
            boolean containsSelf = Arrays.asList(args).contains(event.getSelfUser().getAsMention());

            if (!containsSelf) {
                members.remove(guild.getMember(event.getJDA().getSelfUser()));
            }

            // Add partial names.
            for (String arg : args) {
                String partialName = arg.toLowerCase();

                for (Member member : guild.getMembers()) {
                    if (member.getEffectiveName().toLowerCase().contains(partialName) || member.getUser().getName().toLowerCase().contains(partialName)) {
                        members.add(member);
                    }
                }
            }
        } else {
            // Add all members.
            members = new HashSet<>(event.getGuild().getMembers());
        }

        // Randomly pick a member.
        String reply;

        if (members.size() > 0) {
            int memberIndex = new Random().nextInt(members.size());
            int messageChoice = new Random().nextInt(ROULETTE_SUCCESS_MESSAGES.length);
            reply = Utils.formatMessage(ROULETTE_SUCCESS_MESSAGES[messageChoice], new ArrayList<>(members).get(memberIndex));
        } else {
            int messageChoice = new Random().nextInt(ROULETTE_FAIL_MESSAGES.length);
            reply = Utils.formatMessage(ROULETTE_FAIL_MESSAGES[messageChoice], event.getMember());
        }

        event.reply(reply);
    }
}
