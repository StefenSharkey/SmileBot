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
import com.stefensharkey.smilebot.SmileBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

public class ReportCommand extends Command {

    private static final String REPORT_SYNTAX = "Report Command Syntax: `report <name> [description]`\nReply in a direct message. Attach any relevant screenshots.";

    private Guild reportGuild;

    public ReportCommand() {
        name = "report";
        help = ""; // TODO
        guildOnly = false;
    }

    // TODO: See if this is synchronized or not.
    @Override
    protected void execute(CommandEvent event) {
        SmileBot.LOG.info(event.getChannelType().name());

        switch (event.getChannelType()) {
            case TEXT:
                Guild guild = event.getGuild();
                reportGuild = guild;

                event.getMessage().delete().queue();
                event.replyInDm("Report queued for `" + guild.getName() + "`.\n" + REPORT_SYNTAX);
                break;
            case PRIVATE:
                // If no guild queue exists, alert the user; otherwise, process report.
                if (reportGuild == null) {
                    event.replyInDm("No server report queued! Queue a report by typing `" + SmileBot.COMMAND_PREFIX + name + "` in the guild you want to report to.");
                } else {
                    String[] args = event.getArgs().split(" ");
                    List<Message.Attachment> attachments = event.getMessage().getAttachments();

                    if (args.length > 0 || attachments.size() > 0) {
                        List<TextChannel> reportChannels = reportGuild.getTextChannelsByName("reports", true);

                        StringBuilder reportText = new StringBuilder().append("Report from ").append(event.getAuthor().getAsMention());

                        if (args.length > 0) {
                            reportText.append(" about ").append(args[0]).append('.');

                            if (args.length > 1) {
                                reportText.append("```");

                                for (int x = 1; x < args.length; x++) {
                                    reportText.append(args[x]).append(' ');
                                }

                                reportText.append("```");
                            }
                        }

                        // Include all file attachment URLs in the message.
                        for (Message.Attachment attachment : attachments) {
                            if (attachment.isImage()) {
                                reportText.append('\n').append(attachment.getProxyUrl());
                            }
                        }

                        if (reportChannels.size() > 0) {
                            reportChannels.get(0).sendMessage(reportText).queue();
                        } else {
                            String finalReportMessage = reportText.toString();
                            reportGuild.getOwner().getUser().openPrivateChannel().queue(channel -> channel.sendMessage(finalReportMessage).queue());
                        }

                        event.replyInDm("**Report received!**");

                        reportGuild = null;
                    } else {
                        event.replyInDm(REPORT_SYNTAX);
                    }
                }
                break;
        }
    }
}
