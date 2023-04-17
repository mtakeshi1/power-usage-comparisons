use std::net::SocketAddr;

use axum::{middleware, Router};
use axum::extract::FromRef;
use axum::http::{Method, Uri};
use axum::response::Response;
use dotenvy::dotenv;
use tracing::error;

use crate::core::{OrderService, ProductService};
use crate::core::error::ServiceError;
use crate::datasource::establish_connection;
use crate::datasource::orders_repository::OrderRepository;
use crate::datasource::products_repository::ProductRepository;
use crate::web::{order_routes, product_routes};

pub mod core;
pub mod datasource;
pub mod web;

#[tokio::main]
async fn main() {
    dotenv().ok();
    tracing_subscriber::fmt::init();

    let connection_pool = establish_connection();
    let product_repository = ProductRepository { connection_pool: connection_pool.clone() };
    let order_repository = OrderRepository { connection_pool: connection_pool.clone() };
    let app_state = AppState {
        product_service: ProductService { product_repository: product_repository.clone() },
        order_service: OrderService { order_repository, product_repository },
    };

    let app = Router::new()
        .merge(product_routes())
        .merge(order_routes())
        .layer(middleware::map_response(log_error))
        .with_state(app_state);

    let addr = SocketAddr::from(([127, 0, 0, 1], 8080));
    tracing::info!("Server started on {}", addr);
    axum::Server::bind(&addr)
        .serve(app.into_make_service())
        .await
        .unwrap();
}

async fn log_error(
    uri: Uri,
    req_method: Method,
    response: Response,
) -> Response {
    if let Some(error) = response.extensions().get::<ServiceError>() {
        error!("Error '{}' executing API {} {}", error.to_string() , req_method, uri);
    }
    response
}


#[derive(Clone)]
pub struct AppState {
    product_service: ProductService,
    order_service: OrderService,
}

impl FromRef<AppState> for ProductService {
    fn from_ref(app_state: &AppState) -> ProductService {
        app_state.product_service.clone()
    }
}

impl FromRef<AppState> for OrderService {
    fn from_ref(app_state: &AppState) -> OrderService {
        app_state.order_service.clone()
    }
}
