package io.github.mtakeshi1.rest;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private int id;
    private List<OrderEntry> entries = new ArrayList<>();
    private double total;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<OrderEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<OrderEntry> entries) {
        this.entries = entries;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
