package tourable.booking;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;

import tourable.accommodation.Accommodation;
import tourable.accounting.Invoice;
import tourable.customer.Customer;

/**
 * Represents a booking of an accommodation for a customer
 * @author Pascal Juppe
 */
@Entity
public class Booking {

	private @Id @GeneratedValue long id;

	private LocalDateTime timeOfBooking;

	private LocalDateTime start;
	private LocalDateTime end;

	private double price;
	private PaymentMethod paymentMethod;
	private BookingStatus status;

	@ManyToOne
	private Customer customer;

	@ManyToOne
	private Accommodation accommodation;
	
	@OneToMany
	private List<Invoice> invoicesOfBooking;

	@SuppressWarnings("unused")
	private Booking() {

	}

	/**
	 * Represents a booking of an accommodation for a customer
	 * @param startTime the travel start time
	 * @param endTime the travel end time
	 * @param price the total trip price
	 * @param paymentMethod the used method of payment
	 * @param bookingStatus the current booking status
	 * @param customer the customer that booked the trip
	 * @param accommodation the booked accommodation
	 * @param timeOfBooking the booking timestamp as {@linkplain BusinessTime}}
	 */
	public Booking(LocalDateTime startTime, LocalDateTime endTime, double price, PaymentMethod paymentMethod,
			BookingStatus bookingStatus, Customer customer, Accommodation accommodation, BusinessTime timeOfBooking) {
		this.start = startTime;
		this.end = endTime;
		this.price = price;
		this.paymentMethod = paymentMethod;
		this.status = bookingStatus;
		this.customer = customer;
		this.accommodation = accommodation;

		this.timeOfBooking = timeOfBooking.getTime();
		this.invoicesOfBooking = new ArrayList<Invoice>();
	}

	public long getId() {
		return this.id;
	}

	public void setStatus(BookingStatus status) {
		this.status = status;
	}

	public BookingStatus getStatus() {
		return this.status;
	}

	public double getPrice() {
		return this.price;
	}

	public Interval getInterval() {
		return Interval.from(start).to(end);
	}

	public LocalDateTime getStart() {
		return this.start;
	}

	public LocalDateTime getEnd() {
		return this.end;
	}

	public PaymentMethod getPaymentMethod() {
		return this.paymentMethod;
	}

	public Accommodation getAccommodation() {
		return this.accommodation;
	}

	public Customer getCustomer() {
		return this.customer;
	}

	public LocalDateTime getTimeOfBooking() {
		return this.timeOfBooking;
	}
	
	public List<Invoice> getInvoicesOfBooking(){
		return this.invoicesOfBooking;
	}
	
	public void addInvoiceToBooking(Invoice invoice) {
		this.invoicesOfBooking.add(invoice);
	}
}
