package tourable.accommodation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import tourable.city.City;

/**
 * Represents an accommodation that can be booked
 * 
 * @author Lucian McIntyre
 * @author Pascal Juppe
 */
@Entity
public class Accommodation {

	private @Id @GeneratedValue long id;
	private String name;

	@ManyToOne
	private City city;

	private String description;

	@Lob
	private byte[] image;

	private double price;
	private int bedNumber;
	private int roomNumber;
	private double provision;
	private AccommodationLocation location;
	private AccommodationType type;

	private boolean disabled = false;

	@SuppressWarnings("unused")
	private Accommodation() {
	}

	/**
	 * Instance of Accommodation
	 * 
	 * @param name        the name
	 * @param city        the city where the accommodation is located
	 * @param description a description
	 * @param image       a descriptive image
	 * @param price       the price per night
	 * @param bedNumber   the number ofbeds
	 * @param roomNumber  the number of rooms
	 * @param provision   provision for the accommodation manager
	 * @param type        type of accommodation
	 * @param location    location of the accommodation within the city
	 */
	public Accommodation(String name, City city, String description, byte[] image, double price, int bedNumber,
			int roomNumber, double provision, AccommodationType type, AccommodationLocation location) {
		this.name = name;
		this.city = city;
		this.description = description;
		this.image = image;
		this.price = price;
		this.bedNumber = bedNumber;
		this.roomNumber = roomNumber;
		this.provision = provision;
		this.type = type;
		this.location = location;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public City getCity() {
		return this.city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String descrition) {
		this.description = descrition;
	}

	public byte[] getImage() {
		return this.image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public double getPrice() {
		return this.price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getBedNumber() {
		return this.bedNumber;
	}

	public void setBedNumber(int bedNumber) {
		this.bedNumber = bedNumber;
	}

	public int getRoomNumber() {
		return this.roomNumber;
	}

	public void setRoomNumber(int roomNumber) {
		this.roomNumber = roomNumber;
	}

	public double getProvision() {
		return this.provision;
	}

	public void setProvision(double provision) {
		this.provision = provision;
	}

	public AccommodationLocation getLocation() {
		return this.location;
	}

	public void setLocation(AccommodationLocation location) {
		this.location = location;
	}

	public AccommodationType getType() {
		return this.type;
	}

	public void setType(AccommodationType type) {
		this.type = type;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return this.disabled;
	}

}
