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

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.stefensharkey.smilebot.commands.ReportCommand;
import com.stefensharkey.smilebot.commands.RouletteCommand;
import com.stefensharkey.smilebot.listeners.GameListener;
import com.stefensharkey.smilebot.listeners.GuildListener;
import com.stefensharkey.smilebot.listeners.GuildMemberListener;
import com.stefensharkey.smilebot.listeners.MessageListener;
import com.stefensharkey.smilebot.listeners.ReadyListener;
import com.stefensharkey.smilebot.listeners.UserListener;
import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.utils.JDALogger;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SmileBot {

    public static final Logger LOG = JDALogger.getLog("SmileBot");
    private static Statement statement;

    public static JDA jda;

    public static final String COMMAND_PREFIX = "!!";
    public static final Pattern ASTERISK = Pattern.compile("'");

    public static final boolean BAN_MODE = true;

    public static void main(String[] args) throws InterruptedException, IOException, LoginException {
        new SmileBot();
    }

    private SmileBot() throws InterruptedException, IOException, LoginException {
        // Start the SQLite database in the background.
        new Thread(this::setupDatabase).start();

        boolean rebuildJDA = false;

        String token = Files.lines(Paths.get("token")).collect(Collectors.joining());
        JDABuilder jdaBuilder = new JDABuilder(token);
        EventWaiter waiter = new EventWaiter();
        CommandClientBuilder client = new CommandClientBuilder()
                .setPrefix(COMMAND_PREFIX)
                .setGame(Game.of(Game.GameType.WATCHING, "you sleep. ( ͡° ͜ʖ ͡°)"))
                .setOwnerId("254423536859348992")
                .addCommands(
                    new AboutCommand(Color.RED, "a Smiling Freak.",
                            new String[]{"Being a freak"},
                            Permission.ADMINISTRATOR),
                    new PingCommand(),
                    new ReportCommand(),
                    new RouletteCommand());

        // If Lavalink works, use it.
        try {
            jda = jdaBuilder.build();

            JdaLavalink lavalink = new JdaLavalink(jda.asBot().getApplicationInfo().submit().get(30L, TimeUnit.SECONDS).getId(), 1, integer -> null);

            jda.shutdown();
            rebuildJDA = true;

            jdaBuilder = jdaBuilder.addEventListener(lavalink);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error(e.getLocalizedMessage());
        }

        jdaBuilder.addEventListener(new ReadyListener());
//        jdaBuilder.addEventListener(new GuildVoiceListener());
        jdaBuilder.addEventListener(new GameListener());
        jdaBuilder.addEventListener(new GuildListener());
        jdaBuilder.addEventListener(new GuildMemberListener());
        jdaBuilder.addEventListener(new MessageListener());
        jdaBuilder.addEventListener(new UserListener());
        jdaBuilder.addEventListener(waiter);
        jdaBuilder.addEventListener(client.build());

        if (rebuildJDA) {
            jda = jdaBuilder.build();
            jda.awaitReady();

            LOG.info("SmileBot ready and deplorable.");
        }
    }

    private void setupDatabase() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:smilebot.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);

            LOG.trace("SQLite database started in " + connection.getMetaData().getURL() + " with a " + statement.getQueryTimeout() + "ms timeout.");
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }/* finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }

            LOG.info("SQLite connection closed.");
        }*/
    }

    public static Statement getStatement() {
        return statement;
    }
}
