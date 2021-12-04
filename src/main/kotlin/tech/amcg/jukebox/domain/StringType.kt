package tech.amcg.jukebox.domain

import java.lang.IllegalArgumentException

abstract class StringType<T : StringType<T>> protected constructor(value: String): Comparable<T> {

    val value: String

    init {
        if(value.isBlank()) {
            throw IllegalArgumentException("Value must not be blank or null")
        }
        this.value = value
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as StringType<*>?
        return this.value == that!!.value
    }

    override fun toString(): String {
        return this.value
    }

    override fun hashCode(): Int {
        return this.value.hashCode()
    }

    override fun compareTo(other: T): Int {
        return this.value.compareTo(other.value)
    }
}