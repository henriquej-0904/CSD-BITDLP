package itdlp.api.service;

import java.util.List;

import itdlp.api.User;
import itdlp.api.User.UserId;
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
    User createUser(UserId userId);

    /**
	 * Creates a new message.
	 *
	 * @param UserId user id
	 */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    User getUser(UserId id);

    
    @Path("/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Get all users in the Ledger.
     * @return A list of users.
     */
    List<User> getAllUsers();
    
}
