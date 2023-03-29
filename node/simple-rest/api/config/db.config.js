// TODO (using sequelize)

/*
const { Sequelize, Model, DataTypes } = require("sequelize");
const logger = require('../logger/api.logger');

const connect = () => {

    const hostName = process.env.PG_HOST;
    const port = process.env.PG_PORT;
    const userName = process.env.PG_USER;
    const password = process.env.PG_PWD;
    const database = process.env.PG_DATABASE;
    const dialect = process.env.DIALECT;

    const sequelize = new Sequelize(database, userName, password, {
        host: hostName,
        port: port,
        dialect: dialect,
        operatorsAliases: false,
        pool: {
            max: 10,
            min: 0,
            acquire: 20000,
            idle: 5000
        }
    });

    const db = {};
    db.Sequelize = Sequelize;
    db.sequelize = sequelize;

    db.products = require("../model/product")(sequelize, DataTypes, Model);

    return db;
}

module.exports = {
    connect
}
*/