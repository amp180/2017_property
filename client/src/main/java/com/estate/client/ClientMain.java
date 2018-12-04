package com.estate.client;

import com.estate.common.*;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.Float.max;

public class ClientMain {

    public static void main(final String[] args){
        Client client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 60000);
        client.property(ClientProperties.READ_TIMEOUT,    60000);
        WebTarget restApi;

        if(args.length>0) {
            restApi = client.target(args[0]);
        } else {
            restApi = client.target("http://localhost:8080/api");
        }

        Property firstProperty = new Property(null, "firstProperty", "d4");
        Property secondProperty = new Property(null, "otherProperty", "d8");

        firstProperty = insertProperty(restApi, firstProperty);

        assert(bookProperty(restApi, firstProperty).length>0);
        System.out.println("Booking property "+firstProperty.getPropertyName()+" successful");

        Auction secondPropertyAuction = insertPropertyAndStartAuction(restApi, secondProperty);

        ArrayList<Thread> bidThreads = new ArrayList<>();
        for(int i=0; i<5; i++) {
            final long maxBid = Math.round(10000*Math.random());
            bidThreads.add(startBidThread(restApi, secondPropertyAuction, maxBid));
        }

        for(Thread t : bidThreads){
            try {
                t.join();
            } catch (InterruptedException e){
                e.printStackTrace();
                break;
            }
        }

    }


    public static Property insertProperty(final WebTarget api, Property property){
        System.out.println("Adding property "+property.getPropertyName());
        return api.path("/property")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(property), Property.class);
    }

    public static Booking[] bookProperty(final WebTarget api, final Property p){
        final Booking b = new Booking(null, p.getId(), Date.from(Instant.now()));
        return api.path("/makeBooking")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(b), Booking[].class);
    }


    public static Auction insertPropertyAndStartAuction(final WebTarget api, Property property){
        Property postedProperty = insertProperty(api, property);
        postedProperty.setReservePrice(1000L);
        System.out.println("Starting Auction for "+postedProperty.getPropertyName());
        Auction a = new Auction();
        a.setPropertyId(postedProperty.getId());
        a.setDuration(System.currentTimeMillis()+1000);

        return api.path("/auction")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(a), Auction.class );
    }


    public static Thread startBidThread(final WebTarget api, final Auction auction, final Long maxBid){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread "+Thread.currentThread().getName()+" started with max bid "+maxBid);
                Long currentBidAmount = auction.getTopBid();

                if(currentBidAmount==null){
                    Integer propertyId = auction.getPropertyId();

                    if(propertyId!=null) {
                        Property p = api.path("property/"+propertyId)
                                .request(MediaType.APPLICATION_JSON)
                                .get(Property.class);
                        currentBidAmount = p.getReservePrice();
                    }

                    if(currentBidAmount==null){
                        currentBidAmount = 1l;
                    }
                }

                Long bidId=null;
                Long highestBidderId = null;
                Bidder bid = null;
                Long lastBid = 0l;

                while(true){
                    final Long nextBid = calculateNextBid(currentBidAmount, maxBid);

                    if(needToBid(lastBid,currentBidAmount, highestBidderId, bidId) && nextBid!=null) {
                        bid = new Bidder(bidId, nextBid);
                        bid = api.path("/bid/" + auction.getId())
                                .request(MediaType.APPLICATION_JSON)
                                .post(Entity.json(bid), Bidder.class);
                        lastBid = nextBid;

                        if (bid != null) {
                            bidId = bid.getId();
                            System.out.println("thread " + Thread.currentThread().getName() + " tried to bid " + lastBid + " with id " + bid);
                        }
                    }

                    BidAlert alert = null;
                    while(alert == null) {
                        try {
                            alert = api.path("/long_poll_auction/" + auction.getId())
                                    .request(MediaType.APPLICATION_JSON)
                                    .get(BidAlert.class);
                            alert = alert;
                        } catch (ProcessingException timeout) {
                            //ignore
                        }
                    }

                    if( alert.isAuctionFinished() ){
                        System.out.println("Thread "+Thread.currentThread().getName()+" has been notified that the auction is finished");
                        if(alert.getMaxBidder()!= null) {
                            System.out.println("Thread " + Thread.currentThread().getName() + " highest bid was " + alert.getMaxBidder().toString());
                        }
                        break;
                    }

                    if (alert.getMaxBidder()!=null && alert.getMaxBidder().getBidAmount()!=null ) {
                        System.out.println("Thread "+Thread.currentThread().getName()+" has been notified that max bid is "+alert.getMaxBidder().toString());
                        currentBidAmount = alert.getMaxBidder().getBidAmount();
                        highestBidderId = alert.getMaxBidder().getId();
                    }

                    if(currentBidAmount>maxBid){
                        System.out.println("Thread "+Thread.currentThread().getName()+" hit it's max bid");
                        break;
                    }

                }

            }
        };

        Thread t = new Thread(task);
        t.start();
        return t;
    }


    private static boolean needToBid(Long lastBid, Long currentBidAmount, Long highestBidderId, Long bidId){
        return lastBid<currentBidAmount && (highestBidderId==null || bidId == null || !highestBidderId.equals(bidId));
    }

    private static Long calculateNextBid(long currentPrice, long maxSpend){
        if(currentPrice>maxSpend) return null;
        return (long)(Math.random()*(maxSpend-currentPrice)+currentPrice);
    }
}
