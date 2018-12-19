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

public enum BanReason {

    SPAM_BOT_USERNAME("Spam Bot Discord Username"),
    SPAM_BOT_NICKNAME("Spam Bot Server Nickname"),
    SPAM_BOT_GAME("Spam Bot Game"),
    SPAM_BOT_MESSAGE("Spam Bot Message");

    private final String text;

    BanReason(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static BanReason fromString(String text) {
        for (BanReason banReason : BanReason.values()) {
            if (banReason.text.equalsIgnoreCase(text)) {
                return banReason;
            }
        }

        return null;
    }
}
