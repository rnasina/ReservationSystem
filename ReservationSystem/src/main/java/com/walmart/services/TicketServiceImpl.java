package com.walmart.services;

import com.walmart.entities.Seat;
import com.walmart.entities.SeatHold;
import com.walmart.transaction.Ref;
import com.walmart.transaction.TransactionalMemory;
import com.walmart.transaction.TransactionBlock;
import com.walmart.transaction.TransactionWithResultBlock;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rnasina on 6/15/18.
 */
public class TicketServiceImpl implements TicketService, HoldReleaser {

    private List<Ref<Seat>> seats;

    public TicketServiceImpl(List<Seat> seatsById) {        
        List<Ref<Seat>> seats = seatsById.stream().map(Ref::new).collect(Collectors.toList());
        this.seats = seats;
    }

    @Override
    public long numSeatsAvailable() {
        Long count = TransactionalMemory.transactionWithResult(new TransactionWithResultBlock<>((tx) -> {
            
            return seats.stream().filter(sm -> {
                Seat s = sm.getValue(tx);
                return !s.isReserved() && !s.isOnHold() && s.getHoldExpirationTime() == null;
            }).count();
 
        }));
        return count;
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        if (numSeats <= 0)
            throw new IllegalArgumentException("Number of seats must be > 0");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plus(60, ChronoUnit.SECONDS);
        return TransactionalMemory.transactionWithResult(new TransactionWithResultBlock<>((tx) -> {
            SeatHold hold = new SeatHold();
            final String holdId = UUID.randomUUID().toString();

            Set<Ref<Seat>> seatSet = seats.stream().filter(sm -> {
                Seat s = sm.getValue(tx);
                return s.getCurrentSeatHoldId() == null && !s.isReserved() && !s.isOnHold()
                    && s.getHoldExpirationTime() == null;
            }).sorted((o1, o2) -> {
                return o1.getValue(tx).getSeatNumber() - o2.getValue(tx).getSeatNumber();
            }).limit(numSeats).collect(Collectors.toSet());
            if (seatSet.size() == numSeats) {
                hold.setId(holdId);
                hold.setHoldTime(now);
                hold.setExpirationTime(expiryDate);
                seatSet.stream().forEach(sm -> {
                    Seat s = sm.getValue(tx);
                    Seat ns = new Seat.Builder(s.getId(), s.getSeatNumber()).onHold(true)
                        .holdExpirationTime(expiryDate).currentSeatHoldId(holdId).currentSeatHoldEmail(holdId)
                        .lastTimeReleased(s.getLastTimeReleased()).build();
                    sm.setValue(ns, tx);
                });
                hold.setSeatIdsHeld(seats.stream().filter(sm -> {
                    Seat s = sm.getValue(tx);
                    return holdId.equals(s.getCurrentSeatHoldId());
                }).map(sm -> {
                    Seat s = sm.getValue(tx);
                    return s.getId();
                }).collect(Collectors.toList()));
            } else {
                hold.setHasEnoughSeats(false);
            }
            return hold;
        }));
    }

    @Override
    public String reserveSeats(String seatHoldId, String customerEmail) {
        return TransactionalMemory.transactionWithResult(new TransactionWithResultBlock<>((tx) -> {
            Set<Ref<Seat>> seatSet = seats.stream().filter(sm -> {
                Seat s = sm.getValue(tx);
                return seatHoldId.equals(s.getCurrentSeatHoldId());
            }).collect(Collectors.toSet());

            if (seatSet.size() == 0) {
                // expired already :(
            	// too late to start bookings
                return null;
            }

            seatSet.stream().forEach(sm -> {
                Seat s = sm.getValue(tx);
                Seat ns = new Seat.Builder(s.getId(), s.getSeatNumber()).reserve(true)
                    .reserveEmail(customerEmail).lastTimeReleased(s.getLastTimeReleased()).build();
                sm.setValue(ns, tx);
            });

            return UUID.randomUUID().toString();
        }));
    }


    @Override
    public void releaseHolds() {
        final LocalDateTime now = LocalDateTime.now();
        TransactionalMemory.transaction(new TransactionBlock((tx) -> {
            Set<Ref<Seat>> toRelease = seats.stream().filter(sm -> {
                Seat s = sm.getValue(tx);
                // Checks to see if the expiration time is met or not
                return s.isOnHold() && s.getHoldExpirationTime() != null &&
                    now.isAfter(s.getHoldExpirationTime());
            }).collect(Collectors.toSet());
            System.out.println("Releasing " + toRelease.size() + " seats.");
            toRelease.stream().forEach(sm -> {
                Seat s = sm.getValue(tx);
                Seat ns = new Seat.Builder(s.getId(), s.getSeatNumber()).lastTimeReleased(now).build();
                sm.setValue(ns, tx);
            });
        }));
    }

    public void printFinalObservations() {
    	TransactionalMemory.transaction(new TransactionBlock((tx) -> {
            long reserved = seats.stream().filter((sm) -> {
                Seat s = sm.getValue(tx);
                return s.isReserved();
            }).count();
            long hold = seats.stream().filter((sm) -> {
                Seat s = sm.getValue(tx);
                return s.isOnHold();
            }).count();
            System.out.printf("Final Observation : Reserved: %s, On-Hold: %s \n", reserved, hold);
        }));
    }
}
