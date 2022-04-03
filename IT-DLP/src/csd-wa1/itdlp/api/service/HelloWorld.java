package itdlp.api.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(HelloWorld.PATH)
public interface HelloWorld {
    
    static final String PATH="/hello";

	/**
	 * Creates a new message.
	 *
	 * @param msg Message to be created.
	 * @param media Optional - Attached media
	 * @return 200 the messageId.
	 *         409 if the msg id already exists.
	 *         400 otherwise.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	String helloWorld();

}
