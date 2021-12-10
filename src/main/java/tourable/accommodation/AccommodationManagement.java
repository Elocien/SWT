package tourable.accommodation;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import tourable.booking.BookingManagement;
import tourable.city.City;
import tourable.city.CityManagement;

/**
 * {@linkplain Service} class for {@code Accommodation}s which handles all
 * business logic.
 * 
 * @author Lucian McIntyre
 * @author Pascal Juppe
 */

@Service
@Transactional
public class AccommodationManagement {

	private final AccommodationRepository accommodations;
	private final BookingManagement bookingManagement;
	private final CityManagement cityManagement;
	private final BusinessTime businessTime;

	public AccommodationManagement(AccommodationRepository accommodations, BookingManagement bookingManagement,
			CityManagement cityManagement, BusinessTime businessTime) {
		this.accommodations = Objects.requireNonNull(accommodations);
		this.bookingManagement = Objects.requireNonNull(bookingManagement);
		this.cityManagement = Objects.requireNonNull(cityManagement);
		this.businessTime = Objects.requireNonNull(businessTime);
	}

	/**
	 * adds an {@linkplain Accommodation} to the database
	 * 
	 * @param acm the accommodation to add
	 */
	public void add(Accommodation acm) {
		accommodations.save(acm);
	}

	/**
	 * creates a new {@linkplain Accommodation} with values from a given
	 * {@linkplain AccommodationDataForm}
	 * 
	 * @param form the data form
	 * @return the created accommodation
	 */
	public Accommodation createAccommodation(AccommodationDataForm form) {

		Objects.requireNonNull(form);

		var name = form.getName();
		var city = cityManagement.findByName(form.getCity()).get();
		var description = form.getDescription();
		byte[] image = null;
		try {
			image = form.getImage().getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		var price = form.getPrice();
		var bedNumber = form.getBedNumber();
		var roomNumber = form.getRoomNumber();
		var provision = form.getProvision();
		var type = form.getType();
		var location = form.getLocation();

		return accommodations.save(new Accommodation(name, city, description, image, price, bedNumber, roomNumber,
				provision, type, location));
	}

	/**
	 * disables an {@linkplain Accommodation} with a given id, but only if there are
	 * no open bookings for the accommodation
	 * 
	 * @param id the accommodation id
	 */
	public void deleteAccommodation(Long id) {
		Optional<Accommodation> optAccommodation = findById(id);
		if (optAccommodation.isPresent()) {
			Accommodation acm = optAccommodation.get();
			if (!bookingManagement.hasOpenBookingsForAccommodation(acm)) {
				acm.setDisabled(true);
				accommodations.save(acm);
			}
		}
	}

	/**
	 * updates an {@linkplain Accommodation} with values from a given
	 * {@linkplain AccommodationDataForm}
	 * 
	 * @param id   the id of the accommodation to update
	 * @param form the form that contains the new information
	 */
	public void updateAccommodation(Long id, AccommodationDataForm form) {
		Optional<Accommodation> acm = findById(id);
		if (acm.isPresent()) {
			Accommodation accommodation = acm.get();
			accommodation.setName(form.getName());
			accommodation.setCity(cityManagement.findByName(form.getCity()).get());
			accommodation.setDescription(form.getDescription());
			try {
				if (!form.getImage().isEmpty()) {
					accommodation.setImage(form.getImage().getBytes());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			accommodation.setPrice(form.getPrice());
			accommodation.setBedNumber(form.getBedNumber());
			accommodation.setRoomNumber(form.getRoomNumber());
			accommodation.setProvision(form.getProvision());
			accommodation.setLocation(form.getLocation());
			accommodation.setType(form.getType());

			accommodations.save(accommodation);
		}
	}

	/**
	 * @return all enabled {@linkplain Accommodation}s in the database
	 */
	public Iterable<Accommodation> findAll() {
		return accommodations
				.findByDisabledFalse(Sort.by(Sort.Direction.ASC, "cityName").and(Sort.by(Sort.Direction.ASC, "type")
						.and(Sort.by(Sort.Direction.ASC, "location")).and(Sort.by(Sort.Direction.ASC, "price"))));
	}

	/**
	 * @param id the accommodation id
	 * @return an {@linkplain Optional} of an enabled {@linkplain Accommodation}
	 *         with the given id
	 */
	public Optional<Accommodation> findById(long id) {
		return accommodations.findByIdAndDisabledFalse(id);
	}

	/**
	 * @param name the given name
	 * @return all enabled {@linkplain Accommodation}s with a given name
	 */
	public Optional<Accommodation> findByName(String name) {
		return accommodations.findByNameAndDisabledFalse(name);
	}

	/**
	 * @param city the given city
	 * @return whether there are enabled {@linkplain Accommodation}s in the given
	 *         city
	 */
	public boolean hasAccommodationsForCity(City city) {
		return accommodations.stream().anyMatch(acm -> acm.getCity().equals(city));
	}

	/**
	 * @param cityString the city name
	 * @param location   the accommodation location
	 * @param type       the accommodation type
	 * @return list of accommodations filtered by the given criteria
	 */
	public Iterable<Accommodation> findByFilter(String cityString, AccommodationLocation location,
			AccommodationType type) {

		City city = null;

		var accommodationList = findAll();

		Optional<City> optCity = cityManagement.findByName(cityString);
		city = optCity.isPresent() ? optCity.get() : null;

		accommodationList = accommodations.findByCityTypeAndLocation(city, type, location);
		return accommodationList;
	}

	/**
	 * Returns all enabled {@linkplain Accommodation}s that meet the given criteria
	 * 
	 * @param city      the city of the accommodation
	 * @param types     the types of the accommodations
	 * @param locations the locations of the accommodations
	 * @param startDate the start date of the trip
	 * @param endDate   the end date of the trip
	 * @param offset    an optional offset the the start and end dates
	 * @return the map of {@link AccommodationEntry } objects that fit the given
	 *         criteria, with base64 image as value
	 */
	public List<AccommodationEntry> findSuitableAccommodations(City city, List<AccommodationType> types,
			List<AccommodationLocation> locations, String startDate, String endDate, long offset) {

		LocalDate start = LocalDate.parse(startDate).plusDays(offset);
		LocalDate end = LocalDate.parse(endDate).plusDays(offset);
		Interval interval = Interval.from(start.atTime(12, 0, 1)).to(end.atTime(11, 59, 59));
		var days = ChronoUnit.DAYS.between(start, end);

		if (businessTime.getTime().isAfter(start.atStartOfDay())) {
			return new ArrayList<AccommodationEntry>();
		}

		return accommodations.stream().filter(acm -> types.contains(acm.getType()))
				.filter(acm -> locations.contains(acm.getLocation())).filter(acm -> acm.getCity().equals(city))
				.filter(acm -> bookingManagement.getBookingIntervalsForAccommodation(acm)
						.noneMatch(i -> i.overlaps(interval)))
				.map(acm -> new AccommodationEntry(acm.getId(), acm.getName(), acm.getCity(), acm.getType(),
						acm.getLocation(), acm.getRoomNumber(), acm.getBedNumber(), acm.getPrice() * days,
						Base64.getEncoder().encodeToString(acm.getImage()), start, end))
				.collect(Collectors.toList());
	}

}