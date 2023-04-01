class ProductsController < ApplicationController

    def index
        @products = Product.all.map{ |p| p.small } 
        render json: @products
    end

    # GET /products/:id
    def show
        @product = Product.find(params[:id])
        render json: @product
    end

end
