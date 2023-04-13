package mtakeshi1.githut.io.plugins

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import mtakeshi1.githut.io.model.DataAccess
import mtakeshi1.githut.io.routes.orderRouting
import mtakeshi1.githut.io.routes.productRouting

fun Application.configureRouting(dataAccess: DataAccess) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        productRouting(dataAccess)
        orderRouting(dataAccess)
    }

}
