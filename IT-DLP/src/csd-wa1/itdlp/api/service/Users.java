package itdlp.api.service;

import itdlp.api.User;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(Users.PATH)
public interface Users {

    static final String PATH="/user";

    /**
	 * Creates a new message.
	 *
	 * @param user object
	 */
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    void createUser(User user);

    /**
	 * Creates a new message.
	 *
	 * @param byte[] user id
	 */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    User getUser(byte[] id);
    
}
