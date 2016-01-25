package mybus.buswindow;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import de.tum.score.transport4you.shared.mobilebusweb.data.impl.BlobEntry;
import de.tum.score.transport4you.shared.mobilebusweb.data.impl.BlobEnvelope;
import de.tum.score.transport4you.shared.mobilebusweb.data.impl.ETicket;

public class TicketValidator {
	/* Logger */
	private Logger logger = Logger.getLogger("Application");

	/* The Blob, which is associated with the customer */
	private BlobEntry customerBlob;
//	private final int UNKNOWN=0;
//	private final int VALID=1;
//	private final int EXPIRED_DATE=2;
//	private final int FAILED_AUTH=3;

	public TicketFlag validate(BlobEnvelope envelope) {

		TicketFlag result = TicketFlag.UNKNOWN;
		try {
			PublicKey pKey = PublicKeyReader.getByBC();
			
			if (envelope.checkConsistency(pKey)) {
				// Consistent Envelope
				this.customerBlob = envelope.getPublicBlobEntry();
				// Check if there is an ETicket in the Blob
				logger.debug("Checking for tickets");
				ArrayList<ETicket> eTicketList = customerBlob.geteTicketList();

				if (eTicketList.isEmpty()) {
					logger.debug("No ETicket in list");
				} else {
					logger.debug("ETickets found in list, checking validity");
					ETicket validTicket = checkForValidTicket(eTicketList);

					if (validTicket != null) {
						result = TicketFlag.VALID;

					} else {
						result= TicketFlag.EXPIRED_DATE;
						logger.debug("No valid ticket in list found, need to send ETicket Type list");

					}

				}
			}else {
				result=TicketFlag.FAILED_AUTH;
			}
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	private ETicket checkForValidTicket(ArrayList<ETicket> eTicketList) {

		logger.debug("Checking ETicket List with size: " + eTicketList.size());
		// Check if a Ticket is still valid and return the ticket
		for (ETicket eTicket : eTicketList) {
			//if (eTicket.isInvalidated()) {
				// Check if ticket can be used
				Date timestampInv = eTicket.getValidUntil();
				Date now = new Date();
				System.out.println("ticket timestamp:  " + timestampInv.getTime());
				System.out.println("cur    timestamp:  " + now.getTime());
				if (!now.after(timestampInv)) {
					// Found a valid ticket
					logger.debug("Found a valid already invalidated ticket");
					return eTicket;
				}
		}

		return null;
	}
}
