package com.probendi.aris

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.IOException
import java.io.StringReader
import java.util.List
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.test.assertEquals

val and = And()
val argument = Argument()
val assert = Assert()
val assign = Assign()
val p = Atom("P")
val p1 = Atom("P1")
val q = Atom("Q")
val r = Atom("R")
val comma = Comma()
val arg1 = Identifier("arg1")
val arg2 = Identifier("arg2")
val arg3 = Identifier("arg3")
val lBracket = LBracket()
val not = Not()
val or = Or()
val print = Print()
val rBracket = RBracket()
val therefore = Therefore()
val helloAris = TString("Hello, Aris!")
val theTrue = True()
val validate = Validate()
val valuate = Valuate()

val tokens = listOf(
    listOf(print, helloAris),
    listOf(print),
    listOf(p, assign, theTrue),
    listOf(q, assign, theTrue),
    listOf(r, assign, theTrue),  // argument arg1 := (P & Q) ∴ R
    listOf(argument, arg1, assign, lBracket, p, and, q, rBracket, therefore, r),
    listOf(valuate, arg1),  // argument arg2 := P, ¬(P ∧ ¬Q) ∴ Q
    listOf(argument, arg2, assign, p, comma, not, lBracket, p, and, not, q, rBracket, therefore, q),
    listOf(validate, arg2),  // argument arg3 := (¬(¬(P ∧ Q) ∧ ¬(P ∧ R)) ∨ ¬(P ∧ (Q ∨ R)))
    listOf(
        argument, arg3, assign, lBracket, not, lBracket, not, lBracket, p, and, q, rBracket, and,
        not, lBracket, p, and, r, rBracket, rBracket, or, not, lBracket, p, and,
        lBracket, q, or, r, rBracket, rBracket, rBracket
    ),
    listOf(assert, arg3)
)

class LexicalAnalyzerTest {

    @ParameterizedTest
    @ArgumentsSource(LexicalAnalyzerArgumentsProvider::class)
    @Throws(IOException::class, ArisException::class)
    fun testTokenize(line: String, expected: List<List<Token>>) {
        val lexer: LexicalAnalyzer
        StringReader(line).use { reader ->
            lexer = LexicalAnalyzer(reader)
            lexer.tokenize()
        }
        assertIterableEquals(expected, lexer.tokens)
    }

    @ParameterizedTest
    @ArgumentsSource(LexicalAnalyzerFailsArgumentsProvider::class)
    @Throws(IOException::class)
    fun testTokenizeFails(line: String, clazz: KClass<out ArisException>, message: String) {
        StringReader(line).use { reader ->
            val lexicalAnalyzer = LexicalAnalyzer(reader)
            val e = assertThrows<ArisException> { lexicalAnalyzer.tokenize() }
            assertEquals(clazz.simpleName, e::class.simpleName)
            assertEquals(message, e.message)
        }
    }

    class LexicalAnalyzerArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of("P1 := true", listOf(listOf(p1, assign, theTrue))),
                Arguments.of(text, tokens)
            )
        }

        companion object {
            val text = """
                print "Hello, Aris!"
                print
                            
                // here we go!
                P := ${'\t'}  true
                 Q := true
                R := true
                argument arg1 := (P & Q) ∴ R
                valuate arg1
                            
                argument arg2 := P, ¬(P ∧ ¬Q) ∴ Q
                validate arg2
                            
                argument arg3 := (¬(¬(P ∧ Q) ∧ ¬(P ∧ R)) ∨ ¬(P ∧ (Q ∨ R)))
                assert arg3
                """.trimIndent()
        }
    }

    class LexicalAnalyzerFailsArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    "P* := true", UnexpectedCharacterException::class,
                    "Unexpected character '*' at position 1 of line 'P* := true'"
                ),
                Arguments.of(
                    "argument arg' !P therefore Q", UnexpectedCharacterException::class,
                    "Unexpected character ''' at position 12 of line 'argument arg' !P therefore Q'"
                ),
                Arguments.of(
                    "(P >Q)", UnexpectedCharacterException::class,
                    "Unexpected character '>' at position 3 of line '(P >Q)'"
                ),
                Arguments.of(
                    "P thereforeQ", UnexpectedCharacterException::class,
                    "Unexpected character 'Q' at position 11 of line 'P thereforeQ'"
                ),
                Arguments.of(
                    "Ptherefore Q", UnexpectedCharacterException::class,
                    "Unexpected character 't' at position 1 of line 'Ptherefore Q'"
                ),
                Arguments.of(
                    "P is \"All men are mortal", UnexpectedCharacterException::class,
                    "Unexpected character 'l' at position 23 of line 'P is \"All men are mortal'"
                ),
                Arguments.of(
                    "P :- true", UnexpectedCharacterException::class,
                    "Unexpected character '-' at position 3 of line 'P :- true'"
                ),
                Arguments.of(
                    "P :", UnexpectedCharacterException::class,
                    "Unexpected character ':' at position 2 of line 'P :'"
                ),
                Arguments.of(
                    "P Q", UnexpectedSymbolException::class,
                    "Unexpected symbol 'Q' at position 1 of line 'P Q'"
                ),
                Arguments.of(
                    "(P & Q)", UnexpectedSymbolException::class,
                    "Unexpected symbol '(' at position 1 of line '(P & Q)'"
                ),
                Arguments.of(
                    "!P therefore Q therefore R", UnexpectedSymbolException::class,
                    "Unexpected symbol 'therefore' at position 15 of line '!P therefore Q therefore R'"
                ),
                Arguments.of(
                    "P", UnexpectedEndOfLineException::class,
                    "Unexpected end of line at line 'P'"
                )
            )
        }
    }
}
