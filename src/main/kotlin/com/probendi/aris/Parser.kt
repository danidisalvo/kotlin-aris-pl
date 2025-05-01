package com.probendi.aris

import com.probendi.aris.formula.Argument
import com.probendi.aris.formula.WellFormedFormula
import java.util.*

/**
 * Parses and interprets a list of tokens.
 *
 * © 2023-2025 Daniele Di Salvo
 */
class Parser {
    val arguments: MutableMap<String, Argument> = mutableMapOf()
    val assertions: MutableMap<String, Boolean> = mutableMapOf()
    val values: MutableMap<String, Boolean> = mutableMapOf()
    val validations: MutableMap<String, Boolean> = mutableMapOf()
    val valuations: MutableMap<String, Boolean> = mutableMapOf()

    /**
     * Parses and interprets the given queue.
     *
     * @param queue the tokens to be parsed
     * @throws ArisException if the program cannot be parsed and interpreted
     */
    @Throws(ArisException::class)
    fun parse(queue: Queue<Queue<Token?>>) {
        // We parse and interpret all lines one by one.
        // We assume that the lexer already processed the input file.
        // Consequently, we invoke the remove method rather than the poll method.
        // Therefore, there is no need to confirm that the poll method did not return null
        var line = ""
        try {
            while (true) {
                if (queue.peek() == null ) break

                val tokens = queue.remove()
                line = tokens.toString()
                val token = tokens.remove()

                if (token is Print) {
                    if (tokens.peek() != null) {
                        println(tokens.remove())
                    } else {
                        println()
                    }
                    continue
                }

                val nextToken = tokens.remove()
                if (token is Atom) {
                    if (nextToken is Assign) {
                        values.put(token.value, tokens.remove() is True)
                    }
                } else if (token is com.probendi.aris.Argument) {
                    val identifier = nextToken as Identifier
                    tokens.remove()
                    val argument = Argument(null)
                    while (!tokens.isEmpty()) {
                        val formula = WellFormedFormula.parse(tokens)
                        if (formula == null) {
                            val t = tokens.remove()
                            if (t !is RBracket && argument.conclusion != null) {
                                throw UnexpectedSymbolException("")
                            }
                            if (t is Therefore) {
                                argument.conclusion = WellFormedFormula.parse(tokens)
                            }
                        } else {
                            argument.addPremise(formula)
                        }
                    }
                    arguments.put(identifier.value, argument)
                } else if (token is Assert) {
                    val id = nextToken!!.value
                    val b = arguments.getValue(id).isTautology
                    assertions.put(id, b)
                    if (b) {
                        println("argument \"${arguments[id]?.premises?.get(0)}\" is a tautology\n")
                    } else {
                        println("argument \"${arguments[id]?.premises?.get(0)}\" is not a tautology\n")
                    }
                } else if (token is Validate) {
                    val id = nextToken!!.value
                    val b = arguments.getValue(id).isValid
                    validations.put(id, b)
                    if (b) {
                        println("argument \"${arguments[id]}\" is valid")
                    } else {
                        println("argument \"${arguments[id]}\" is invalid")
                    }
                } else if (token is Valuate) {
                    val id = nextToken!!.value
                    val b = arguments.getValue(id).valuate(values)
                    valuations.put(id, b)
                    println("argument \"${arguments[id]}\" is ${b}\n")
                }
            }
        } catch (_: NoSuchElementException) { // this should never happen
            throw ParserException("Failed to parse line $line")
        }
    }
}
