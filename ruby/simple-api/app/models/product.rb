class Product < ActiveRecord::Base
    self.table_name = "product"

    def small
        {:id => self.id, :name => self.name}
    end


end
