package com.olziedev.spotbot.server.punishment;

public enum PunishmentType {

    BAN() {
        @Override
        public String getName() {
            return "Ban";
        }

        @Override
        public String getOtherName() {
            return "Banned";
        }
    },
    MUTE() {
        @Override
        public String getName() {
            return "Mute";
        }

        @Override
        public String getOtherName() {
            return "Muted";
        }
    },
    KICK() {
        @Override
        public String getName() {
            return "Kick";
        }

        @Override
        public String getOtherName() {
            return "Kicked";
        }
    },
    WARN() {
        @Override
        public String getName() {
            return "Warn";
        }

        @Override
        public String getOtherName() {
            return "Warned";
        }
    };

    public abstract String getName();

    public abstract String getOtherName();
}
