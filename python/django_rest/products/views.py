from django.shortcuts import render
from rest_framework import generics, status
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Product, ShoppingCart, ProductOrder
from .serializers import ProductListSerializer, ProductRetrieveSerializer, ProductOrderSerializer


class ProductList(generics.ListAPIView):
    queryset = Product.objects.all()
    serializer_class = ProductListSerializer


class ProductRetrieve(generics.RetrieveAPIView):
    queryset = Product.objects.all()
    serializer_class = ProductRetrieveSerializer
    lookup_field = 'id'


class OrderCreateView(APIView):
    def post(self, request):
        items = request.data
        shopping_cart = ShoppingCart.objects.create(total=0)
        total = 0
        for item in items:
            product = Product.objects.get(id=item['productId'])
            amount = item['amount']
            ProductOrder.objects.create(
                amount=amount,
                product=product,
                shoppingcart=shopping_cart
            )
            total += product.price * amount
        shopping_cart.total = total
        shopping_cart.save()
        return Response(shopping_cart.id)


class OrderRetrieveView(APIView):
    def get(self, request, pk):
        shopping_cart = ShoppingCart.objects.get(id=pk)
        orders = ProductOrder.objects.filter(shoppingcart=shopping_cart)
        entries = [
            {'productId': order.product_id, 'amount': order.amount}
            for order in orders
        ]
        return Response({'id': pk, 'entries': entries, 'total': shopping_cart.total})
