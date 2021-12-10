package tourable.user;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

interface UserRepository extends CrudRepository<User, Long> {

	@Query("select u from User u")
	public Stream<User> stream();
}
