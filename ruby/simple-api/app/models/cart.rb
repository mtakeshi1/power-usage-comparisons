class Cart < ActiveRecord::Base
    self.table_name = "shoppingcart"
    has_many :product_orders, dependent: :destroy
    # has_many :products
end
