package ru.vishnyakov

import ru.vishnyakov.ReservationEvent.*
import ru.vishnyakov.ReservationState.*
import com.tinder.StateMachine
import com.tinder.StateMachine.Companion.create
import java.time.Instant
import java.time.ZoneId
import java.util.*

data class Tenant(
    val id: UUID,
    val email: String
)

data class Flat(
    val id: UUID,
    val address: String,
    val tenantId: UUID
)

data class Reservation(
    val id: UUID,
    val time: Instant,
    val flatId: UUID,
    var status: ReservationStatus,
    var tenantId: UUID?
) {

    private fun toStates() = create<ReservationState, ReservationEvent, () -> Unit> {
        initialState(status.state)
        state<FREE> {
            on<RESERVE> {
                transitionTo(RESERVED) {
                    if(Instant.now().atZone(ZoneId.of("UTC")).plusHours(24) >= time.atZone(ZoneId.of("UTC"))) {
                        throw ValidationException("Current Tenant should be notified about reservation in at least 24 hours")
                    }
                }
            }
        }
        state<RESERVED> {
            on<APPROVE> {
                transitionTo(APPROVED)
            }
            on<REJECT> {
                transitionTo(REJECTED)
            }
            on<CANCEL> {
                transitionTo(FREE)
            }
        }
        state<APPROVED> {
            on<CANCEL> {
                transitionTo(FREE)
            }
        }
        state<REJECTED> {

        }
        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid ?: throw ValidationException("Can't change reservation status")
            validTransition.sideEffect?.invoke()
            status = ReservationStatus.values().first { it.state == validTransition.toState }
        }
    }

    fun reserve(tenant: Tenant, flat: Flat) {
        if(tenant.id == flat.tenantId || this.flatId != flat.id) {
            throw IllegalAccessException()
        }
        toStates().transition(RESERVE)
        tenantId = tenant.id
    }

    fun cancel(tenant: Tenant) {
        if(tenantId != tenant.id) {
            throw IllegalAccessException()
        }
        toStates().transition(CANCEL)
        tenantId = null
    }

    fun approve(tenant: Tenant, flat: Flat) {
        if(tenant.id != flat.tenantId || this.flatId != flat.id) {
            throw IllegalAccessException()
        }
        toStates().transition(APPROVE)
    }

    fun reject(tenant: Tenant, flat: Flat) {
        if(tenant.id != flat.tenantId || this.flatId != flat.id) {
            throw IllegalAccessException()
        }
        toStates().transition(REJECT)
    }

}

sealed class ReservationEvent {
    object RESERVE : ReservationEvent()
    object APPROVE : ReservationEvent()
    object REJECT : ReservationEvent()
    object CANCEL : ReservationEvent()
}

sealed class ReservationState {
    object FREE : ReservationState()
    object RESERVED : ReservationState()
    object APPROVED : ReservationState()
    object REJECTED : ReservationState()
}

enum class ReservationStatus(
    val state: ReservationState
) {
    FREE(ReservationState.FREE),
    RESERVED(ReservationState.RESERVED),
    APPROVED(ReservationState.APPROVED),
    REJECTED(ReservationState.REJECTED)
}




