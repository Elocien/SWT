package tourable.travelguide;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import tourable.city.City;

/**
 * value class that represents a travel guide
 * @author pascal
 *
 */
@Entity
public class Travelguide {

	private @Id @GeneratedValue long id;
	private String name;

	@ManyToOne
	private City city;
	private double price;

	@SuppressWarnings("unused")
	private Travelguide() {

	}

	public Travelguide(String name, City city, double price) {
		this.name = name;
		this.city = city;
		this.price = price;
	}

	public long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public City getCity() {
		return this.city;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getPrice() {
		return this.price;
	}

}
