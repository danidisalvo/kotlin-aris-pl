package com.probendi.aris.formula

import com.probendi.aris.Atom
import com.probendi.aris.MissingSymbolException

/**
 * An atomic condition.
 *
 * © 2023-2025 Daniele Di Salvo
 */
class AtomicCondition(value: String) : Atom(value), Condition, WellFormedFormula {
    var boolValue: Boolean? = null

    /**
     * Sets this atomic condition to `false`.
     *
     * @return this atomic condition.
     */
    fun setFalse(): AtomicCondition {
        boolValue = false
        return this
    }

    /**
     * Sets this atomic condition to `true`.
     *
     * @return this atomic condition.
     */
    fun setTrue(): AtomicCondition {
        boolValue = true
        return this
    }

    override fun determineFalsehoodConditions(): List<Condition> {
        val atomicCondition = AtomicCondition(value)
        atomicCondition.setFalse()
        return listOf(atomicCondition)
    }

    override fun determineTruthnessConditions(): List<Condition> {
        val atomicCondition = AtomicCondition(value)
        atomicCondition.setTrue()
        return listOf(atomicCondition)
    }

    @Throws(MissingSymbolException::class)
    override fun valuate(values: Map<String, Boolean>): Boolean {
        if (!values.containsKey(value)) {
            throw MissingSymbolException(value)
        }
        return values[value]!!
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AtomicCondition) return false

        if (!super<Atom>.equals(other)) return false
        if (boolValue != other.boolValue) return false

        return true
    }

    override fun hashCode(): Int {
        return boolValue?.hashCode() ?: 0
    }

    override fun toString(): String {
        return if (boolValue == null) value else ("$value=$boolValue")
    }

}
