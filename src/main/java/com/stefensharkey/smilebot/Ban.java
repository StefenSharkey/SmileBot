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

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public class Ban {

    public static boolean shouldBan(GenericGuildMemberEvent event) {
        return processBan(event.getMember(), event.getGuild()).size() > 0;
    }

    public static void banNoInviteUser(User user, Guild guild, Iterable<BanReason> banReasons) {
        try {
            guild.getBan(user).complete();
        } catch (ErrorResponseException e) {
            switch (e.getErrorResponse()) {
                case UNKNOWN_BAN:
                    if (PermissionUtil.canInteract(guild.getSelfMember(), guild.getMember(user))) {
                        if (!guild.getMember(user).getPermissions().contains(Permission.CREATE_INSTANT_INVITE)) {
                            StringBuilder banReasonsText = new StringBuilder();
                            for (BanReason banReason : banReasons) {
                                banReasonsText.append('`').append(banReason.getText()).append("`, ");
                            }

                            String banReasonsTextFinal = banReasonsText.substring(0, banReasonsText.length() - 2);

                            // Remove leading and trailing grave symbols.
                            Pattern grave = Pattern.compile("`");
                            guild.getController().ban(user, 1, grave.matcher(banReasonsTextFinal).replaceAll("")).queue();

                            user.openPrivateChannel().queue((channel) ->
                                    channel.sendMessage("You have been detected a bot in at least one server you're in and have received a permanent ban from that server.").queue()
                            );

                            guild.getOwner().getUser().openPrivateChannel().queue((channel) ->
                                    channel.sendMessage("**Banned User:** `" + user.getName() + '#' + user.getDiscriminator() + "`\n**Server:** `" + guild.getName() + "`\n**Reason(s):** " + banReasonsTextFinal).queue());

                            SmileBot.LOG.info("Banned " + user.getName() + '#' + user.getDiscriminator() + " in " + guild.getName() + '.');
                        } else {
                            SmileBot.LOG.info("Attempted to ban " + user.getName() + '#' + user.getDiscriminator() + " in " + guild.getName() + ". Failed because user has invite permissions.");
                        }
                    } else {
                        SmileBot.LOG.info("Attempted to ban " + user.getName() + '#' + user.getDiscriminator() + " in " + guild.getName() + ". Failed because user is of an equal or higher role.");
                    }
                    break;
                default:
                    SmileBot.LOG.info("Attempted to ban " + user.getName() + '#' + user.getDiscriminator() + " in " + guild.getName() + ". Failed because" + e.getMeaning());
                    break;
            }
        }
    }

    public static void banNoInviteUser(User user, Iterable<Guild> guilds, Iterable<BanReason> banReasons) {
        for (Guild guild : guilds) {
            banNoInviteUser(user, guild, banReasons);
        }
    }

    public static void processBans(Iterable<Guild> guilds) {
        for (Guild guild : guilds) {
            for (Member member : guild.getMembers()) {
                processBan(member, guild);
            }
        }
    }

    public static void processBans(User user, Iterable<Guild> guilds) {
        for (Guild guild : guilds) {
            processBan(guild.getMember(user), guild);
        }
    }

    public static Collection<BanReason> processBan(Member member, Guild guild) {
        return processBan(member, guild, null);
    }

    public static Collection<BanReason> processBan(Member member, Guild guild, Message message) {
        try {
            User user = member.getUser();
            Collection<Guild> banGuilds = new ArrayList<>();
            Collection<BanReason> banReasons = new ArrayList<>();

            if (Utils.BANNED_EXPRESSIONS.parallelStream().anyMatch(member.getEffectiveName()::contains)) {
                banGuilds.add(guild);
                banReasons.add(BanReason.SPAM_BOT_NICKNAME);
            }

            if (Utils.BANNED_EXPRESSIONS.parallelStream().anyMatch(user.getName()::contains)) {
                banReasons.add(BanReason.SPAM_BOT_USERNAME);
                banGuilds.addAll(user.getMutualGuilds());
            }

            if (member.getGame() != null && Utils.BANNED_EXPRESSIONS.parallelStream().anyMatch(member.getGame().getName()::contains)) {
                banReasons.add(BanReason.SPAM_BOT_GAME);
                banGuilds.addAll(user.getMutualGuilds());
            }

            if (message != null && Utils.BANNED_EXPRESSIONS.parallelStream().anyMatch(message.getContentStripped()::contains)) {
                banReasons.add(BanReason.SPAM_BOT_MESSAGE);
                banGuilds.add(guild);
            }

            if (banReasons.size() > 0) {
                banNoInviteUser(user, banGuilds, banReasons);
            }

            return banReasons;
        } catch (NullPointerException e) {
            // TODO: Figure out why PatchBot causes NPE.
            SmileBot.LOG.error(e.getLocalizedMessage());

            return null;
        }
    }
}
