package com.probendi.aris.formula

import com.probendi.aris.MissingSymbolException

/**
 * Represents a conditional (also known as material conditional or material implication) operation.
 *
 * @param wff1 the first argument
 * @param wff2 the second argument
 *
 * © 2023-2025 Daniele Di Salvo
 */
data class Conditional(val wff1: WellFormedFormula, val wff2: WellFormedFormula) : WellFormedFormula {
    override fun determineFalsehoodConditions(): List<Condition> {
        return listOf(BinaryCondition(wff1.determineTruthnessConditions()[0], wff2.determineFalsehoodConditions()[0]))
    }

    override fun determineTruthnessConditions(): List<Condition> {
        return listOf(
            BinaryCondition(wff1.determineFalsehoodConditions()[0], wff2.determineFalsehoodConditions()[0]),
            BinaryCondition(wff1.determineFalsehoodConditions()[0], wff2.determineTruthnessConditions()[0]),
            BinaryCondition(wff1.determineTruthnessConditions()[0], wff2.determineTruthnessConditions()[0])
        )
    }

    @Throws(MissingSymbolException::class)
    override fun valuate(values: Map<String, Boolean>): Boolean {
        return !wff1.valuate(values) || wff2.valuate(values)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Conditional) return false

        if (wff1 != other.wff1) return false
        if (wff2 != other.wff2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = wff1.hashCode()
        result = 31 * result + wff2.hashCode()
        return result
    }

    override fun toString(): String {
        return "($wff1 → $wff2)"
    }
}
