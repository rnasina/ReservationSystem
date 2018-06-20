package com.walmart.entities;

import java.time.LocalDateTime;

/**
 * Created by rnasina on 6/15/18.
 */
public class Seat {
    private int seatNumber;
    private boolean reserved;
    private String reservedEmail;
    private boolean onHold;
    private LocalDateTime holdExpirationTime;
    private String id;
    private String currentSeatHoldId;
    private String currentSeatHoldEmail;
    private LocalDateTime lastTimeReleased;

    private Seat(Builder builder) {
        this.id = builder.id;
        this.reserved = builder.reserved;
        this.onHold = builder.onHold;
        this.seatNumber = builder.seatNumber;
        this.reservedEmail = builder.reservedEmail;
        this.currentSeatHoldId = builder.currentSeatHoldId;
        this.currentSeatHoldEmail = builder.currentSeatHoldEmail;
        this.lastTimeReleased = builder.lastTimeReleased;
        this.holdExpirationTime = builder.holdExpirationTime;
    }

    public static class Builder {
        private int seatNumber;
        private boolean reserved;
        private String reservedEmail;
        private boolean onHold;
        private LocalDateTime holdExpirationTime;
        private String id;
        private String currentSeatHoldId;
        private String currentSeatHoldEmail;
        private LocalDateTime lastTimeReleased;

        public Builder(String id, int seatNumber) {
            this.id = id;
            this.seatNumber = seatNumber;
        }

        public Builder reserve(boolean reserve) {
            this.reserved = reserve;
            return this;
        }

        public Builder reserveEmail(String email) {
            this.reservedEmail = email;
            return this;
        }


        public Builder onHold(boolean onHold) {
            this.onHold = onHold;
            return this;
        }

        public Builder holdExpirationTime(LocalDateTime holdExpirationTime) {
            this.holdExpirationTime = holdExpirationTime;
            return this;
        }

        public Builder currentSeatHoldId(String currentSeatHoldId) {
            this.currentSeatHoldId = currentSeatHoldId;
            return this;
        }

        public Builder currentSeatHoldEmail(String currentSeatHoldEmail) {
            this.currentSeatHoldEmail = currentSeatHoldEmail;
            return this;
        }

        public Builder lastTimeReleased(LocalDateTime lastTimeReleased) {
            this.lastTimeReleased = lastTimeReleased;
            return this;
        }

        public Seat build() {
            return new Seat(this);
        }

    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public boolean isReserved() {
        return reserved;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Seat) obj).getId().equalsIgnoreCase(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public LocalDateTime getHoldExpirationTime() {
        return holdExpirationTime;
    }

    public boolean isOnHold() {
        return onHold;
    }

    public String getCurrentSeatHoldId() {
        return currentSeatHoldId;
    }

    public String getCurrentSeatHoldEmail() {
        return currentSeatHoldEmail;
    }

    public String getReservedEmail() {
        return reservedEmail;
    }

    @Override
    public String toString() {
        return String.format("Seat id %s, seat number: %s, hold: %s, reserved: %s, "
                + "expiration time: %s, current seat hold id: %s", getId(), getSeatNumber(), isOnHold(),
            isReserved(), getHoldExpirationTime(), getCurrentSeatHoldId());
    }

    public LocalDateTime getLastTimeReleased() {
        return lastTimeReleased;
    }
}
