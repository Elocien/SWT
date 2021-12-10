package tourable.accommodation;

import java.util.Optional;

public enum AccommodationType {
	SIMPLE, STANDARD, STANDARD_PLUS, LUXURY;

	public Optional<AccommodationType> prev() {
		var type = Optional.<AccommodationType>empty();
		switch (this) {
		case STANDARD:
			type = Optional.of(SIMPLE);
			break;
		case STANDARD_PLUS:
			type = Optional.of(STANDARD);
			break;
		case LUXURY:
			type = Optional.of(STANDARD_PLUS);
			break;
		default:
			type = Optional.empty();
			break;
		}

		return type;
	}

	public Optional<AccommodationType> next() {
		var type = Optional.<AccommodationType>empty();
		switch (this) {
		case SIMPLE:
			type = Optional.of(STANDARD);
			break;
		case STANDARD:
			type = Optional.of(STANDARD_PLUS);
			break;
		case STANDARD_PLUS:
			type = Optional.of(LUXURY);
			break;
		default:
			type = Optional.empty();
			break;
		}

		return type;
	}
}
