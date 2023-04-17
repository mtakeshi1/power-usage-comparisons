use core::fmt::Debug;

use axum::{Json, Router};
use axum::body::Body;
use axum::extract::{Path, State};
use axum::http::StatusCode;
use axum::response::{IntoResponse, Response};
use axum::routing::get;
use axum::routing::post;
use serde::Serialize;
use serde_json::json;

use ServiceError::OrderNotFound;

use crate::AppState;
use crate::core::{OrderService, ProductService};
use crate::core::error::ServiceError;
use crate::core::error::ServiceError::ProductNotFound;
use crate::core::models::{NewOrder, Product, ProductAmount, ProductInfo};
use crate::core::Result;

pub fn product_routes() -> Router<AppState, Body> {
    Router::new()
        .route("/products", get(products_list))
        .route("/products/:id", get(product_get))
}

pub fn order_routes() -> Router<AppState, Body> {
    Router::new()
        .route("/orders/new", post(new_order))
        .route("/orders/:id", get(order_get))
}

async fn products_list(
    State(product_service): State<ProductService>
) -> Result<Json<Vec<ProductInfo>>> {
    let products = product_service.list_products().await?
        .into_iter()
        .map(|it| ProductInfo { id: it.id, name: it.name })
        .collect();

    Ok(Json(products))
}

async fn product_get(
    State(product_service): State<ProductService>,
    Path(id): Path<i32>,
) -> Result<Json<Product>> {
    if let Some(product) = product_service.get_product(id).await? {
        Ok(Json(product))
    } else {
        Err(ProductNotFound { id })
    }
}

async fn new_order(
    State(order_service): State<OrderService>,
    Json(order): Json<Vec<ProductAmount>>,
) -> Result<Json<NewOrder>> {
    Ok(Json(order_service.create_order(order).await?))
}

#[derive(Serialize, Debug)]
pub struct Order {
    id: i32,
    entries: Vec<OrderEntry>,
    total: f64,
}

#[derive(Serialize, Debug)]
pub struct OrderEntry {
    product_id: i32,
    amount: i32,
}

async fn order_get(
    State(order_service): State<OrderService>,
    Path(id): Path<i32>,
) -> Result<Json<Order>> {
    let result = order_service.get_order(id).await?;

    Ok(Json(Order {
        id: result.id,
        entries: result.entries.into_iter()
            .map(|it| OrderEntry { product_id: it.product_id, amount: it.amount })
            .collect(),
        total: result.total,
    }))
}

impl IntoResponse for ServiceError {
    fn into_response(self) -> Response {
        let error_body = json!({"error": self.to_string()});
        let status_code = match self {
            ProductNotFound { .. } => StatusCode::NOT_FOUND,
            OrderNotFound { .. } => StatusCode::NOT_FOUND,
            _ => StatusCode::INTERNAL_SERVER_ERROR,
        };
        let mut response = (status_code, Json(error_body)).into_response();
        response.extensions_mut().insert(self);
        response
    }
}
