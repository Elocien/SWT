package tourable.accommodation;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import tourable.city.City;

public interface AccommodationRepository extends CrudRepository<Accommodation, Long> {

	/**
	 * @return the {@linkplain Stream} of all enabled accommodations
	 */
	@Query("select a from Accommodation a where a.disabled = false")
	public Stream<Accommodation> stream();

	public Iterable<Accommodation> findByDisabledFalse(Sort sort);

	public Optional<Accommodation> findByIdAndDisabledFalse(long id);

	public Iterable<Accommodation> findByCityAndDisabledFalse(City city);

	public Optional<Accommodation> findByNameAndDisabledFalse(String name);

	public Iterable<Accommodation> findByLocationAndDisabledFalse(AccommodationLocation location);

	public Iterable<Accommodation> findByLocationAndDisabledFalse(AccommodationLocation location, Sort sort);

	public Iterable<Accommodation> findByTypeAndDisabledFalse(AccommodationType type);

	public Iterable<Accommodation> findByTypeAndDisabledFalse(AccommodationType type, Sort sort);

	@Query("SELECT a FROM Accommodation a WHERE (:city is null or a.city = :city) and (:type is null "
			+ " or a.type = :type) and (:location is null or a.location = :location)")
	public Iterable<Accommodation> findByCityTypeAndLocation(City city, AccommodationType type,
			AccommodationLocation location);
}
