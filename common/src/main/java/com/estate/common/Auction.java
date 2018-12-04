package com.estate.common;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Auction {
    private Integer id;
    private Integer propertyId;
    private Long topBid = null;
    private Bidder topBidder = null;
    private HashMap<Long, Bidder> bidders = null;
    private Long duration = null;

    public Auction(){
        bidders = new HashMap<>();
    }

    public Integer getId() {
        return id;
    }

    public synchronized void setId(Integer id) {
        this.id = id;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public synchronized void setPropertyId(Integer propertyId1) {
        this.propertyId = propertyId1;
    }

    public Long getTopBid() {
        return topBid;
    }

    public synchronized void setTopBid(Long topBid) {
        this.topBid = topBid;
    }

    public Bidder getTopBidder() {
        return topBidder;
    }

    public synchronized void setTopBidder(Bidder topBidder) {
        this.topBidder = topBidder;
    }

    public HashMap<Long, Bidder> getBidders() {
        return bidders;
    }

    public synchronized void setBidders(HashMap<Long, Bidder> bidders) {
        this.bidders = bidders;
    }

    public Long getDuration() {
        return duration;
    }

    public synchronized void setDuration(Long duration) {
        this.duration = duration;
    }

}
