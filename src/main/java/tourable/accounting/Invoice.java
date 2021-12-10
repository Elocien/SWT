package tourable.accounting;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.salespointframework.time.BusinessTime;

/**
 * The {@code Invoice} entity represents an invoice.
 * 
 * @author Florian Richter
 */
@Entity
public class Invoice {

	private @Id @GeneratedValue long id;
	private TransactionCategory transactionCategory;
	private double value;
	private String description;
	private LocalDateTime date;

	@SuppressWarnings("unused")
	private Invoice() {

	}

	/**
	 * Constructor of class {@code Invoice}.
	 * 
	 * @param value               the value of the invoice (negative for Expense,
	 *                            positive for Income)
	 * @param description         a description of {@code Invoice}.
	 * @param transactionCategory the {@link TransactionCategory} of the
	 *                            {@code Invoice}
	 * @param businessTime        {@link BusinessTime} to set the creation date.
	 */
	public Invoice(double value, String description, TransactionCategory transactionCategory,
			BusinessTime businessTime) {
		this.value = value;
		this.description = description;
		this.transactionCategory = transactionCategory;
		this.date = businessTime.getTime();
	}

	public TransactionCategory getTransactionCategory() {
		return this.transactionCategory;
	}

	/**
	 * @return Returns whether the {@code Invoice} is revenue
	 */
	public boolean isRevenue() {
		return this.value >= 0;
	}

	/**
	 * @return Returns whether the {@code Invoice} is expense
	 */
	public boolean isExpense() {
		return this.value < 0;
	}

	public double getValue() {
		return this.value;
	}

	public String getDescription() {
		return this.description;
	}

	public LocalDateTime getDate() {
		return this.date;
	}

	public long getId() {
		return this.id;
	}
}
