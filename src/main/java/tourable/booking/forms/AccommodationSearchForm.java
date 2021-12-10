package tourable.booking.forms;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import tourable.accommodation.AccommodationLocation;
import tourable.accommodation.AccommodationType;

public class AccommodationSearchForm {

	@NotEmpty
	private String city;

	@NotNull
	private AccommodationType type;

	@NotNull
	private AccommodationLocation location;

	@NotEmpty
	private String dateRange;

	public AccommodationSearchForm(String city, AccommodationType type, AccommodationLocation location,
			String dateRange) {
		this.city = city;
		this.type = type;
		this.location = location;
		this.dateRange = dateRange;
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

	public String getDateRange() {
		return this.dateRange;
	}
}
