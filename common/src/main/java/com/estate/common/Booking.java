package com.estate.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Booking {

	private Integer id;
	private Integer propertyId;
	private Long time;

	public Booking(){};
	
	public Booking(Integer id, Integer propertyId, Date time){
		this.id = id;
		this.propertyId = propertyId;
		if(time != null) {
			this.time = time.getTime();
		} else {
			this.time=null;
		}
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	public Integer getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(Integer propertyId) {
		this.propertyId = propertyId;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Date time){
		if (time != null) this.time = time.getTime();
	}

}
