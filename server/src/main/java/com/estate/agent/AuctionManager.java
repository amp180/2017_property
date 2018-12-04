package com.estate.agent;

import static java.util.Collections.max;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.estate.common.Property;

public class AuctionManager {
    private static AuctionManager res;
    private static HashMap<Integer, ServerAuction> auctions = new HashMap<>();
    private static HashMap<Integer, Property> properties = new HashMap<>();

    public static synchronized AuctionManager getInstance() {
        if(res == null) {
            res = new AuctionManager();
        }
        return res;
    }

    private AuctionManager() {
    }

    public ServerAuction getAuctionById(int id) {
        if(auctions.containsKey(id)) {
            return auctions.get(id);
        }
        return null;
    }

    public ServerAuction[] getAuctions(){
        final Collection<ServerAuction> auctionsCollection = auctions.values();
        return auctionsCollection.toArray(new ServerAuction[auctionsCollection.size()]);
    }

    public synchronized ServerAuction addAuction(ServerAuction a){
        if(properties.keySet().size() == 0) {
            a.setId(0);
        } else {
            a.setId(max(properties.keySet())+1);
        }
        auctions.put(a.getId(), a);
        return a;
    }

    public Property getPropertyById(int id) {
        if(properties.containsKey(id)) {
            return properties.get(id);
        }
        return null;
    }

    public List<Property> getProperties(){
        return new ArrayList<Property>(properties.values());
    }

    public synchronized Property addProperty(Property p){
        if(properties.keySet().size() == 0) {
            p.setId(0);
        } else {
            p.setId(max(properties.keySet())+1);
        }
        properties.put(p.getId(), p);
        return p;
    }
}
