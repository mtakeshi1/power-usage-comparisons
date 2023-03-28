package io.github.mtakeshi1.rest;

public class ProductInformation {

    private int id;
    private String name;

    public ProductInformation(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProductInformation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
