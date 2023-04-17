use diesel::prelude::*;
use serde::Serialize;

#[derive(Queryable, Selectable, Serialize)]
#[diesel(table_name = crate::datasource::schema::product)]
pub struct ProductEntity {
    pub id: i32,
    pub name: Option<String>,
    pub description: Option<String>,
    pub price: f64,
}

#[derive(Queryable, Selectable, Serialize)]
#[diesel(table_name = crate::datasource::schema::shoppingcart)]
pub struct ShoppingCartEntity {
    pub id: i32,
    pub total: f64,
}

#[derive(Insertable, Serialize)]
#[diesel(table_name = crate::datasource::schema::shoppingcart)]
pub struct NewShoppingCartEntity {
    pub total: f64,
}

#[derive(Queryable, Selectable, Associations, Serialize)]
#[diesel(belongs_to(ShoppingCartEntity, foreign_key = shoppingcart_id))]
#[diesel(table_name = crate::datasource::schema::productorder)]
pub struct ProductOrderEntity {
    pub id: i32,
    pub shoppingcart_id: i32,
    pub product_id: i32,
    pub amount: i32,
}

#[derive(Insertable, Serialize)]
#[diesel(table_name = crate::datasource::schema::productorder)]
pub struct NewProductOrderEntity {
    pub shoppingcart_id: i32,
    pub product_id: i32,
    pub amount: i32,
}

