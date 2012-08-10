package lisp2forex.handlers.formatters;

import java.util.List;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;

public class MyLispFormatter implements Formatter {

	// seconds between 1 January 1970 <-> 1 January 1900 java and lisp time
	final static long timeDelta = 2208988800L;

	synchronized public String format(ITick tic) {

		long lispTime = tic.getTime() / 1000 + timeDelta;
		double[] asks = tic.getAsks();
		double[] bids = tic.getBids();
		double[] askVol = tic.getAskVolumes();
		double[] bidVol = tic.getBidVolumes();

		int size = Math.min(asks.length, bids.length);
		StringBuilder str = new StringBuilder(10 * (10 + 4 * 6));

		str.append("(");
		for (int i = 0; i < size; i++) {
			str.append("(");
			str.append(lispTime + " ");
			str.append(asks[i] + " ");
			str.append(bids[i] + " ");
			str.append(askVol[i] + " ");
			str.append(bidVol[i] + " ");
			str.append(")");
		}
		str.append(")");

		return str.toString();

	}

	synchronized public String format(IMessage message) {
		return "(:" + message.getType() + " . \"" + message.getContent() + "\")";
	}

	synchronized public String format(IAccount account) {
		StringBuilder str = new StringBuilder();
		str.append("(:account . (");
		str.append("(:balance . " + account.getBalance() + ")");
		str.append("(:equity . " + account.getEquity() + ")");
		str.append("(:leverage . " + account.getUseOfLeverage() + ")");
		str.append("(:credit . " + account.getCreditLine() + ")");
		str.append("))");
		return str.toString();
	}
	
	synchronized public String format(List<IOrder> orders) {
		StringBuilder str = new StringBuilder();
		str.append("(orders . (");
		for (IOrder order : orders) {
			str.append("(:" + order.getLabel() + " . (");
			str.append("(:pair . :" + order.getInstrument().name() + ")");
			str.append("(:state . :" + order.getState() + ")");
			str.append("(:command . :" + order.getOrderCommand() + ")");
			str.append("(:amount . " + order.getAmount() * 1000000.0d + ")");
			str.append("(:open . " + order.getOpenPrice() + ")");
			str.append("(:close . " + order.getClosePrice() + ")");
			str.append("(:pl . " + order.getProfitLossInPips() + ")");
			str.append("(:pl-2 . " + order.getProfitLossInAccountCurrency() + ")");
			str.append("))");
		}
		str.append("))");
		System.out.println(str.toString());
		return str.toString();
	}
	
	synchronized public String format(Exception e) {
		return e.toString();
	}
}
