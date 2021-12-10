package tourable.customer;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.salespointframework.time.BusinessTime;

/**
 * value class, that represents a customer
 * @author Leon Augustat
 *
 */

@Entity
public class Customer {

	private @Id @GeneratedValue long id;
	private String name;
	private String address;
	private String email;
	private String phone;

	private LocalDateTime lastBookingDate;

	private boolean disabled;

	@SuppressWarnings("unused")
	private Customer() {
	}

	public Customer(String name, String address, String email, String phone, BusinessTime bT) {
		this.name = name;
		this.address = address;
		this.email = email;
		this.phone = phone;
		this.lastBookingDate = bT.getTime();
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public LocalDateTime getLastBookingDate() {
		return lastBookingDate;
	}

	public void setLastBookingDate(LocalDateTime time) {
		this.lastBookingDate = time;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return this.disabled;
	}
}
