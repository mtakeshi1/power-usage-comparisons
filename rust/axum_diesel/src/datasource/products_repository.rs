use diesel::{OptionalExtension, RunQueryDsl};
use diesel::prelude::*;

use crate::core::error::ServiceError::ErrorExecutingQueryToDatabase;
use crate::core::models::Product;
use crate::core::Result;
use crate::datasource::ConnectionPool;
use crate::datasource::models::ProductEntity;
use crate::datasource::schema::product::dsl::product;
use crate::datasource::schema::product::id as product_id;

#[derive(Clone)]
pub struct ProductRepository {
    pub connection_pool: ConnectionPool,
}

impl ProductRepository {
    pub async fn list_products(&self) -> Result<Vec<Product>> {
        let mut conn = self.connection_pool.get_connection()?;

        let results = product
            .load::<ProductEntity>(&mut conn)
            .map_err(ErrorExecutingQueryToDatabase)?
            .into_iter()
            .map(|it| it.into())
            .collect();
        Ok(results)
    }

    pub async fn get_product(&self, id: i32) -> Result<Option<Product>> {
        let mut conn = self.connection_pool.get_connection()?;

        let results = product
            .filter(product_id.eq(id))
            .first::<ProductEntity>(&mut conn)
            .optional()
            .map_err(ErrorExecutingQueryToDatabase)?
            .map(|it| it.into());

        Ok(results)
    }

    pub async fn get_products(&self, ids: &[i32]) -> Result<Vec<Product>> {
        let mut conn = self.connection_pool.get_connection()?;

        let results = product
            .filter(product_id.eq_any(ids))
            .load::<ProductEntity>(&mut conn)
            .map_err(ErrorExecutingQueryToDatabase)?
            .into_iter()
            .map(|it| it.into())
            .collect();
        Ok(results)
    }
}

impl From<ProductEntity> for Product {
    fn from(item: ProductEntity) -> Self {
        Product {
            id: item.id,
            name: item.name,
            description: item.description,
            price: item.price,
        }
    }
}