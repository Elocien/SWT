package tourable.travelguide;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class TravelguideDataForm {

	@NotEmpty
	private final String name;

	@NotEmpty
	private final String city;

	@NotNull
	private final Double price;

	public TravelguideDataForm(String name, String city, Double price) {
		this.name = name;
		this.city = city;
		this.price = price;
	}

	public String getName() {
		return this.name;
	}

	public String getCity() {
		return this.city;
	}

	public Double getPrice() {
		return this.price;
	}

}
