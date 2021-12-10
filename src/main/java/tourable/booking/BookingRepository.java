package tourable.booking;

import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import tourable.customer.Customer;

public interface BookingRepository extends CrudRepository<Booking, Long> {

	@Query("select b from Booking b")
	public Stream<Booking> stream();

	public Iterable<Booking> findByCustomer(Customer customer);

	public Iterable<Booking> findAll();

	public Iterable<Booking> findAll(Sort sort);

	public Iterable<Booking> findByStatus(BookingStatus status);

	public Iterable<Booking> findByStatus(BookingStatus status, Sort sort);
}
