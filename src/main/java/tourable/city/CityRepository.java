package tourable.city;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CityRepository extends CrudRepository<City, Long> {

	@Query("select c from City c where c.disabled = false")
	public Stream<City> stream();

	public Iterable<City> findByDisabledFalse(Sort sort);

	public Optional<City> findByIdAndDisabledFalse(long id);

	public Optional<City> findByNameAndDisabledFalse(String name);

}
