package lisp2forex.handlers.formatters;

import java.util.List;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;

public interface Formatter {
	
	public String format(ITick tic);

	public String format(IMessage message);

	public String format(IAccount account);
	
	public String format(List<IOrder> orders);
	
	public String format(Exception e);
}