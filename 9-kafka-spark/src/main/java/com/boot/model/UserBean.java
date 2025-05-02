package com.boot.model;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserBean implements Serializable {
	private static final long serialVersionUID = 3775871090088504659L;

	private UUID id;
	@JsonProperty("firstname")
	private String firstname;

	@JsonProperty("lastname")
	private String lastname;

	@JsonProperty("email")
	private String email;

	public UserBean() {
	}

	public UserBean(String firstname, String lastname, String email) {
		this.id = UUID.randomUUID();
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@JsonProperty("firstname")
	public String getfirstname() {
		return firstname;
	}

	@JsonProperty("firstname")
	public void setfirstname(String firstname) {
		this.firstname = firstname;
	}

	@JsonProperty("lastname")
	public String getlastname() {
		return lastname;
	}

	@JsonProperty("lastname")
	public void setlastname(String lastname) {
		this.lastname = lastname;
	}

	@JsonProperty("email")
	public String getemail() {
		return email;
	}

	@JsonProperty("email")
	public void setemail(String email) {
		this.email = email;
	}

	 public String toJson() {
	        return "{" +
	                "\"firstname\": " + firstname +
	                ", \"lastname\": " + lastname +
	                ", \"email\": \"" + email + '\"' +
	                '}';
	    }
	 
}