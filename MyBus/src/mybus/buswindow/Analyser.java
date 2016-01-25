package mybus.buswindow;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.bouncycastle.util.encoders.Base64;
import org.restlet.resource.ClientResource;
import org.w3c.dom.Document;

import de.tum.score.transport4you.shared.mobilebusweb.data.impl.BlobEnvelope;

public class Analyser {
	/*
	 * Get signed ticket from remote server Convert to an Envelop object
	 */

	private TicketValidator validator;
	private String baseURL = "";

	public Analyser(String baseURL) {
		this.validator = new TicketValidator();
		this.baseURL = baseURL;
	}

	public String validateTicket() throws Exception {
		// boolean result = false;
		String message = "";
		BlobEnvelope ticket = fetchTicket();

		switch (validator.validate(ticket)) {
		case VALID:
			message = "The ticket is valid!";
			break;
		case EXPIRED_DATE:
			message = "The ticket is expired";
			break;
		case FAILED_AUTH:
			message = " The ticket has invalid signature!";
			break;
		default:
			message = "Unknown error happened. The ticket is corrupted!";
			break;
		}

		return message;
	}

	private BlobEnvelope fetchTicket() {
		BlobEnvelope result = null;
		try {
			System.out.println("fetching : " + baseURL);

			String xml = new ClientResource(baseURL).get().getText();
			System.out.println("xml: " + xml);
			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);

			XPath xpath = XPathFactory.newInstance().newXPath();

			String ticketString = (String) xpath.evaluate("/user/ticket",
					document, XPathConstants.STRING);

			// String ticketString =
			// "rO0ABXNyAEVkZS50dW0uc2NvcmUudHJhbnNwb3J0NHlvdS5zaGFyZWQubW9iaWxlYnVzd2ViLmRhdGEuaW1wbC5CbG9iRW52ZWxvcGXcD7lWxEHhJwIAAUwAD3NpZ25lZEJsb2JFbnRyeXQAHExqYXZhL3NlY3VyaXR5L1NpZ25lZE9iamVjdDt4cHNyABpqYXZhLnNlY3VyaXR5LlNpZ25lZE9iamVjdAn/vWgqPNX/AgADWwAHY29udGVudHQAAltCWwAJc2lnbmF0dXJlcQB+AARMAAx0aGVhbGdvcml0aG10ABJMamF2YS9sYW5nL1N0cmluZzt4cHVyAAJbQqzzF/gGCFTgAgAAeHAAAAOZrO0ABXNyAEJkZS50dW0uc2NvcmUudHJhbnNwb3J0NHlvdS5zaGFyZWQubW9iaWxlYnVzd2ViLmRhdGEuaW1wbC5CbG9iRW50cnnKmiqOxqt/+QIACEwADmFjY291bnRCYWxhbmNldAASTGphdmEvbGFuZy9Eb3VibGU7TAALYWNjb3VudFR5cGV0ABJMamF2YS9sYW5nL1N0cmluZztMAAtlVGlja2V0TGlzdHQAFUxqYXZhL3V0aWwvQXJyYXlMaXN0O0wAEG1vZGlmaWNhdGlvbkRhdGV0ABBMamF2YS91dGlsL0RhdGU7TAALdXNlckFkZHJlc3NxAH4AAkwABnVzZXJJZHEAfgACTAAIdXNlck5hbWVxAH4AAlsAC3VzZXJQaWN0dXJldAACW0J4cHNyABBqYXZhLmxhbmcuRG91YmxlgLPCSilr+wQCAAFEAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAAAAAAAAAAdAANcHJlUGF5QWNjb3VudHNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABc3IAQGRlLnR1bS5zY29yZS50cmFuc3BvcnQ0eW91LnNoYXJlZC5tb2JpbGVidXN3ZWIuZGF0YS5pbXBsLkVUaWNrZXRDhOgO0xxWfwIACEoAAmlkWgALaW52YWxpZGF0ZWRKAAl2YWxpZFRpbWVMAApjdXN0b21lcklkcQB+AAJMAA9lbmNyeXB0ZWRUaWNrZXRxAH4AAkwADWludmFsaWRhdGVkQXRxAH4ABEwAC3NlbGxpbmdEYXRlcQB+AARMAAp2YWxpZFVudGlscQB+AAR4cgBSZGUudHVtLnNjb3JlLnRyYW5zcG9ydDR5b3Uuc2hhcmVkLm1vYmlsZWJ1c3dlYi5kYXRhLmltcGwuQWJzdHJhY3RQZXJzaXN0ZW5jZU9iamVjdNxNusPWVtthAgAAeHAAAAAAAAAAAAAAAAFSeW78kXQABHRlc3R0ADlUNFk6MCx0ZXN0LDE0NTM3MzYyNjI4MDE6NWM1MjlhZjM3ZWI5MWQ4YzQzZmI2OTkxZDYyMDdiZmVwcHNyAA5qYXZhLnV0aWwuRGF0ZWhqgQFLWXQZAwAAeHB3CAAAAVJ5bvyReHhzcQB+ABJ3CAAAAVJVYnsZeHQABHRlc3RxAH4AEHBwdXEAfgAHAAABADsl59q2EK6iS8+o5n0yFgLyLgsyfDLBrKQhEaKsKxw9tGNMTcKrpHtMiUTiPc8WwjPoWt6E7/jiGD4k0KsoshjySqMgOVw/ciHiwlwxXZGYVEVntf4YxGgyov8fGqIIHcW2zIlCrf5VH8P7/rT6zYDCit8AhP0T6i5SicybKjlX2Qw4RNUmWhl7s4z/m8FlFoTaKmeuRbIf3QEuSfIbnf1KTPPbgGsp7ixvyMPZYHTTuaiE8apKOPihtN8BraIwZ1bZn2EJaF6d/VWYej3IMjkVfLLQ8P3XZjqbJ60uKnuZdKKsD2qklX947Lu2rREoZ+ioxgvzsfGLNBjWaqePjWZ0AAtTSEExd2l0aFJTQQ==";

			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
					Base64.decode(ticketString.getBytes()));
			ObjectInput in = new ObjectInputStream(byteInputStream);
			result = (BlobEnvelope) in.readObject();
			in.close();
			byteInputStream.close();

		} catch (Exception e) {
			System.out.println("REST request failed (synchronizeETickets)");
		}

		return result;
	}
}
