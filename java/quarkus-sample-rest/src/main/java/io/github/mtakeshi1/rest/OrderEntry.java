package io.github.mtakeshi1.rest;

public class OrderEntry {

    private int productId;
    private int amount;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public OrderEntry(int productId, int amount) {
        this.productId = productId;
        this.amount = amount;
    }

    public OrderEntry() {
    }
}
