package com.estate.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BidAlert {

    boolean auctionFinished;
    Bidder maxBidder;

    public BidAlert(){};

    public BidAlert(boolean auctionFinished, Bidder maxBidder) {
        this.auctionFinished = auctionFinished;
        this.maxBidder = maxBidder;
    }

    public boolean isAuctionFinished() {
        return auctionFinished;
    }

    public Bidder getMaxBidder() {
        return maxBidder;
    }
}
