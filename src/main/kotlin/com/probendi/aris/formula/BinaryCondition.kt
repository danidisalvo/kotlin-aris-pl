package com.probendi.aris.formula

/**
 * A binary condition.
 *
 * © 2023-2025 Daniele Di Salvo
 */
data class BinaryCondition(val c1: Condition, val c2: Condition) : Condition {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BinaryCondition) return false

        if (c1 != other.c1) return false
        if (c2 != other.c2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = c1.hashCode()
        result = 31 * result + c2.hashCode()
        return result
    }

    override fun toString(): String {
        return "($c1, $c2)"
    }
}
