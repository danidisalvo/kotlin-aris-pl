package com.probendi.aris.formula

import com.probendi.aris.And
import com.probendi.aris.Atom
import com.probendi.aris.Comma
import com.probendi.aris.LBracket
import com.probendi.aris.MissingSymbolException
import com.probendi.aris.Not
import com.probendi.aris.Or
import com.probendi.aris.RBracket
import com.probendi.aris.Therefore
import com.probendi.aris.Token
import com.probendi.aris.UnexpectedSymbolException
import java.util.Queue

/**
 * A well-formed formula of a propositional logic language.
 *
 * © 2023-2025 Daniele Di Salvo
 */
interface WellFormedFormula {
    /**
     * Returns the conditions which make this formula false.
     *
     * @return the conditions which make this formula false
     */
    fun determineFalsehoodConditions(): List<Condition>

    /**
     * Returns the conditions which make this formula true.
     *
     * @return the conditions which make this formula true
     */
    fun determineTruthnessConditions(): List<Condition>

    /**
     * Valuates this formula.
     *
     * @param values a map containing the symbols' truth-values
     * @return `true` if this formula preserves the truth
     * @throws IllegalArgumentException if values is `null`
     * @throws MissingSymbolException  if this formula could not be evaluated because a symbol has no value
     */
    @Throws(MissingSymbolException::class)
    fun valuate(values: Map<String, Boolean>): Boolean

    companion object {
        /**
         * Parses a well-formed formula from the given tokens.
         *
         * @param tokens the tokens to be parsed
         * @return a well-formed formula
         * @throws UnexpectedSymbolException if an unexpected symbol was found
         */
        @Throws(UnexpectedSymbolException::class)
        fun parse(tokens: Queue<Token?>): WellFormedFormula? {
            var token = tokens.peek()
            if (token != null) {
                if (token is Comma || token is RBracket || token is Therefore) {
                    return null
                }
                token = tokens.remove()
                when (token) {
                    is Atom -> {
                        return AtomicCondition(token.value)
                    }

                    is Not -> {
                        return Negation(requireNotNull(parse(tokens)))
                    }

                    is LBracket -> {
                        return parseBinary(tokens)
                    }
                }
            }
            throw UnexpectedSymbolException("")
        }

        /**
         * Parses a well-formed binary formula from the given tokens.
         *
         * @param tokens the tokens to be parsed
         * @return a well-formed binary formula
         * @throws UnexpectedSymbolException if an unexpected symbol was found
         */
        @Throws(UnexpectedSymbolException::class)
        fun parseBinary(tokens: Queue<Token?>): WellFormedFormula {
            val wff1 = requireNotNull(parse(tokens))
            val operator = tokens.remove()
            val wff2 = requireNotNull(parse(tokens))
            val formula = when (operator) {
                is And -> {
                    Conjunction(wff1, wff2)
                }

                is Or -> {
                    Disjunction(wff1, wff2)
                }

                else -> {
                    Conditional(wff1, wff2)
                }
            }
            tokens.remove()
            return formula
        }
    }
}
