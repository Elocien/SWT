package tourable.travelguide;

import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import tourable.city.City;

public interface TravelguideRepository extends CrudRepository<Travelguide, Long> {

	@Query("select t from Travelguide t")
	public Stream<Travelguide> stream();
	
	public Iterable<Travelguide> findAll(Sort sort);

	public Iterable<Travelguide> findByCity(City city);

}
