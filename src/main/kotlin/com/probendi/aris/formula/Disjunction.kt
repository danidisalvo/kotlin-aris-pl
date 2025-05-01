package com.probendi.aris.formula

import com.probendi.aris.MissingSymbolException

/**
 * Represents a disjunction (also known as OR) operation.
 *
 * @param wff1 the first argument
 * @param wff2 the second argument
 *
 * © 2023-2025 Daniele Di Salvo
 */
data class Disjunction(val wff1: WellFormedFormula, val wff2: WellFormedFormula) : WellFormedFormula {
    override fun determineFalsehoodConditions(): List<Condition> {
        return listOf(
            BinaryCondition(wff1.determineFalsehoodConditions()[0], wff2.determineFalsehoodConditions()[0])
        )
    }

    override fun determineTruthnessConditions(): List<Condition> {
        return listOf(
            BinaryCondition(wff1.determineTruthnessConditions()[0], wff2.determineFalsehoodConditions()[0]),
            BinaryCondition(wff1.determineFalsehoodConditions()[0], wff2.determineTruthnessConditions()[0]),
            BinaryCondition(wff1.determineTruthnessConditions()[0], wff2.determineTruthnessConditions()[0])
        )
    }

    @Throws(MissingSymbolException::class)
    override fun valuate(values: Map<String, Boolean>): Boolean {
        return wff1.valuate(values) || wff2.valuate(values)
    }

    override fun toString(): String {
        return "($wff1 ∨ $wff2)"
    }
}
