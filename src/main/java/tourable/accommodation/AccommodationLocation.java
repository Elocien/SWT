package tourable.accommodation;

import java.util.Optional;

public enum AccommodationLocation {
	CITY_CENTRE, SUBURBAN, COUNTRYSIDE;

	public Optional<AccommodationLocation> prev() {
		var location = Optional.<AccommodationLocation>empty();
		switch (this) {
		case SUBURBAN:
			location = Optional.of(CITY_CENTRE);
			break;
		case COUNTRYSIDE:
			location = Optional.of(SUBURBAN);
			break;
		default:
			location = Optional.empty();
			break;
		}

		return location;
	}

	public Optional<AccommodationLocation> next() {
		var location = Optional.<AccommodationLocation>empty();
		switch (this) {
		case CITY_CENTRE:
			location = Optional.of(SUBURBAN);
			break;
		case SUBURBAN:
			location = Optional.of(COUNTRYSIDE);
			break;
		default:
			location = Optional.empty();
			break;
		}

		return location;
	}

}