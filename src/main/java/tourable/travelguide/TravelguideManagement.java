package tourable.travelguide;

import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tourable.accounting.AccountingManagement;
import tourable.city.City;
import tourable.city.CityManagement;

/**
 * {@linkplain Service} class that handles all business logic for travel guide
 * components
 * 
 * @author Pascal Juppe
 */
@Service
@Transactional
public class TravelguideManagement {

	private final TravelguideRepository travelguides;
	private final CityManagement cityManagement;
	private final AccountingManagement accountingManagement;

	public TravelguideManagement(TravelguideRepository travelguides, CityManagement cityManagement,
			AccountingManagement accountingManagement) {
		this.travelguides = Objects.requireNonNull(travelguides);
		this.cityManagement = Objects.requireNonNull(cityManagement);
		this.accountingManagement = Objects.requireNonNull(accountingManagement);
	}

	/**
	 * creates a new {@linkplain Travelguide} with values from a given
	 * {@linkplain TravelguideDataForm} and saves it to the database
	 * 
	 * @param form the given form
	 * @return the new travelguide
	 */
	public Travelguide createTravelguide(TravelguideDataForm form) {

		Objects.requireNonNull(form);

		var name = form.getName();
		var city = cityManagement.findByName(form.getCity()).get();
		var price = form.getPrice();

		return travelguides.save(new Travelguide(name, city, price));
	}

	/**
	 * updates a {@linkplain Travelguide} with values from a given
	 * {@linkplain TravelguideDataForm}
	 * 
	 * @param id   the id of the travel guide to update
	 * @param form the form that contains the new information
	 */
	public void updateTravelguide(long id, TravelguideDataForm form) {

		Optional<Travelguide> opt = travelguides.findById(id);
		if (opt.isPresent()) {
			Travelguide travelguide = opt.get();
			travelguide.setName(form.getName());
			travelguide.setCity(cityManagement.findByName(form.getCity()).get());
			travelguide.setPrice(form.getPrice());

			travelguides.save(travelguide);
		}
	}

	/**
	 * deletes a {@linkplain Travelguide} from the database
	 * 
	 * @param id the id of the travel guide to delete
	 */
	public void deleteTravelguide(long id) {
		travelguides.deleteById(id);
	}

	/**
	 * invokes {@linkplain AccountingManagement#sellTravelguide(Travelguide)} to
	 * create invoices for a {@linkplain Travelguide} sale
	 * 
	 * @param id the id of the travel guide to buy
	 */
	public void buyTravelguide(long id) {

		Optional<Travelguide> opt = travelguides.findById(id);
		if (opt.isPresent()) {
			accountingManagement.sellTravelguide(opt.get());
		}
	}

	/**
	 * @return all {@linkplain Travelguide}s in the database
	 */
	public Iterable<Travelguide> findAll() {
		return travelguides.findAll(Sort.by(Direction.ASC, "cityName"));
	}

	/**
	 * @param id the travel guide id
	 * @return an {@linkplain Optional} of a {@linkplain Travelguide} with the given
	 *         id
	 */
	public Optional<Travelguide> findById(long id) {
		return travelguides.findById(id);
	}

	/**
	 * @param city the travel guide city
	 * @return all {@linkplain Travelguide}s for a given city
	 */
	public Iterable<Travelguide> findByCity(City city) {
		return travelguides.findByCity(city);
	}

	/**
	 * @param city the travel guide city
	 * @return whether there are any {@linkplain Travelguide}s for a given city in
	 *         the database
	 */
	public boolean hasTravelguidesForCity(City city) {
		return travelguides.stream().anyMatch(guide -> guide.getCity().equals(city));
	}

}
