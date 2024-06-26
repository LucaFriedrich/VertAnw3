package de.berlin.htw.boundary;

import de.berlin.htw.boundary.dto.Basket;
import de.berlin.htw.boundary.dto.Item;
import de.berlin.htw.control.BasketController;
import de.berlin.htw.control.OrderController;
import de.berlin.htw.entity.dao.UserRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.Logger;

@Path("/basket")
public class BasketResource {

    @Context
    UriInfo uri;

    @Context
    SecurityContext context;

    @Inject
    BasketController basket;

    @Inject
    OrderController order;

    @Inject
    UserRepository userRepository;

    @Inject
    Logger logger;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the basket with all items.")
    @APIResponse(responseCode = "200", description = "Retrieve all items in basket successfully")
    @APIResponse(responseCode = "401", description = "No or wrong User Id provided as header")
    @APIResponse(responseCode = "415", description = "Unsupported Media Type")
    public Basket getBasket(@HeaderParam("X-User-Id") final String userId) {
        logger.info(context.getUserPrincipal().getName() + " is calling " + uri.getAbsolutePath());
        return basket.getBasket(userId);
    }

    @DELETE
    @Operation(summary = "Remove all items from basket.")
    @APIResponse(responseCode = "204", description = "Items removed successfully")
    @APIResponse(responseCode = "401", description = "No or wrong User Id provided as header")
    public void clearBasket(@HeaderParam("X-User-Id") final String userId) {
        logger.info(context.getUserPrincipal().getName() + " is calling " + uri.getAbsolutePath());
        basket.clearBasket(userId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Checkout the basket and complete the order.")
    @APIResponse(responseCode = "201", description = "Checkout successfully", headers = @Header(name = "Location", description = "URL to retrieve all orders"))
    @APIResponse(responseCode = "401", description = "No or wrong User Id provided as header")
    @APIResponse(responseCode = "404", description = "No product with this ID in the basket")
    public Response checkout(@HeaderParam("X-User-Id") final String userId) {
        logger.info(context.getUserPrincipal().getName() + " is calling " + uri.getAbsolutePath());
        try {
            basket.checkout(userId);
            return Response.created(uri.getBaseUriBuilder().path("/orders").build()).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("{productId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add an item to basket.")
    @APIResponse(responseCode = "201", description = "Item added successfully")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No or wrong User Id provided as header")
    @APIResponse(responseCode = "409", description = "Another product with this ID already exist in the basket")
    @APIResponse(responseCode = "501", description = "Not Implemented")
    public Response addItem(@HeaderParam("X-User-Id") final String userId, @PathParam("productId") final String productId, @Valid final Item item) {
        logger.info(context.getUserPrincipal().getName() + " is calling " + uri.getAbsolutePath());
        Basket updatedBasket = basket.addItem(userId, productId, item);
        return Response.status(Response.Status.CREATED).entity(updatedBasket).build();
    }

    @DELETE
    @Path("{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove an item from basket.")
    @APIResponse(responseCode = "200", description = "Item removed successfully")
    @APIResponse(responseCode = "401", description = "No or wrong User Id provided as header")
    @APIResponse(responseCode = "404", description = "No product with this ID in the basket")
    public Response removeItem(@HeaderParam("X-User-Id") final String userId, @PathParam("productId") final String productId) {
        logger.info(context.getUserPrincipal().getName() + " is calling " + uri.getAbsolutePath());
        Basket newBasket = basket.removeItem(userId, productId);
        return Response.status(Response.Status.OK).entity(newBasket).build();
    }

    @PATCH
    @Path("{productId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Change the number of an item in the basket.")
    @APIResponse(responseCode = "200", description = "Number changed successfully")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No or wrong User Id provided as header")
    @APIResponse(responseCode = "404", description = "No product with this ID in the basket")
    public Response changeCount(@HeaderParam("X-User-Id") final String userId, @PathParam("productId") final String productId, @Valid final Item item) {
        logger.info(context.getUserPrincipal().getName() + " is calling " + uri.getAbsolutePath());
        Basket newBasket = basket.changeCount(userId, item);
        return Response.status(Response.Status.OK).entity(newBasket).build();
    }

}