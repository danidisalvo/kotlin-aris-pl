package com.probendi.aris.formula

import com.probendi.aris.ArisException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

val p1 = AtomicCondition("P1")
val s = AtomicCondition("S")
val notS = Negation(s)

class ArgumentTest {
    @ParameterizedTest
    @ArgumentsSource(ValidateArgumentsProvider::class)
    @Throws(ArisException::class)
    fun testIsValid(argument: Argument, expected: Boolean) {
        Assertions.assertEquals(expected, argument.isValid)
    }

    internal class ValidateArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    Argument(s, notP, notQ, notR), // ¬P, ¬Q, ¬R ∴ S
                    false
                ),

                Arguments.of(
                    Argument(
                        Disjunction(q, s),
                        Conjunction(p, notQ),
                        Conjunction(r, notS)
                    ), // (P ∧ ¬Q), (R ∧ ¬S) ∴ (Q ∨ S)
                    false
                ),

                Arguments.of(
                    Argument(
                        Negation(Conjunction(q, r)),
                        Conjunction(p, notQ)
                    ), // (P ∧ ¬Q) ∴ ¬(Q ∧ R)
                    true
                ),

                Arguments.of(
                    Argument(
                        p, Disjunction(p, q)
                    ), // (P ∨ Q) ∴ P
                    false
                ),

                Arguments.of(
                    Argument(
                        q, p,
                        Negation(Conjunction(p, notQ))
                    ), // P, ¬(P ∧ ¬Q) ∴ Q
                    true
                ),

                Arguments.of(
                    Argument(
                        Conditional(p, r),
                        Conditional(p, q),
                        Conditional(q, r)
                    ), // (P → Q), (Q → R) ∴ (P → R)
                    true
                ),

                Arguments.of(
                    Argument(
                        Disjunction(notP, notR),
                        Disjunction(
                            Conjunction(p, q),
                            r
                        )
                    ), // ((P ∧ Q) ∨ R) ∴ ¬(¬P ∨ ¬R)
                    false
                ),

                Arguments.of(
                    Argument(
                        Conjunction(
                            q,
                            Conjunction(r, p)
                        ),
                        Conjunction(
                            Conjunction(p, q),
                            r
                        )
                    ), // ((P ∧ Q) ∧ R) ∴ (Q ∧ (R ∧ P))
                    true
                ),

                Arguments.of(
                    Argument(
                        Negation(Conjunction(r, notP)),
                        Negation(Conjunction(p, notQ)),
                        Negation(Conjunction(q, r))
                    ), // ¬(P ∧ ¬Q), ¬(Q ∧ R) ∴ ¬(R ∧ ¬P)
                    false
                ),

                Arguments.of(
                    Argument(
                        Disjunction(r, s),
                        Disjunction(notP, r),
                        Disjunction(p, q),
                        Negation(Conjunction(q, notS))
                    ), // (¬P ∨ R), (P ∨ Q), ¬(Q ∧ ¬S) ∴ (R ∨ S)
                    true
                ),

                Arguments.of(
                    Argument(
                        Conditional(s, p1),
                        Conditional(
                            Conditional(
                                p,
                                q
                            ),
                            Conditional(
                                r,
                                notS
                            )
                        ),
                        Conditional(
                            notR,
                            Conjunction(q, p1)
                        ),
                        Conditional(notP, p1)
                    ), // ((P → Q) → (R → ¬S)), (¬R → (Q ∧ P1)), (¬P → P1) ∴ (S → P1)
                    false
                )
            )
        }
    }
}
