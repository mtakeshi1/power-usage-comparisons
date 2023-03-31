const ProductService = (pgPool) => {
    const productService = {};

    productService.getAllProducts = async () => {
        const { rows } = await pgPool.query('SELECT * FROM product');
        return rows;
    };

    productService.createProduct = async (name, description, price) => {
        const { rows } = await pgPool.query(
            'INSERT INTO product (name, description, price) VALUES ($1, $2, $3) RETURNING *',
            [name, description, price]
        );
        return rows[0];
    };

    productService.getProductById = async (productId) => {
        const { rows } = await pgPool.query('SELECT * FROM product WHERE id = $1', [productId]);
        return rows[0];
    };

    return productService;
}

module.exports = ProductService;
