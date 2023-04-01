class ProductOrder < ActiveRecord::Base
    self.table_name = "productorder"

    # belongs_to :cart
    # belongs_to :product

    def small
        {:productId => self.product_id, :amount => self.amount}
    end


end
