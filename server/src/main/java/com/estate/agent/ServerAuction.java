package com.estate.agent;

import java.util.ArrayList;
import java.util.HashMap;


import com.estate.common.Auction;
import com.estate.common.BidAlert;
import com.estate.common.Bidder;
import com.estate.common.Property;
import com.google.gson.Gson;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import static java.util.Collections.max;


public class ServerAuction extends Auction {

	private static transient AuctionManager auctionManager = AuctionManager.getInstance();
	private transient Property property;
	private transient ArrayList<AsyncResponse> asyncClients;
	private transient Thread monitorThread;
	private static Gson gson = new Gson();

	public ServerAuction(Auction a) {
		setPropertyId(a.getPropertyId());
		this.property = auctionManager.getPropertyById(getPropertyId());
		asyncClients = new ArrayList<>();
		setBidders(new HashMap<>());
		setTopBid(property.getReservePrice());
		startMonitorThread();
	}

	private void startMonitorThread(){
		final ServerAuction _this = this;

		Runnable task = new Runnable() {
			@Override
			public void run() {
				//Set duration if it's not already set.
				if (_this.getDuration() == null) {
					_this.setDuration(60000 + System.currentTimeMillis());
				};

				//wait until duration finished
				while(_this.getDuration() > System.currentTimeMillis()) {
					try {
						Thread.sleep(500L);
					} catch (InterruptedException e) {
						System.out.println("Auction " + _this.getId() + " interrupted early.");
						_this.close();
						return;
					}
					//alert every 10ms
					_this.sendAlerts();
				}
				//finish out the auction
				_this.close();
			}
		};

		monitorThread = new Thread(task);
		monitorThread.start();
	}

	synchronized Bidder placeBid(Bidder bid) {
		if(!isAuctionOpen()) {
            return null;
		}
		bid = update_bidders(bid);
		if((getTopBid()==null) || (bid!=null && bid.getBidAmount() > getTopBid())) {
			setTopBidder(bid);
			sendAlerts();
		}
		return bid;
	}

    protected synchronized void close() {
        if(getTopBidder() != null) {
            auctionManager.getPropertyById(getPropertyId()).buy();
        }
        sendResults();
    }

    private void sendResults() {
	    sendAlerts();
    }

    private synchronized Bidder update_bidders(Bidder bid) {
		Long id = bid.getId();
		if(id==null) {
			if (getBidders().size() == 0) {
				id = 0l;
			} else {
				id = max(getBidders().keySet()) + 1;
			}
			bid.setId(id);
		}

		getBidders().put(id, bid);
		return bid;
	}

	private synchronized void sendAlerts() {
		String a = gson.toJson(this.getTopBidder());
		AsyncResponse[] asyncClients = this.asyncClients.toArray(new AsyncResponse[this.asyncClients.size()]);

		final String bidAlert = gson.toJson(new BidAlert(!isAuctionOpen(), getTopBidder()));
		for(AsyncResponse asyncClient:	asyncClients){
			asyncClient.resume(Response.status(Response.Status.OK).entity(bidAlert).build());
		}

		this.asyncClients.clear();
		
	}

	public boolean isAuctionOpen() {
		return getDuration() > System.currentTimeMillis();
	}

	synchronized public void addAsyncClient(AsyncResponse response){
		if(asyncClients==null) {
			asyncClients = new ArrayList<>();
		}
		asyncClients.add(response);
	}

	synchronized public void removeAsyncClient(AsyncResponse response){
		if(asyncClients!=null) {
			asyncClients.remove(response);
		}
	}


}
