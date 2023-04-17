// @generated automatically by Diesel CLI.

diesel::table! {
    product (id) {
        id -> Int4,
        description -> Nullable<Varchar>,
        name -> Nullable<Varchar>,
        price -> Float8,
    }
}

diesel::table! {
    productorder (id) {
        id -> Int4,
        amount -> Int4,
        product_id -> Int4,
        shoppingcart_id -> Int4,
    }
}

diesel::table! {
    shoppingcart (id) {
        id -> Int4,
        total -> Float8,
    }
}

diesel::joinable!(productorder -> product (product_id));
diesel::joinable!(productorder -> shoppingcart (shoppingcart_id));

diesel::allow_tables_to_appear_in_same_query!(
    product,
    productorder,
    shoppingcart,
);