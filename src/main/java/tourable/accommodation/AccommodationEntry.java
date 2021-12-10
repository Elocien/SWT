package tourable.accommodation;

import java.time.LocalDate;

import tourable.city.City;

public class AccommodationEntry {

	private long id;
	private String name;
	private City city;
	private AccommodationType type;
	private AccommodationLocation location;
	private int roomNumber;
	private int bedNumber;
	private double totalPrice;
	private String image;
	private LocalDate from;
	private LocalDate to;

	public AccommodationEntry(long id, String name, City city, AccommodationType type, AccommodationLocation location,
			int roomNumber, int bedNumber, double totalPrice, String image, LocalDate from, LocalDate to) {
		this.id = id;
		this.name = name;
		this.city = city;
		this.type = type;
		this.location = location;
		this.roomNumber = roomNumber;
		this.bedNumber = bedNumber;
		this.totalPrice = totalPrice;
		this.image = image;
		this.from = from;
		this.to = to;
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public City getCity() {
		return this.city;
	}

	public AccommodationType getType() {
		return this.type;
	}

	public AccommodationLocation getLocation() {
		return this.location;
	}

	public int getRoomNumber() {
		return this.roomNumber;
	}

	public int getBedNumber() {
		return this.bedNumber;
	}

	public double getTotalPrice() {
		return this.totalPrice;
	}

	public String getImage() {
		return this.image;
	}

	public LocalDate getFrom() {
		return this.from;
	}

	public LocalDate getTo() {
		return this.to;
	}

}
