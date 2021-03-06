package net.gthomps.domino;

import static org.junit.Assert.*;

import net.gthomps.domino.Domino;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DominoTest {

	@Test
	public void testNewDominoHasCorrectHighSide() {
		int high = 4;
		int low = 2;
		Domino domino = new Domino(high, low);
		
		assertEquals(high, domino.getHighSide());		
	}

	@Test
	public void testNewDominoHasCorrectLowSide() {
		int high = 4;
		int low = 2;
		Domino domino = new Domino(high, low);
		
		assertEquals(low, domino.getLowSide());		
	}

	@Test
	public void testNewDominoIsDouble() {
		int high = 4;
		int low = 4;
		Domino domino = new Domino(high, low);
		
		assertTrue(domino.getIsDouble());		
	}
	
	@Test 
	public void testDominoToString() {
		int high = 4;
		int low = 2;
		
		Domino d = new Domino(high, low);
		assertEquals("4:2", d.toString());
	}

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testMaxValueCheck() {
		int tooHigh = 7;
		int low = 0;
		
		exception.expect(RuntimeException.class);
		new Domino(tooHigh, low);
	}

	@Test
	public void testMinValueCheck() {
		int high = 6;
		int tooLow = -1;
		
		exception.expect(RuntimeException.class);
		new Domino(high, tooLow);
	}

	@Test
	public void dominoBeatTest() {
		assertTrue((new Domino(4,2)).beats(4, 0, new Domino(4,1)));
		assertTrue((new Domino(4,6)).beats(4, 0, new Domino(4,3)));
		assertFalse((new Domino(4,5)).beats(4, 0, new Domino(4,4)));
		assertFalse((new Domino(3,2)).beats(4, 0, new Domino(4,1)));
		assertFalse((new Domino(4,6)).beats(4, 0, new Domino(4,4)));
	}
	
	@Test
	public void trumpBeatsNonTrump() {
		assertTrue((new Domino(4,2).beats(4, 2, new Domino(4,4))));
	}
	
	@Test
	public void nonTrumpDoesNotBeatTrump() {
		assertFalse((new Domino(4,4).beats(4, 2, new Domino(4,2))));
	}
	
	@Test
	public void higherTrumpBeatsLowerTrump() {
		assertTrue((new Domino(4,2).beats(4, 2, new Domino(3,2))));
		assertTrue((new Domino(6,2).beats(6, 6, new Domino(6,1))));
		assertTrue((new Domino(6,3).beats(3, 3, new Domino(3,1))));
		assertTrue((new Domino(3,2).beats(3, 3, new Domino(3,1))));
	}
	
	@Test
	public void lowerTrumpDoesNotBeatHigherTrump() {
		assertFalse((new Domino(3,2).beats(2, 2, new Domino(4,2))));
		assertFalse((new Domino(6,1).beats(6, 6, new Domino(6,2))));
		assertFalse((new Domino(1,3).beats(3, 3, new Domino(3,6))));
		assertFalse((new Domino(3,1).beats(3, 3, new Domino(3,2))));
	}
	
	@Test
	public void followsSuitTest() {
		Domino ledDomino = new Domino(5,4);

		assertTrue((new Domino(3,5)).followsSuit(ledDomino, 0));
		assertTrue((new Domino(6,5)).followsSuit(ledDomino, 0));
		assertFalse((new Domino(4,3)).followsSuit(ledDomino, 0));
	}

	@Test
	public void followsSuitTrumpTest() {
		Domino ledDomino = new Domino(4,6);

		assertTrue((new Domino(3,4)).followsSuit(ledDomino, 4));
		assertTrue((new Domino(4,5)).followsSuit(ledDomino, 4));
		assertFalse((new Domino(5,3)).followsSuit(ledDomino, 4));
	}
	
	@Test
	public void testEquals() {
		assertTrue((new Domino(4,2)).equals(new Domino(2,4)));
		assertFalse((new Domino(4,2)).equals(new Domino(1,4)));
	}
	
	@Test
	public void testTrumpIsZero() {
		Domino ledDomino = new Domino(3,0);
		Domino domino = new Domino(0,0);
		
		assertTrue(domino.beats(4, 0, ledDomino));
	}

	@Test
	public void testDoubleWins() {
		Domino winningDomino = new Domino(4,2);
		Domino domino = new Domino(2,2);
		
		assertTrue(domino.beats(2, 6, winningDomino));
	}
	
	@Test 
	public void testLedDomino() {
		Domino ledDomino = new Domino(2,0);
		Domino winningDomino = new Domino(4,2);
		Domino doubleDomino = new Domino(2,2);
		
		assertTrue(doubleDomino.beats(ledDomino, 6, winningDomino));
	}
	
	@Test
	public void testFollowsSuit() {
		Domino ledDomino = new Domino(6,3);
		Domino trumpDomino = new Domino(6,1);
		
		assertFalse(trumpDomino.followsSuit(ledDomino, 1));
	}
}
