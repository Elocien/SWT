package tourable.accounting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * The {@code InvoiceRepository} interface saves the {@link Invoice}s
 * persistent.
 * 
 * @author Florian Richter
 */
public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

	/**
	 * @return a {@code Stream} of all {@link Invoice}s
	 */
	@Query("select i from Invoice i")
	public Stream<Invoice> stream();

	/**
	 * Returns a {@link Invoice} that has the {@link TransactionCategory}
	 * {@code category} and was created after {@code date}
	 * 
	 * @param category {@link TransactionCategory}
	 * @param date     {@code LocalDateTime}
	 * @return a {@link Invoice} that has the {@link TransactionCategory}
	 *         {@code category} and was created after {@code date}
	 */
	public List<Invoice> findByTransactionCategoryAndDateAfter(TransactionCategory category, LocalDateTime date);
	
	public List<Invoice> findByDescription(String desc);
}
