use diesel::prelude::*;

use crate::core::error::ServiceError;
use crate::core::error::ServiceError::{ErrorExecutingQueryToDatabase, OrderNotFound};
use crate::core::models::{NewOrder, ProductAmount, ProductOrder, ShoppingCart};
use crate::datasource::ConnectionPool;
use crate::datasource::models::{NewProductOrderEntity, NewShoppingCartEntity, ProductOrderEntity, ShoppingCartEntity};
use crate::datasource::schema::productorder::dsl::productorder;
use crate::datasource::schema::shoppingcart::dsl::shoppingcart;
use crate::datasource::schema::shoppingcart::id as shoppingcart_id;

pub type Result<T> = core::result::Result<T, ServiceError>;


#[derive(Clone)]
pub struct OrderRepository {
    pub connection_pool: ConnectionPool,
}

impl OrderRepository {
    pub async fn create_order(&self, order: Vec<ProductAmount>, total: f64) -> Result<NewOrder> {
        let mut conn = self.connection_pool.get_connection()?;

        let cart_id: i32 = diesel::insert_into(shoppingcart)
            .values(NewShoppingCartEntity { total })
            .returning(shoppingcart_id)
            .get_result(&mut conn)
            .map_err(ErrorExecutingQueryToDatabase)?;

        let order: Vec<NewProductOrderEntity> = order.into_iter()
            .map(|it| NewProductOrderEntity {
                shoppingcart_id: cart_id,
                product_id: it.product_id,
                amount: it.amount,
            })
            .collect();

        diesel::insert_into(productorder)
            .values(order)
            .execute(&mut conn)
            .map_err(ErrorExecutingQueryToDatabase)?;

        Ok(NewOrder { id: cart_id })
    }

    pub async fn get_order(&self, id: i32) -> Result<ShoppingCart> {
        let mut conn = self.connection_pool.get_connection()?;

        let result: Vec<(ShoppingCartEntity, ProductOrderEntity)> = shoppingcart
            .inner_join(productorder)
            .filter(shoppingcart_id.eq(id))
            .select((ShoppingCartEntity::as_select(), ProductOrderEntity::as_select()))
            .load::<(ShoppingCartEntity, ProductOrderEntity)>(&mut conn)
            .map_err(ErrorExecutingQueryToDatabase)?;

        if result.is_empty() {
            Err(OrderNotFound { id })
        } else {
            let total = result.first().expect("Should have at least one element").0.total;
            let entries = result.into_iter()
                .map(|it| ProductOrder {
                    id,
                    shoppingcart_id: it.1.shoppingcart_id,
                    product_id: it.1.product_id,
                    amount: it.1.amount,
                })
                .collect();

            Ok(ShoppingCart {
                id,
                entries,
                total,
            })
        }
    }
}