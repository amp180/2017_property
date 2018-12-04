package com.estate.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Property {
	Integer id;
	String propertyName;
	String propertyLocation;
	Long reservePrice;
	HashMap<Long, Boolean> bookings;
    Boolean sold;
	
	public Property(){};

	public Property(Integer id, String propertyName, String propertyLocation) {
		this.id = id;
	    bookings = new HashMap<>();
		this.propertyName = propertyName;
		this.propertyLocation = propertyLocation;
		sold = false;
	}

	public void setId(Integer id) {
	    this.id = id;
    }
	
	public Integer getId() {
		return id;
	}

    public boolean book(Date time) {
        if(bookings.containsKey(time.getTime()) && bookings.get(time.getTime())) {
           return false;
        }
        bookings.put(time.getTime(), true);
        return true;
    }

	public HashMap<Long, Boolean> getBookings() { return bookings; }

	public String getPropertyName() {
    	return propertyName;
    }
    
    public String getPropertyLocation() {
    	return propertyLocation;
    }

	public Long getReservePrice() {
		return reservePrice;
	}

	public void setReservePrice(Long price) {
		this.reservePrice = price;
	}

	public boolean isForSale() {
	    return !sold;
    }

    public void buy() {
	    sold = true;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setPropertyLocation(String propertyLocation) {
        this.propertyLocation = propertyLocation;
    }

    public void setSold(Boolean sold) {
        this.sold = sold;
    }

}