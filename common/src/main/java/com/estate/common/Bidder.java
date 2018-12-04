package com.estate.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bidder {

	private Long id;
	private Long bidAmount;

	public Bidder(){};

	public Bidder(Long id, long bidAmount) {
		this.id = id;
		this.bidAmount = bidAmount;
	}
	
	public Long getId() {
		return id;
	}

	public Long getBidAmount() {
		return bidAmount;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setBidAmount(Long bidAmount) {
		this.bidAmount = bidAmount;
	}

	@Override
	public String toString() {
		return "Bidder{" +
				"id=" + id +
				", bidAmount=" + bidAmount +
				'}';
	}

}
