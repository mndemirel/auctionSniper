package test.unit.auctionsniper.ui;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import auctionsniper.Auction;
import auctionsniper.AuctionSniper;
import auctionsniper.Item;
import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import auctionsniper.ui.Column;
import auctionsniper.ui.SnipersTableModel;

import com.objogate.exception.Defect;

@RunWith(JMock.class)
public class SnipersTableModelTest {
	private final Mockery context = new Mockery();
	private TableModelListener listener = context.mock(TableModelListener.class);
	private final SnipersTableModel model = new SnipersTableModel();
	private final Auction auction = context.mock(Auction.class);
	private final AuctionSniper sniper = new AuctionSniper(new Item("item id", Integer.MAX_VALUE), auction);
	private final AuctionSniper anotherSniper = new AuctionSniper(new Item("another item id", Integer.MAX_VALUE), auction);

	@Before public void
	attachModelListener() {
		model.addTableModelListener(listener);
	}

	@Test public void
	hasEnoughColumns() {
		assertThat(model.getColumnCount(), equalTo(Column.values().length));
	}

	@Test public void
	setsSniperValuesInColumns() {
		SniperSnapshot bidding = sniper.getSnapshot().bidding(555, 666);
		context.checking(new Expectations() {{
			allowing(listener).tableChanged(with(anyInsertionEvent()));
			one(listener).tableChanged(with(aChangeInRow(0)));
		}});

		model.sniperAdded(sniper);
		model.sniperStateChanged(bidding);

		assertRowMatchesSnapshot(0, bidding);
	}

	@Test public void
	notifiesListenersWhenAddingASniper() {
		context.checking(new Expectations() {{
			one(listener).tableChanged(with(anInsertionAtRow(0)));
		}});

		assertEquals(0, model.getRowCount());
		model.sniperAdded(sniper);
		assertEquals(1, model.getRowCount());
		assertRowMatchesSnapshot(0, sniper.getSnapshot());
	}

	@Test public void
	holdsSnipersInAdditionOrder() {
		context.checking(new Expectations() {{
			ignoring(listener);
		}});

		model.sniperAdded(sniper);
		model.sniperAdded(anotherSniper);

		assertEquals("item id", cellValue(0, Column.ITEM_IDENTIFIER));
		assertEquals("another item id", cellValue(1, Column.ITEM_IDENTIFIER));
	}

	@Test public void
	updatesCorrectRowForSniper() {
		SniperSnapshot bidding = sniper.getSnapshot().bidding(100, 100);

		context.checking(new Expectations() {{
			allowing(listener).tableChanged(with(anyInsertionEvent()));
			one(listener).tableChanged(with(aChangeInRow(0)));
		}});

		model.sniperAdded(sniper);
		model.sniperAdded(anotherSniper);

		model.sniperStateChanged(bidding);
		assertRowMatchesSnapshot(0, bidding);
	}

	@Test(expected=Defect.class) public void
	throwsDefectIfNoExistingSniperForAnUpdate() {
		model.sniperStateChanged(new SniperSnapshot("item 3", 123, 123, SniperState.WINNING));
	}

	private void assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
		assertEquals(snapshot.itemId, cellValue(row, Column.ITEM_IDENTIFIER));
		assertEquals(snapshot.lastPrice, cellValue(row, Column.LAST_PRICE));
		assertEquals(snapshot.lastBid, cellValue(row, Column.LAST_BID));
		assertEquals(SnipersTableModel.textFor(snapshot.state), cellValue(row, Column.SNIPER_STATE));
	}

	private Object cellValue(int rowIndex, Column column) {
		return model.getValueAt(rowIndex, column.ordinal());
	}

	Matcher<TableModelEvent> anyInsertionEvent() {
		return hasProperty("type", equalTo(TableModelEvent.INSERT));
	}

	Matcher<TableModelEvent> anInsertionAtRow(final int row) {
		return samePropertyValuesAs(new TableModelEvent(model, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	private Matcher<TableModelEvent> aChangeInRow(int row) {
		return samePropertyValuesAs(new TableModelEvent(model, row));
	}
}
