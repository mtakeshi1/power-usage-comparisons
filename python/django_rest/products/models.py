from django.db import models


# Create your models here.

class Product(models.Model):
    id = models.BigAutoField(primary_key=True)
    description = models.CharField(max_length=255, null=True)
    name = models.CharField(max_length=255, null=True)
    price = models.DecimalField(max_digits=10, decimal_places=2)
    class Meta:
        db_table = 'product'


class ShoppingCart(models.Model):
    id = models.BigAutoField(primary_key=True)
    total = models.DecimalField(max_digits=10, decimal_places=2)

    class Meta:
        db_table = 'shoppingcart'


class ProductOrder(models.Model):
    id = models.BigAutoField(primary_key=True)
    amount = models.IntegerField()
    product = models.ForeignKey(Product, null=True, on_delete=models.SET_NULL)
    shoppingcart = models.ForeignKey(ShoppingCart, null=True, on_delete=models.SET_NULL)

    class Meta:
        db_table = 'productorder'
