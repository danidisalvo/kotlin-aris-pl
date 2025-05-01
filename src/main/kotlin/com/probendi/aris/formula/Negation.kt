package com.probendi.aris.formula

import com.probendi.aris.MissingSymbolException

/**
 * Represents a negation (also known as NOT) operation.
 *
 * @param wff the argument
 *
 * © 2023-2025 Daniele Di Salvo
 */
data class Negation(val wff: WellFormedFormula) : WellFormedFormula {
    override fun determineFalsehoodConditions(): List<Condition> {
        return wff.determineTruthnessConditions()
    }

    override fun determineTruthnessConditions(): List<Condition> {
        return wff.determineFalsehoodConditions()
    }

    @Throws(MissingSymbolException::class)
    override fun valuate(values: Map<String, Boolean>): Boolean {
        return !wff.valuate(values)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Negation) return false

        if (wff != other.wff) return false

        return true
    }

    override fun hashCode(): Int {
        return wff.hashCode()
    }

    override fun toString(): String {
        return "¬$wff"
    }
}
