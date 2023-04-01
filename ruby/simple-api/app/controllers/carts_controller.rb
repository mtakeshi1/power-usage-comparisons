class CartsController < ActionController::API

    # GET /orders/id
    def show
        @order = Cart.find(params[:id])
        @products = (ProductOrder.where "shoppingcart_id = ?", @order.id).map{ |p| p.small } 
        
        render json: {:id => @order.id, :total => @order.total, :entries => @products}
    end

    # POST /orders/new
    def new
        total = 0
        array = JSON.parse request.body.read
        array.each { |e| 
            p = Product.find(e["productId"])
            total += e["amount"] * p.price
        }
        order = Cart.create(total: total )
        puts(order.id)
        array.each { |e| 
            ProductOrder.create(shoppingcart_id: order.id, product_id: e["productId"], amount: e["amount"])
        }
        render plain: order.id.to_s
    end

end
