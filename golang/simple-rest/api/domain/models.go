package domain

type Product struct {
	ID          int     `json:"id"`
	Name        string  `json:"name"`
	Description string  `json:"description"`
	Price       float64 `json:"price"`
}

type OrderEntry struct {
	ProductId int     `json:"productId"`
	Amount    float64 `json:"amount"`
}

type Order struct {
	ID      int           `json:"id"`
	Total   float64       `json:"total"`
	Entries *[]OrderEntry `json:"entries"`
}

type Cart struct {
	ID    int     `json:"id"`
	Total float64 `json:"total"`
}

type ProductOrder struct {
	ID             int `json:"id"`
	Amount         int `json:"amount"`
	ProductId      int `json:"productId"`
	ShoppingcartId int `json:"shoppingcartId"`
}
