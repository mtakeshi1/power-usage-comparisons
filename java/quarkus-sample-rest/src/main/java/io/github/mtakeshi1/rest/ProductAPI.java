package io.github.mtakeshi1.rest;

import io.github.mtakeshi1.model.Product;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Path("/products")
public class ProductAPI {

    @GET
    @Transactional
    public List<ProductInformation> list() {
        return Product.<Product>listAll().stream().map(p -> new ProductInformation(p.getId(), p.getName())).toList();
    }


    @Transactional
    @POST
    @Path("/random")
    public void insertRandom() throws Exception {
        if (Product.count() == 0) {
            try (var reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/sampleproducts.csv")))) {
                reader.lines()
                        .map(s -> s.split(";"))
                        .map(line -> new Product(line[0], line[1], Double.parseDouble(line[2])))
                        .forEach(p -> p.persistAndFlush());
            }
        }
    }

    @Transactional
    @GET
    @Path("/{id}")
    public Product withId(int id) {
        return Product.withId(id);
    }

}
