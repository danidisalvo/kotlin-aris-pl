package com.probendi.aris.formula

import com.probendi.aris.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.*
import java.util.stream.Stream

val p = AtomicCondition("P")
val q = AtomicCondition("Q")
val r = AtomicCondition("R")

val pFalse: Condition = AtomicCondition("P").setFalse()
val pTrue: Condition = AtomicCondition("P").setTrue()
val qFalse: Condition = AtomicCondition("Q").setFalse()
val qTrue: Condition = AtomicCondition("Q").setTrue()
val rFalse: Condition = AtomicCondition("R").setFalse()

val notP = Negation(p)
val notQ = Negation(q)
val notR = Negation(r)

val disNotPNotQ = Disjunction(notP, notQ)

class WellFormattedFormulaTest {

    @ParameterizedTest
    @ArgumentsSource(DetermineTruthConditionsArgumentsProvider::class)
    fun testDetermineTruthConditions(wff: WellFormedFormula, expected: List<Condition>) {
        val conditions = wff.determineTruthnessConditions()
        println(conditions)
        assertEquals(expected, conditions)
    }

    @ParameterizedTest
    @ArgumentsSource(ValuationArgumentsProvider::class)
    @Throws(ArisException::class)
    fun testValuation(wff: WellFormedFormula, values: MutableMap<String, Boolean>, expected: Boolean) {
        assertEquals(expected, wff.valuate(values))
    }

    @org.junit.jupiter.api.Test
    fun testMissingSymbolException() {
        val wff: WellFormedFormula = Negation(AtomicCondition("P"))
        val values = mapOf("Q" to true)
        val e = assertThrows<MissingSymbolException> { wff.valuate(values) }
        assertEquals("MissingSymbolException", e::class.simpleName)
        assertEquals("P", e.message)
    }

    @org.junit.jupiter.api.Test
    @Throws(ArisException::class)
    fun testEmbeddedOperators() {
        val and = And()
        val p = Atom("P")
        val q = Atom("Q")
        val lBracket = LBracket()
        val or = Or()
        val r = Atom("R")
        val rBracket = RBracket()

        val line: List<Token> = listOf(
            lBracket,
            lBracket,
            lBracket, p, and, q, rBracket,
            or,
            lBracket, q, and, r, rBracket,
            rBracket,
            and,
            lBracket,
            lBracket, p, or, q, rBracket,
            and,
            lBracket, q, or, r, rBracket,
            rBracket,
            rBracket,
            rBracket
        )
        val tokens: Queue<Token?> = LinkedList(line)

        val wff: WellFormedFormula = Conjunction(
            Disjunction(
                Conjunction(AtomicCondition("P"), AtomicCondition("Q")),
                Conjunction(AtomicCondition("Q"), AtomicCondition("R"))
            ),
            Conjunction(
                Disjunction(AtomicCondition("P"), AtomicCondition("Q")),
                Disjunction(AtomicCondition("Q"), AtomicCondition("R"))
            )
        )
        assertEquals(wff, WellFormedFormula.parse(tokens))
    }

    @ParameterizedTest
    @ArgumentsSource(ValidateTautologyProvider::class)
    fun testIsTautology(wff: WellFormedFormula, expected: Boolean) {
        val argument = Argument(null)
        argument.addPremise(wff)
        assertEquals(expected, argument.isTautology)
    }

    internal class ValuationArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments?> {
            return Stream.of(
                Arguments.of(notP, mapOf("P" to true), false),
                Arguments.of(notP, mapOf("P" to false), true),
                Arguments.of(disNotPNotQ, mapOf("P" to true, "Q" to false), true),
                Arguments.of(disNotPNotQ, mapOf("P" to true, "Q" to true), false)
            )
        }
    }

    internal class DetermineTruthConditionsArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?> {

            return Stream.of(
                    Arguments.of(p, listOf(pTrue)),
                    Arguments.of(notP, listOf(pFalse)),
                    Arguments.of(Negation(notQ), listOf(qTrue)),
                    Arguments.of(Negation(Negation(notR)), listOf(rFalse)),

                    Arguments.of(Conjunction(p, q), listOf(BinaryCondition(pTrue, qTrue))),
                    Arguments.of(Conjunction(p, notQ), listOf(BinaryCondition(pTrue, qFalse))),
                    Arguments.of(Conjunction(notP, notQ), listOf(BinaryCondition(pFalse, qFalse))),
                    Arguments.of(Negation(Conjunction(p, q)), listOf(
                            BinaryCondition(pFalse, qFalse),
                            BinaryCondition(pFalse, qTrue),
                            BinaryCondition(pTrue, qFalse)
                    )),
                    Arguments.of(Negation(Conjunction(notP, notQ)), listOf(
                            BinaryCondition(pTrue, qTrue),
                            BinaryCondition(pTrue, qFalse),
                            BinaryCondition(pFalse, qTrue)
                    )),
                    Arguments.of(Disjunction(p, q), listOf(
                            BinaryCondition(pTrue, qFalse),
                            BinaryCondition(pFalse, qTrue),
                            BinaryCondition(pTrue, qTrue)
                    )),
                    Arguments.of(Negation(Disjunction(p, q)), listOf(BinaryCondition(pFalse, qFalse))),
                    Arguments.of(Conditional(p, q), listOf(
                            BinaryCondition(pFalse, qFalse),
                            BinaryCondition(pFalse, qTrue),
                            BinaryCondition(pTrue, qTrue)
                    ))
            )
        }
    }

    internal class ValidateTautologyProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?> {
            return Stream.of(
                Arguments.of(p, false),
                Arguments.of(Conjunction(p, p), false),

                Arguments.of(Conjunction(p, Negation(p)), false),

                Arguments.of(Disjunction(p, p), false),

                Arguments.of(
                    Disjunction(p, Negation(p)), true
                ),  // ((P ∧ (Q ∨ R)) ∨ (¬P ∧ (¬Q ∨ ¬R)))

                Arguments.of(
                    Disjunction(
                        Conjunction(
                            p,
                            Disjunction(q, r)
                        ),
                        Conjunction(
                            notP,
                            Conjunction(
                                notQ,
                                notR
                            )
                        )
                    ), false
                ),  // (¬(¬(P ∧ Q) ∧ ¬(P ∧ R)) ∨ ¬(P ∧ (Q ∨ R)))

                Arguments.of(
                    Disjunction(
                        Negation(
                            Conjunction(
                                Negation(
                                    Conjunction(
                                        p,
                                        q
                                    )
                                ),
                                Negation(
                                    Conjunction(
                                        p,
                                        r
                                    )
                                )
                            )
                        ),
                        Negation(
                            Conjunction(
                                p,
                                Disjunction(q, r)
                            )
                        )
                    ), true
                )
            )
        }
    }
}

