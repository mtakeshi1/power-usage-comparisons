package mtakeshi1.githut.io.model

import java.sql.ResultSet
import java.sql.Statement
import javax.sql.DataSource

class JDBCDataAccess(private val dataSource: DataSource) : DataAccess {

    fun interface ResultSetMapper<T> {
        fun apply(rs: ResultSet): T
    }

    private fun <T> findMany(query: String, resultMapper: ResultSetMapper<T>): List<T> {
        val r = mutableListOf<T>()
        dataSource.connection.use {
            it.createStatement().use { st ->
                st.execute(query)
                st.resultSet.use { rs ->
                    while (rs.next()) {
                        r.add(resultMapper.apply(rs))
                    }
                }
            }
        }
        return r
    }

    override fun listProducts(): List<Models.SmallProduct> {
        return findMany("select id,name from product") {rs -> Models.SmallProduct(rs.getInt(1), rs.getString(2))}
    }

    override fun productWithId(id: Int): Models.Product {
        // yes I should use exposed or at least prepared statement but alas
        return findMany("select id, name, description, price from product where id = $id")
            { Models.Product(it.getInt(1), it.getString(2), it.getString(3), it.getDouble(4)) }[0]
    }

    override fun newOrder(items: List<Models.ShoppingCartEntry>): Models.ShoppingCart {
        val totalPrice = items.sumOf { productWithId(it.productId).price * it.amount }
        val id = dataSource.connection.use {
            val orderId:Int = it.prepareStatement("insert into shoppingcart (total) values (?)", Statement.RETURN_GENERATED_KEYS).use { ps ->
                ps.setDouble(1, totalPrice)
                if(ps.executeUpdate() > 0 && ps.generatedKeys.next()) {
                    ps.generatedKeys.getInt(1)
                } else -1
            }
            it.prepareStatement("insert into productorder(amount, product_id, shoppingcart_id) values(?, ?, ?)").use { ps ->
                items.forEach { item ->
                    ps.setInt(1, item.amount)
                    ps.setInt(2, item.productId)
                    ps.setInt(3, orderId)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
            orderId
        }
        return Models.ShoppingCart(id, totalPrice, items)
    }

    override fun orderWithId(id: Int): Models.ShoppingCart {
        val orders = findMany("select c.id,c.total,i.product_id, i.amount from shoppingcart c join productorder i on c.id = i.shoppingcart_id where c.id = $id") { rs ->
                Models.ShoppingCart(rs.getInt(1), rs.getDouble(2), listOf(Models.ShoppingCartEntry(rs.getInt(3), rs.getInt(4))))
        }
        val items = orders.flatMap { it.entries }
        return Models.ShoppingCart(orders[0].id, orders[0].total, items)
    }
}