package tourable.customer;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

	@Query("select c from Customer c where c.disabled = false")
	public Stream<Customer> stream();

	public Iterable<Customer> findByDisabledFalse();

	public Iterable<Customer> findByNameContainingIgnoreCaseAndDisabledFalse(String name);
	
	public Optional<Customer> findByIdAndDisabledFalse(long id);

	public Optional<Customer> findByEmailAndDisabledFalse(String email);
}
