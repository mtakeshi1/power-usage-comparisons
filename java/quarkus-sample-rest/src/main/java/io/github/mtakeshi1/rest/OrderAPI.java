package io.github.mtakeshi1.rest;

import io.github.mtakeshi1.model.Product;
import io.github.mtakeshi1.model.ProductOrder;
import io.github.mtakeshi1.model.ShoppingCart;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@Path("/orders")
public class OrderAPI {

    @POST
    @Path("/new")
    @Transactional
    public int placeOrder(List<OrderEntry> productsAndAmount) {
        ShoppingCart cart = new ShoppingCart();
        long total = 0;
        for (var entry : productsAndAmount) {
            Product product = Product.withId(entry.getProductId());
            ProductOrder po = new ProductOrder();
            po.setProduct(product);
            po.setShoppingCart(cart);
            po.setAmount(entry.getAmount());
            cart.getProducts().add(po);
            total += entry.getAmount() * product.getPrice();
        }
        cart.setTotal(total);
        cart.persistAndFlush();
        return cart.getId();
    }

    @GET
    @Path("/{id}")
    @Transactional
    public Order view(@PathParam("id") int id) {
        ShoppingCart cart = ShoppingCart.findById(id);
        Order order = new Order();
        order.setId(cart.getId());
        order.setTotal(cart.getTotal());
        for (var entry : cart.getProducts()) {
            order.getEntries().add(new OrderEntry(entry.getProduct().getId(), entry.getAmount()));
        }
        return order;
    }

}
