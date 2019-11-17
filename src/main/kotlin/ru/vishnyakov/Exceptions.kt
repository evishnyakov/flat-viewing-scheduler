package ru.vishnyakov

import java.util.*
import kotlin.reflect.KClass

class NotFoundExceptions(id: UUID, clazz : KClass<out Any>):
    RuntimeException("""${clazz.simpleName} with id = $id is not found""")

class IllegalAccessException: RuntimeException()

class ValidationException(message: String) : RuntimeException(message)