package net.gthomps.domino;

import static org.junit.Assert.*;

import net.gthomps.domino.Bid;
import net.gthomps.domino.Domino;
import net.gthomps.domino.GameService;
import net.gthomps.domino.GameState;
import net.gthomps.domino.Player;
import net.gthomps.domino.GameState.State;
import net.gthomps.domino.ai.PlayChooser;
import net.gthomps.domino.ai.SimpleBidChooser42;
import net.gthomps.domino.ai.SimpleDominoChooser;
import net.gthomps.domino.rules.Rules;
import net.gthomps.domino.rules.Texas42Rules;
import net.gthomps.domino.validation.BidValidator;
import net.gthomps.domino.validation.BidValidator42;
import net.gthomps.domino.validation.PlayValidator;
import net.gthomps.domino.validation.PlayValidator42;
import net.gthomps.domino.validation.TestBidValidator;
import net.gthomps.domino.validation.TestPlayValidator;
import net.gthomps.domino.validation.ValidatorException;

import org.junit.Test;

public class GameServiceTest {
	private final GameService service = new GameService();
	private BidValidator testBidValidator = new TestBidValidator();
	private PlayValidator testPlayValidator = new TestPlayValidator();
	private Rules testRules = new Texas42Rules();
	
	@Test
	public void newGameHasFourPlayers() {
		service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator, testRules);
		assertEquals(4, service.getGame().getPlayers().length);
	}

	@Test
	public void newGameHasCurrentHand() {
		service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator, testRules);
		assertNotNull(service.getGame().getCurrentHand());
	}

	@Test
	public void newGamePlayersHaveSevenDominos() {
		service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator, testRules);
		assertEquals(7, service.getGame().getPlayers()[0].getDominosInHand().size());
	}
	
	@Test
	public void newGameReturnsCorrectState() {
		GameState state = service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator, testRules);
		assertEquals(State.Bidding, state.getState());
		assertEquals(service.getGame().getPlayers()[0], state.getNextPlayer());		
	}

	@Test
	public void addOneBidAddsBidAndReturnsCorrectState() throws ValidatorException {
		service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator, testRules);
		Bid bid = new Bid(service.getGame().getPlayers()[0], Bid.PASS);
		
		GameState state = service.placeBid(bid);
		
		assertNotNull(service.getGame().getCurrentHand().getBids().contains(bid));
		assertEquals(State.Bidding, state.getState());
		assertEquals(service.getGame().getPlayers()[1], state.getNextPlayer());
	}

	private Bid[] createFourBids(Player[] players) {
		Bid[] bids = new Bid[4];
		bids[0] = new Bid(players[0], Bid.PASS);
		bids[1] = new Bid(players[1], Bid.PASS);
		bids[2] = new Bid(players[2], Bid.PASS);
		bids[3] = new Bid(players[3], 30);
		
		return bids;
	}
	
	@Test
	public void addingFourBidsDeclaresBidWinner() throws ValidatorException {
		service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator, testRules);
		
		Bid[] bids = createFourBids(service.getGame().getPlayers());
		placeBids(bids);
		
		assertEquals(bids[3], service.getGame().getCurrentHand().getWinningBid());
	}

	private GameState placeBids(Bid[] bids) throws ValidatorException {
		GameState state = null;
		for (Bid b : bids)
			state = service.placeBid(b);
				
		return state;
	}
	
	@Test
	public void addingFourBidsAndSettingTrumpCausesNewTrick() throws ValidatorException {
		service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator, testRules);
		
		Bid[] bids = createFourBids(service.getGame().getPlayers());
		placeBids(bids);
		
		service.setTrump(null, 0);
		
		assertNotNull(service.getGame().getCurrentHand().getCurrentTrick());
	}
	
	@Test
	public void addingFourBidsWithWinnerSetsCorrectNextPlayer() throws ValidatorException {
		service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator, testRules);
		
		Bid[] bids = createFourBids(service.getGame().getPlayers());
		GameState state = placeBids(bids);
		
		assertEquals(service.getGame().getPlayers()[3], state.getNextPlayer());
	}
	
	@Test
	public void addingFourDominosCausesPlayedTricksCountToIncrease() throws ValidatorException {
		service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator, testRules);

		Bid[] bids = createFourBids(service.getGame().getPlayers());
		placeBids(bids);
		service.setTrump(null, 0);
		
		for (Player p : service.getGame().getPlayers())
			service.playDomino(p, new Domino(0,0));
		
		assertEquals(1, service.getGame().getCurrentHand().getPlayedTricks().size());
	}

	@Test
	public void testForfeit() {
		Player[] players = PlayerTest.createFourGenericPlayers();
		
		service.createNewGame(players, testBidValidator, testPlayValidator, testRules);
		assertNull(service.getGame().getWinner());

		service.forfeit(service.getGame().getTeam(0).getPlayer(0));
		assertEquals(service.getGame().getTeam(1), service.getGame().getWinner());
	}
	
	@Test
	public void playFullGame() throws ValidatorException {
//		GameState state = service.createNewGame(PlayerTest.createFourGenericPlayers(), testBidValidator, testPlayValidator);
		GameState state = service.createNewGame(PlayerTest.createFourGenericPlayers(), new BidValidator42(), new PlayValidator42(), testRules);
		assertEquals(State.Bidding, state.getState());
		assertFalse(service.getGame().hasWinner());

		while (state.getState().equals(State.Bidding)) {
			assertFalse(service.getGame().getCurrentHand().isOver());
			assertFalse(service.getGame().hasWinner());

			Player[] biddingPlayers = service.getGame().getHandOrderedPlayers();
			for (Player player : biddingPlayers) {
				PlayChooser<Bid> bidChooser = new SimpleBidChooser42();
				Bid bid = bidChooser.choose(service.getGame(), player);
				state = service.placeBid(bid);
			}
			
			assertEquals(-1, service.getGame().getCurrentHand().getTrump());			
			state = service.setTrump(state.getNextPlayer(), 5);
			assertEquals(5, service.getGame().getCurrentHand().getTrump());			
			assertEquals(5, service.getGame().getCurrentHand().getCurrentTrick().getTrump());			
			
			assertEquals(State.Playing, state.getState());			
			assertTrue(service.getGame().getCurrentHand().biddingIsOver());
			assertFalse(service.getGame().getCurrentHand().getCurrentTrick().isOver());
	
			while (state.getState().equals(State.Playing)) {
				PlayChooser<Domino> chooser = new SimpleDominoChooser();
				Domino domino = chooser.choose(service.getGame(), state.getNextPlayer());
				state = service.playDomino(state.getNextPlayer(), domino);
			}
		}
		
		assertTrue(service.getGame().hasWinner());
	}
}
