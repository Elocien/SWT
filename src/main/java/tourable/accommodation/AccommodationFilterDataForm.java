package tourable.accommodation;

public class AccommodationFilterDataForm {

	private String city;

	private AccommodationType type;

	private AccommodationLocation location;

	public AccommodationFilterDataForm(String city, AccommodationType type, AccommodationLocation location) {
		this.city = city;
		this.type = type;
		this.location = location;
	}

	public String getCity() {
		return this.city;
	}

	public AccommodationType getType() {
		return this.type;
	}

	public AccommodationLocation getLocation() {
		return this.location;
	}

}