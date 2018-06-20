package com.walmart.services;

import java.util.List;
import java.util.Optional;

import com.walmart.entities.SeatHold;

/**
 * Created by rnasina on 6/15/18.
 */
public interface TicketService {


    long numSeatsAvailable();
  
    SeatHold findAndHoldSeats(int numSeats, String customerEmail);

    String reserveSeats(String seatHoldId, String customerEmail);
}
