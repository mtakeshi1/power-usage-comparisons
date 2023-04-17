use thiserror::Error;

#[derive(Error, Debug)]
pub enum ServiceError {
    #[error("Product id: {id} not found.")]
    ProductNotFound { id: i32 },
    #[error("Order id: {id} not found.")]
    OrderNotFound { id: i32 },
    #[error("Error connecting to database.")]
    ErrorConnectingToDatabase,
    #[error("Error executing query.")]
    ErrorExecutingQueryToDatabase(#[from] diesel::result::Error),
}