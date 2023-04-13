package mtakeshi1.githut.io

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mtakeshi1.githut.io.model.JDBCDataAccess
import mtakeshi1.githut.io.model.MockDataAccess
import mtakeshi1.githut.io.plugins.configureRouting
import mtakeshi1.githut.io.plugins.configureSerialization
import org.postgresql.Driver
import java.io.File
import java.io.FileReader
import java.util.*
import javax.sql.DataSource

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun dataSource(env: Map<String, String>): DataSource {
    return HikariDataSource(HikariConfig().apply {
        driverClassName = Driver::class.java.name
        jdbcUrl = env["quarkus.datasource.jdbc.url"]
        username = env["quarkus.datasource.username"]
        password = env["quarkus.datasource.password"]
        maximumPoolSize = 32
        minimumIdle = 8
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })
}

fun Application.module() {
    val dataAccess = if(File(".env").exists()) {
        val props = Properties()
        props.load(FileReader(".env"))
        val defaults = System.getenv()
        val copy = HashMap<String, String>(defaults)
        props.entries.forEach { entry -> copy.put(entry.key.toString(), entry.value.toString()) }
        JDBCDataAccess(dataSource(copy))
    } else if (System.getenv("quarkus.datasource.jdbc.url") != null) {
        JDBCDataAccess(dataSource(System.getenv()))
    } else {
        MockDataAccess
    }


    configureSerialization()
//    configureDatabases()
    configureRouting(dataAccess)

}
