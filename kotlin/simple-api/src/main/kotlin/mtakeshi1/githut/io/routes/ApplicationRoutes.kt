package mtakeshi1.githut.io.routes

import io.ktor.server.routing.*
import mtakeshi1.githut.io.model.DataAccess
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.*
import mtakeshi1.githut.io.model.Models

fun Route.productRouting(dataAccess: DataAccess) {
    route("/products") {
        get { call.respond(dataAccess.listProducts()) }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            call.respond(dataAccess.productWithId(Integer.parseInt(id)))
        }
    }
}

fun Route.orderRouting(dataAccess: DataAccess) {
    route("/orders") {
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            call.respond(dataAccess.orderWithId(Integer.parseInt(id)))
        }
        post("new") {
            val entries = call.receive<List<Models.ShoppingCartEntry>>()
            call.respondText(dataAccess.newOrder(entries).id.toString())
        }
    }
}