package com.probendi.aris.formula

import com.probendi.aris.MissingSymbolException

/**
 * A propositional logic argument.
 *
 * © 2023-2025 Daniele Di Salvo
 */
class Argument(conclusion: WellFormedFormula?, vararg premises: WellFormedFormula) : WellFormedFormula {
    var premises: MutableList<WellFormedFormula> = premises.toMutableList()

    var conclusion: WellFormedFormula? = conclusion
        set(value) {
            requireNotNull(value) { "Conclusion cannot be null" }
            field = value
        }

    /**
     * Add the given premise to this argument.
     *
     * @param premise the premise to be added
     */
    fun addPremise(premise: WellFormedFormula) {
        premises.add(premise)
    }

    val isTautology: Boolean
        /**
         * Returns `true` if this argument is a tautology.
         *
         * @return `true` if the given well-formed formula is a tautology
         */
        get() {
            if (conclusion != null || premises.size != 1) {
                val msg =
                    "this method can be only invoked on arguments without conclusion and exactly one premises"
                throw UnsupportedOperationException(msg)
            }

            val wff = premises[0]
            val vars: MutableSet<String> = mutableSetOf()
            for (condition in wff.determineTruthnessConditions()) {
                for (atomicCondition in getAtomicConditions(condition)) {
                    vars.add((atomicCondition as AtomicCondition).value)
                }
            }

            for (values in generateTruthTable(vars)) {
                try {
                    if (!wff.valuate(values)) {
                        return false
                    }
                } catch (_: MissingSymbolException) { // ignored, for 'values' is always complete
                }
            }

            return true
        }


    @get:Throws(MissingSymbolException::class)
    val isValid: Boolean
        /**
         * Returns `true` if this argument is valid, i.e., if there is a relevant valuation which makes the premises
         * and the negated conclusion all true.
         *
         * @return `true` if this argument is valid
         * @throws MissingSymbolException if a symbol is not found in the 'values' lookup table
         */
        get() {
            val formulae: MutableList<WellFormedFormula> = mutableListOf()
            formulae.add(Negation(requireNotNull(conclusion)))
            formulae.addAll(premises)

            val vars: MutableSet<String> = mutableSetOf()
            for (wff in formulae) {
                for (condition in wff.determineTruthnessConditions()) {
                    for (atomicCondition in getAtomicConditions(condition)) {
                        vars.add((atomicCondition as AtomicCondition).value)
                    }
                }
            }

            // iterate over all possible input variables configurations
            for (values in generateTruthTable(vars)) {
                var valid = true
                // valuate all formulae for a given input variables configuration
                for (wff in formulae) {
                    if (!wff.valuate(values)) {
                        // if one formula is false, then move on to the next input variables configuration
                        valid = false
                        break
                    }
                }
                // an input variables configuration which makes the premises and the negated conclusion all true was found
                if (valid) {
                    return false
                }
            }

            // no configuration was found, hence the argument is valid
            return true
        }

    override fun determineFalsehoodConditions(): List<Condition> {
        throw UnsupportedOperationException()
    }

    override fun determineTruthnessConditions(): List<Condition> {
        throw UnsupportedOperationException()
    }

    @Throws(MissingSymbolException::class)
    override fun valuate(values: Map<String, Boolean>): Boolean {
        var p = true
        for (premise in premises) {
            p = premise.valuate(values)
            if (!p) break
        }
        return p and conclusion!!.valuate(values)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Argument) return false

        if (premises != other.premises) return false
        if (conclusion != other.conclusion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = premises.hashCode()
        result = 31 * result + (conclusion?.hashCode() ?: 0)
        return result
    }

    override fun toString() = if (premises.isEmpty()) "" else "${premises.joinToString(", ")} ∴ $conclusion"

    private fun generateTruthTable(vars: Set<String>): List<Map<String, Boolean>> {
        val varsList = vars.toList()
        val result: MutableList<MutableMap<String, Boolean>> = mutableListOf()
        val n = varsList.size
        var i = 0
        while (n > 0 && i != (1 shl n)) {
            val sb = i.toString(2).padStart(n, '0')
            val map = mutableMapOf<String, Boolean>()
            for (j in 0 until n) {
                map[varsList[j]] = sb[j] == '1'
            }
            result.add(map)
            i++
        }
        return result
    }

    private fun getAtomicConditions(condition: Condition?): MutableList<Condition> {
        val atomicConditions: MutableList<Condition> = mutableListOf()
        if (condition is AtomicCondition) {
            atomicConditions.add(condition)
        } else {
            val b = condition as BinaryCondition
            atomicConditions.addAll(getAtomicConditions(b.c1))
            atomicConditions.addAll(getAtomicConditions(b.c2))
        }
        return atomicConditions
    }
}
