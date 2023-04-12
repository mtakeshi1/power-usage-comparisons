from django.urls import path
from .views import ProductList, ProductRetrieve, OrderCreateView, OrderRetrieveView

urlpatterns = [
    path('products', ProductList.as_view(), name='product-list'),
    path('products/<int:id>', ProductRetrieve.as_view(), name='product-retrieve'),
    path('orders/new', OrderCreateView.as_view(), name='order-create'),
    path('orders/<int:pk>', OrderRetrieveView.as_view(), name='order-retrieve'),
]
