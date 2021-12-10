package tourable.accommodation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class AccommodationTypeTest {

	@Test
	public void testNumberOfTypes() {
		assertEquals(4, AccommodationType.values().length);
	}

	@Test
	public void testPreviousTypes() {
		assertEquals(Optional.<AccommodationType>empty(), AccommodationType.SIMPLE.prev());
		assertEquals(Optional.of(AccommodationType.SIMPLE), AccommodationType.STANDARD.prev());
		assertEquals(Optional.of(AccommodationType.STANDARD), AccommodationType.STANDARD_PLUS.prev());
		assertEquals(Optional.of(AccommodationType.STANDARD_PLUS), AccommodationType.LUXURY.prev());
	}

	@Test
	public void testNextTypes() {
		assertEquals(Optional.of(AccommodationType.STANDARD), AccommodationType.SIMPLE.next());
		assertEquals(Optional.of(AccommodationType.STANDARD_PLUS), AccommodationType.STANDARD.next());
		assertEquals(Optional.of(AccommodationType.LUXURY), AccommodationType.STANDARD_PLUS.next());
		assertEquals(Optional.<AccommodationType>empty(), AccommodationType.LUXURY.next());
	}

}
