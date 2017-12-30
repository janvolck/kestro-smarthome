package be.kestro.smarthome.core.api;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("")
@Produces("text/plain")
public interface SmarthomeResource {

    @GET
    @Path("/version")
    String getVersion();

    @GET
    @Path("version/module/{id}")
    String getVersion(@PathParam("id") String id);
}
