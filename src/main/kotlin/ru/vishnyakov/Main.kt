package ru.vishnyakov

import com.fatboyindustrial.gsonjavatime.Converters.registerInstant
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.*

fun main() {
    val tenants = TenantsRepository()
    val flats = FlatsRepository()
    val reservations = ReservationsRepository()

    val notificationService = NotificationService()
    val tenantsService = TenantsService(tenants)
    val reservationService = ReservationService(reservations, tenants, flats, notificationService)
    val flatsService = FlatsService(flats, tenants, reservationService)

    val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson {
                registerInstant(this)
                setPrettyPrinting()
            }
        }
        install(StatusPages) {
            exception<NotFoundExceptions> { cause ->
                call.respond(HttpStatusCode.NotFound, cause.message!!)
            }
            exception<IllegalAccessException> {
                call.respond(HttpStatusCode.Forbidden)
            }
            exception<ValidationException> { cause ->
                call.respond(HttpStatusCode.BadRequest, cause.message!!)
            }
            exception<Throwable> {
                call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            }
        }
        routing {
            post("/tenants") {
                val command = call.receive<CreateTenantCommand>()
                call.respond(tenantsService.create(command.email))
            }
            post("/flats") {
                val command = call.receive<CreateFlatCommand>()
                call.respond(flatsService.create(command.address, command.tenantId))
            }
            put("/reservations/reserve") {
                val command = call.receive<ModifyReservationCommand>()
                call.respond(reservationService.reserve(command.reservationId, command.tenantId))
            }
            put("/reservations/approve") {
                val command = call.receive<ModifyReservationCommand>()
                call.respond(reservationService.approve(command.reservationId, command.tenantId))
            }
            put("/reservations/reject") {
                val command = call.receive<ModifyReservationCommand>()
                call.respond(reservationService.reject(command.reservationId, command.tenantId))
            }
            put("/reservations/cancel") {
                val command = call.receive<ModifyReservationCommand>()
                call.respond(reservationService.cancel(command.reservationId, command.tenantId))
            }
            get("/reservations/{flatId}") {
                val flatId = UUID.fromString(call.parameters["flatId"]!!)
                call.respond(reservationService.findByFlat(flatId))
            }
        }
    }
    server.start(wait = true)
}

data class CreateTenantCommand(
    val email: String
)

data class CreateFlatCommand(
    val address: String,
    val tenantId: UUID
)

data class ModifyReservationCommand(
    val reservationId: UUID,
    var tenantId: UUID
)