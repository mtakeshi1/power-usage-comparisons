use std::collections::HashMap;

use crate::core::error::ServiceError;
use crate::core::error::ServiceError::ProductNotFound;
use crate::core::models::{NewOrder, Product, ProductAmount, ShoppingCart};
use crate::datasource::orders_repository::OrderRepository;
use crate::datasource::products_repository::ProductRepository;

pub mod models;
pub mod error;

pub type Result<T> = core::result::Result<T, ServiceError>;

#[derive(Clone)]
pub struct ProductService {
    pub product_repository: ProductRepository,
}

impl ProductService {
    pub async fn list_products(&self) -> Result<Vec<Product>> {
        self.product_repository.list_products().await
    }

    pub async fn get_product(&self, id: i32) -> Result<Option<Product>> {
        self.product_repository.get_product(id).await
    }
}

#[derive(Clone)]
pub struct OrderService {
    pub order_repository: OrderRepository,
    pub product_repository: ProductRepository,
}

impl OrderService {
    pub async fn create_order(&self, order: Vec<ProductAmount>) -> Result<NewOrder> {
        let product_ids: Vec<i32> = order.iter().map(|it| it.product_id).collect();
        let products: HashMap<i32, Product> = self.product_repository.get_products(&product_ids).await?
            .into_iter().map(|it| (it.id, it))
            .collect();

        let mut total = 0f64;
        for entry in &order {
            if let Some(product) = products.get(&entry.product_id) {
                total += entry.amount as f64 * product.price;
            } else {
                return Err(ProductNotFound { id: entry.product_id });
            }
        }

        self.order_repository.create_order(order, total).await
    }

    pub async fn get_order(&self, id: i32) -> Result<ShoppingCart> {
        self.order_repository.get_order(id).await
    }
}