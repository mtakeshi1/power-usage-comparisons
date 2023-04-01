Rails.application.routes.draw do
  # Define your application routes per the DSL in https://guides.rubyonrails.org/routing.html

  # Defines the root path route ("/")
  # root "articles#index"

  # resources :products
  get 'products', to: 'products#index'
  get 'products/:id', to: 'products#show'
  get 'orders/:id', to: 'carts#show'
  post 'orders/:new', to: 'carts#new'
end
