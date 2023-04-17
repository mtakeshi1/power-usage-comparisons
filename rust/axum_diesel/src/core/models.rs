use serde::Deserialize;
use serde::Serialize;

#[derive(Serialize, Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct Product {
    pub id: i32,
    pub name: Option<String>,
    pub description: Option<String>,
    pub price: f64,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ProductInfo {
    pub id: i32,
    pub name: Option<String>,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ProductAmount {
    pub product_id: i32,
    pub amount: i32,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ShoppingCart {
    pub id: i32,
    pub entries: Vec<ProductOrder>,
    pub total: f64,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct NewOrder {
    pub id: i32,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ProductOrder {
    pub id: i32,
    pub shoppingcart_id: i32,
    pub product_id: i32,
    pub amount: i32,
}
