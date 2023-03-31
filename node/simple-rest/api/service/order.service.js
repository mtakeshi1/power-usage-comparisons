const createProductService = require('./product.service');

const OrderService = (pgPool) => {
    const orderService = {};

    const productService = createProductService(pgPool);
    
    const createCart = async () => {
        const { rows } = await pgPool.query(
            'INSERT INTO shoppingcart (total) VALUES ($1) RETURNING *',
            [0]
        );
        return rows[0];
    };
    
    const createProductOrder = async (productId, amount, cartId) => {
        await pgPool.query(
            'INSERT INTO productorder (product_id, amount, shoppingcart_id) VALUES ($1, $2, $3)',
            [productId, amount, cartId]
        );
    };
    
    const updateCart = async (id, total) => {
        await pgPool.query(
            'UPDATE shoppingcart SET total = $2 where id = ($1)',
            [id, total]
        );
    };

    orderService.createOrder = async (orderEntries) => {
        const cart = await createCart();
        let total = 0.0;
        for (const orderEntry of orderEntries) {
            const product = await productService.getProductById(orderEntry.productId);
            await createProductOrder(product.id, orderEntry.amount, cart.id);
            total += orderEntry.amount * product.price;
        };
        await updateCart(cart.id, total);
        return cart.id;
    };

    const getCartById = async (cartId) => {
      const { rows } = await pgPool.query('SELECT * FROM shoppingcart WHERE id = $1', [cartId]);
      return rows[0];
    };
    
    const getProductOrdersByCartId = async (cartId) => {
      const { rows } = await pgPool.query('SELECT product_id, amount FROM productorder WHERE shoppingcart_id = $1', [cartId]);
      return rows;
    };

    orderService.getOrderById = async (orderId) => {
        const order = {};
        const cart = await getCartById(orderId);
        order.id = cart.id;
        order.total = cart.total;
        order.entries = await getProductOrdersByCartId(cart.id);
        return order;
    };

    return orderService;
}

module.exports = OrderService;
