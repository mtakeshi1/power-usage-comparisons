use std::env;

use diesel::prelude::*;
use diesel::r2d2::{ConnectionManager, Pool, PooledConnection};

use crate::core::error::ServiceError::ErrorConnectingToDatabase;
use crate::core::Result;

pub mod schema;
pub mod models;
pub mod products_repository;
pub mod orders_repository;

#[derive(Clone)]
pub struct ConnectionPool(Pool<ConnectionManager<PgConnection>>);

pub fn establish_connection() -> ConnectionPool {
    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
    let max_pool_size = env::var("MAX_POOL_SIZE").unwrap_or("10".to_string());

    let manager = ConnectionManager::<PgConnection>::new(database_url);
    ConnectionPool(Pool::builder()
        .max_size(max_pool_size.parse::<u32>().expect("MAX_POOL_SIZE should be a positive integer"))
        .test_on_check_out(true)
        .build(manager)
        .expect("Could not build connection pool"))
}

impl ConnectionPool {
    pub fn get_connection(&self) -> Result<PooledConnection<ConnectionManager<PgConnection>>> {
        self.0.get().map_err(|_| ErrorConnectingToDatabase)
    }
}