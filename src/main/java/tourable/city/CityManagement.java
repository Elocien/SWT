package tourable.city;

import java.util.Objects;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tourable.accommodation.AccommodationManagement;
import tourable.travelguide.TravelguideManagement;

/**
 * {@linkplain Service} class that handles all business logic for city
 * components
 * 
 * @author Pascal Juppe
 */
@Service
@Transactional
public class CityManagement {

	private final CityRepository cities;

	private final AccommodationManagement accommodationManagement;
	private final TravelguideManagement travelguideManagement;

	public CityManagement(CityRepository cities, @Lazy AccommodationManagement accommodationManagement,
			@Lazy TravelguideManagement travelguideManagement) {
		this.cities = Objects.requireNonNull(cities);
		this.accommodationManagement = Objects.requireNonNull(accommodationManagement);
		this.travelguideManagement = Objects.requireNonNull(travelguideManagement);
	}

	/**
	 * create a new {@linkplain City} with values from a given
	 * {@linkplain CityDataForm}
	 * 
	 * @param form the given form
	 * @return the new city
	 */
	public City createCity(CityDataForm form) {

		Objects.requireNonNull(form);
		var name = Objects.requireNonNull(form.getName());

		return cities.save(new City(name));
	}

	/**
	 * Updates a {@linkplain City} with values from a given
	 * {@linkplain CityDataForm}
	 * 
	 * @param id   the id of the city to update
	 * @param form the form which values are used
	 */
	public void updateCity(long id, CityDataForm form) {

		Optional<City> opt = cities.findById(id);
		if (opt.isPresent()) {
			City city = opt.get();
			city.setName(form.getName());

			cities.save(city);
		}
	}

	/**
	 * Disables a {@linkplain City}, but only if it is not used in any
	 * accommodations or travel guides
	 * 
	 * @param id the id of the city to delete
	 */
	public void deleteCity(long id) {
		Optional<City> optCity = findById(id);
		if (optCity.isPresent()) {
			City city = optCity.get();
			if (!accommodationManagement.hasAccommodationsForCity(city)
					&& !travelguideManagement.hasTravelguidesForCity(city)) {
				city.setDisabled(true);
				cities.save(city);
			}
		}
	}

	/**
	 * @return all enabled {@linkplain City} objects in the database
	 */
	public Iterable<City> findAll() {
		return cities.findByDisabledFalse(Sort.by(Direction.ASC, "name"));
	}

	/**
	 * @param id the city id
	 * @return an {@linkplain Optional} of an enabled {@linkplain City} with the
	 *         given id
	 */
	public Optional<City> findById(long id) {
		return cities.findByIdAndDisabledFalse(id);
	}

	/**
	 * 
	 * @param name the city name
	 * @return an {@linkplain Optional} of an enabled {@linkplain City} with the
	 *         given name
	 */
	public Optional<City> findByName(String name) {
		return cities.findByNameAndDisabledFalse(name);
	}

}
