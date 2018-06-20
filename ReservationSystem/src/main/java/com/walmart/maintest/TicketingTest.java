package com.walmart.maintest;

import com.walmart.entities.Seat;
import com.walmart.entities.SeatHold;
import com.walmart.services.TicketServiceImpl;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by rnasina on 6/15/18.
 */
public class TicketingTest {

    /**
     * 300 seats
     *
     * @param args
     */
	private static final String ALPHA_STRING = "abcdefghijklmnopqrstuvwxyz";
	public static String randomAlpha(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_STRING.length());
			builder.append(ALPHA_STRING.charAt(character));
		}
		return builder.toString();
	}
	
    public static void main(String[] args) {
        int maxSeats = 300; 
        // Initializing the seats in the Venue
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < maxSeats; i++) {
            String id = UUID.randomUUID().toString();
            List<Seat> seatsAvail = seats;
            if (seatsAvail == null) {
                seatsAvail = new ArrayList<>();
                seats = seatsAvail;
            }
            seatsAvail.add(new Seat.Builder(id, i).build());
        }
        
        TicketServiceImpl service = new TicketServiceImpl(seats);
        int numberOfTransactions = 1000;

        ExecutorService startService = Executors.newFixedThreadPool(numberOfTransactions);
        ScheduledExecutorService releazer = Executors.newScheduledThreadPool(5);
        //holds the seat for cancellations or reservations
        releazer.scheduleAtFixedRate(() -> service.releaseHolds(), 1, 10, TimeUnit.SECONDS);
        AtomicInteger seatsTaken = new AtomicInteger(0);
        IntStream.range(0, numberOfTransactions).forEach(i -> startService.execute(() -> {
       
            int seatsToHold = (new Random()).nextInt(20 - 1) + 1;
            while (seatsToHold <= 0) {
                seatsToHold = (new Random()).nextInt(20 - 1) + 1;
            }
            String customerEmail = randomAlpha(5) + "@rsvp.com";
            SeatHold seatHold = service
                .findAndHoldSeats(seatsToHold, customerEmail);
            if (seatHold.hasEnoughSeats()) {
                System.out.println("Holding " + seatHold.getSeatIdsHeld().size()
                    + " seats and for customer email: " + customerEmail + ", expiration time : " + seatHold
                    .getExpirationTime());
                int seconds = (new Random()).nextInt(80);
                System.out.println("Thinking  " + seconds + " seconds whether to reserve or not");
                try {
                    Thread.sleep(seconds * 1000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean shouldIReserve = (new Random()).nextBoolean();
                String filler = shouldIReserve?"was trying":"failed";
                String mainContent = customerEmail+" "+filler+" to reserve "+seatHold.getSeatIdsHeld().size()+" seats";
                String postFix = shouldIReserve?"; Reservation Id:"+seatHold.getId():"";
                System.out.println(mainContent+""+postFix);
                if (shouldIReserve) {
                    String confirmationCd = service.reserveSeats(seatHold.getId(), customerEmail);
                    if (confirmationCd != null) {
                        seatsTaken.set(seatsTaken.addAndGet(seatHold.getSeatIdsHeld().size()));
                        System.out.println("Seats Reserved : " + seatsTaken + ", Confirmation Code : "+ confirmationCd);
                    } else System.out.println("Too late to book seats. Try for the next available show.");
                }
            }
        }));


        try {
        	
            startService.shutdown();
            startService.awaitTermination(6000, TimeUnit.SECONDS);   
            //Once the releazer shut downs - this marks the end of reservation time
            releazer.shutdown();
            releazer.awaitTermination(6000, TimeUnit.SECONDS);            
            
        } catch (InterruptedException e) {
            e.printStackTrace();
            startService.shutdownNow();
            releazer.shutdownNow();
        }
        service.printFinalObservations();
    }
}
