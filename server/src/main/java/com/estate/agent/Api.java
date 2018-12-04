package com.estate.agent;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.estate.common.Auction;
import com.estate.common.BidAlert;
import com.estate.common.Bidder;
import com.estate.common.Booking;
import com.estate.common.Property;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/")
public class Api {
	final static AuctionManager auctionManager = AuctionManager.getInstance();
	final Gson gson = new GsonBuilder().create();

	@Path("property")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getProperties() {
	    List<Property> results = auctionManager.getProperties();
        return gson.toJson(results);
    }

    @Path("property/{propId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPropertyById(@PathParam("propId") int propertyId) {
        Property result = auctionManager.getPropertyById(propertyId);
        return Response.ok().entity(gson.toJson(result)).build();
    }
    
	@Path("property")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProperty(String json) {
	    Property property = gson.fromJson(json, Property.class);
    	property = auctionManager.addProperty(property);
    	return Response.ok().entity(gson.toJson(property)).build();
    }

    @Path("auction")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAuctions() {
        Auction[] results = auctionManager.getAuctions();
        return gson.toJson(results);
    }

    @Path("auction")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAuction(String json) {
	    Auction auction = gson.fromJson(json, Auction.class);
	    ServerAuction serverAuction = new ServerAuction(auction);
        serverAuction = auctionManager.addAuction(serverAuction);
        return Response.ok().entity(gson.toJson(serverAuction)).build();
    }

    @Path("makeBooking")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makeBooking(String json) {
	    final Booking tmp = gson.fromJson(json, Booking.class);
        Property p = auctionManager.getPropertyById(tmp.getPropertyId());
        if(p.book(new Date(tmp.getTime()))) {
            return Response.ok().entity(gson.toJson(p.getBookings())).build();
        } else {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }
    
    @Path("bid/{auctionId}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response placeBid(@PathParam("auctionId") int auctionId, String json) {
    	ServerAuction auction = auctionManager.getAuctionById(auctionId);
    	Bidder bid = gson.fromJson(json, Bidder.class);

    	if(auction != null) {
    	    bid = auction.placeBid(bid);
    	    return Response.ok().entity(gson.toJson(bid)).build();
        } else {
    	    return Response.status(Response.Status.NOT_FOUND).build();
        }

    }

    @Path("/long_poll_auction/{auctionId}")
    @GET
    public void longPoll(@PathParam("auctionId") int auctionId, @Suspended AsyncResponse asyncResponse){
	    //grab the auction, immediately respond on failure
        AuctionManager auctionManager = AuctionManager.getInstance();
        ServerAuction auction = auctionManager.getAuctionById(auctionId);

        if(auction==null) {
            asyncResponse.resume(Response.status(Response.Status.NOT_FOUND).build());
            asyncResponse.isDone();
            return;
        }

        //set a long timeout
        asyncResponse.setTimeout(60, TimeUnit.SECONDS);

        if(auction.isAuctionOpen()) {
            //Add the asyncResponse to a list in the auction Object for later use
            auction.addAsyncClient(asyncResponse);

            //remove the dead response from the list if it times out
            asyncResponse.setTimeoutHandler(auction::removeAsyncClient);
        } else {
            asyncResponse.resume(
                    Response.status(Response.Status.OK).entity(gson.toJson(new BidAlert(!auction.isAuctionOpen(), auction.getTopBidder()))).build());
            asyncResponse.isDone();
        }
    }
}
