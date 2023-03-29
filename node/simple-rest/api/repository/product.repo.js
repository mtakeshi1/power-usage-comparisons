// TODO (using sequelize)

/*
const { connect } = require('../config/db.config');

class ProductRepo {

    db = {};

    constructor() {
        this.db = connect();
        // For Development
        this.db.sequelize.sync({ force: true }).then(() => {
            console.log("Drop and re-sync db.");
        });
    }

    async getProducts() {
        try {
            const products = await this.db.products.findAll();
            console.log('products:', products);
            return products;
        } catch (err) {
            console.log(err);
            return [];
        }
    }

    async createProduct(product) {
        let data = {};
        try {
            // product.createdate = new Date().toISOString();
            data = await this.db.products.create(product);
        } catch (err) {
            console.error('Error:' + err);
        }
        return data;
    }

    async updateProduct(product) {
        let data = {};
        try {
            // product.updateddate = new Date().toISOString();
            data = await this.db.products.update({ ...product }, {
                where: {
                    id: product.id
                }
            });
        } catch (err) {
            console.error('Error:' + err);
        }
        return data;
    }

    async deleteProduct(productId) {
        let data = {};
        try {
            data = await this.db.products.destroy({
                where: {
                    id: productId
                }
            });
        } catch (err) {
            console.error('Error:' + err);
        }
        // return data;
        return { status: `${data.deletedCount > 0 ? true : false}` };
    }
}

module.exports = new ProductRepo();
*/