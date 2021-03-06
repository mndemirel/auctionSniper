package auctionsniper.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import auctionsniper.AuctionSniper;
import auctionsniper.SniperListener;
import auctionsniper.SniperPortfolio.PortfolioListener;
import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;

import com.objogate.exception.Defect;

@SuppressWarnings("serial")
public class SnipersTableModel extends AbstractTableModel implements SniperListener, PortfolioListener {
	private List<SniperSnapshot> sniperSnapshots = new ArrayList<SniperSnapshot>();

	private static String[] STATUS_TEXT = {
		"Joining", "Bidding", "Winning", "Losing", "Lost", "Won", "Failed"
	};

	@Override
	public int getColumnCount() {
		return Column.values().length;
	}
	@Override
	public int getRowCount() {
		return sniperSnapshots.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return Column.at(columnIndex).valueIn(sniperSnapshots.get(rowIndex));
	}

	@Override
	public void sniperStateChanged(SniperSnapshot newSnapshot) {
		int row = rowMatching(newSnapshot);
		sniperSnapshots.set(row, newSnapshot);
		fireTableRowsUpdated(row, row);
	}

	private int rowMatching(SniperSnapshot snapshot) {
		for (int i=0; i < sniperSnapshots.size(); i++) {
			if (snapshot.isForSameItemAs(sniperSnapshots.get(i))) {
				return i;
			}
		}
		throw new Defect("Cannot find match for " + snapshot);
	}
	public static String textFor(SniperState state) {
		return STATUS_TEXT[state.ordinal()];
	}

	@Override
	public String getColumnName(int column) {
		return Column.at(column).name;
	}

	private void addSniperSnapshot(SniperSnapshot snapshot) {
		sniperSnapshots.add(snapshot);
		int row = sniperSnapshots.size()-1;
		fireTableRowsInserted(row, row);
	}

	@Override
	public void sniperAdded(AuctionSniper sniper) {
		addSniperSnapshot(sniper.getSnapshot());
		sniper.addSniperListener(new SwingThreadSniperListener(this));
	}
}