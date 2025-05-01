package com.probendi.aris

import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.util.LinkedList
import java.util.Queue
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * `com.probendi.aris.LexicalAnalyzer` lexically analyses the input data and produces a list of tokens.
 *
 * © 2023-2025 Daniele Di Salvo
 */
class LexicalAnalyzer(val reader: Reader) {

    val tokens: Queue<Queue<Token?>> = LinkedList()

    /**
     * Tokenizes the lines read from the given input reader.
     *
     * @throws ArisException if an error occurs
     */
    @Throws(ArisException::class)
    fun tokenize() {
        var line: String?
        try {
            BufferedReader(reader).use { br ->
                while (true) {
                    line = br.readLine()
                    if (line == null) break
                    val counts: MutableMap<String, Boolean> = mutableMapOf()
                    var inAtom = false
                    var inIdentifier = false
                    var inString = false
                    var sb = StringBuilder()

                    val n = line.indexOf("//")
                    val str = (if (n != -1) line.substring(0, n) else line)
                    val queue: Queue<Token?> = LinkedList()

                    var i = 0
                    while (i < str.length) {
                        val c = str[i]

                        // first we deal with some special cases
                        if (inAtom) {
                            if (c == ' ' || c == '\t' || c == '&' || c == ')' || c == ',' || c == ':' || c == '-' || c == '|') {
                                queue.add(Atom(sb.toString()))
                                inAtom = false
                            } else if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '\'') {
                                sb.append(c)
                                i++
                                continue
                            } else {
                                throw UnexpectedCharacterException("Unexpected character '$c' at position $i of line '$line'")
                            }
                        }

                        if (inIdentifier) {
                            if (c == ' ' || c == '\t') {
                                queue.add(Identifier(sb.toString()))
                                inIdentifier = false
                            } else if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_') {
                                sb.append(c)
                                i++
                                continue
                            } else {
                                throw UnexpectedCharacterException("Unexpected character '$c' at position $i of line '$line'")
                            }
                        }

                        if (inString) {
                            if (c == '"') {
                                queue.add(TString(sb.toString()))
                                inString = false
                            } else {
                                if (i == str.length - 1) {
                                    throw UnexpectedCharacterException("Unexpected character '$c' at position $i of line '$line'")
                                }
                                sb.append(c)
                            }
                            i++
                            continue
                        }

                        // spaces are ignored except when parsing a string
                        if (c == ' ' || c == '\t') {
                            i++
                            continue
                        }

                        if (c == '"') {
                            inString = true
                            sb = StringBuilder()
                        } else if (oneCharacterTokenMap.containsKey(c)) {
                            queue.add(oneCharacterTokenMap[c]!!.createInstance())
                        } else if (twoCharactersTokenMap.containsKey(c)) {
                            try {
                                val nc = str[++i]
                                if (twoCharactersTokenMap[c]!!.containsKey(nc)) {
                                    queue.add(twoCharactersTokenMap[c]!![nc]!!.createInstance())
                                } else {
                                    throw UnexpectedCharacterException("Unexpected character '$nc' at position $i of line '$line'")
                                }
                            } catch (_: IndexOutOfBoundsException) {
                                throw UnexpectedCharacterException("Unexpected character '$c' at position ${i-1} of line '$line'")
                            }
                        } else if (c >= 'A' && c <= 'Z') {
                            inAtom = true
                            sb = StringBuilder(c.toString())
                        } else if (c >= 'a' && c <= 'z') {
                            // search for a keyword
                            var found = false
                            val s = str.substring(i)
                            for (k in keywords.keys) {
                                if (s.startsWith(k)) {
                                    queue.add(keywords[k]!!.createInstance())
                                    i += k.length
                                    if (i < str.length && str[i] != ' ' && str[i] != '\t') {
                                        throw UnexpectedCharacterException("Unexpected character '${str[i]}' at position $i of line '$line'")
                                    }

                                    if ("argument" == k || "print" == k || "therefore" == k || "valuate" == k) {
                                        if (counts.containsKey(k)) {  // there can be only one symbol per line
                                            throw UnexpectedSymbolException("Unexpected symbol '$k' at position ${i - k.length} of line '$line'")
                                        } else {
                                            counts.put(k, true)
                                        }
                                    }

                                    found = true
                                    break
                                }
                            }
                            if (!found) {
                                inIdentifier = true
                                sb = StringBuilder(c.toString())
                            }
                        } else {
                            throw UnexpectedCharacterException("Unexpected character '$c' at position $i of line '$line'")
                        }
                        i++
                    }

                    // an atom or an identifier can be the last item of a line
                    if (inAtom) {
                        queue.add(Atom(sb.toString()))
                    } else if (inIdentifier) {
                        queue.add(Identifier(sb.toString()))
                    }

                    if (queue.isEmpty()) continue

                    // validates the line's syntax
                    i = 1
                    val list: Queue<Token?> = LinkedList<Token?>()
                    var token: Token?
                    while (true) {
                        token = queue.poll()
                        if (token == null) break

                        if (list.isEmpty()) {
                            val nextToken = queue.peek()
                            if (token !is Print && nextToken == null) {
                                throw UnexpectedEndOfLineException("Unexpected end of line at line '$line'")
                            }

                            if (token is Atom) {
                                if (nextToken !is Assign) {
                                    throw UnexpectedSymbolException("Unexpected symbol '$nextToken' at position $i of line '$line'")
                                }
                            } else if (!(token is Argument || token is Assert ||
                                        token is Print || token is Validate || token is Valuate)
                            ) {
                                throw UnexpectedSymbolException("Unexpected symbol '$token' at position $i of line '$line'")
                            }
                        }
                        val nextToken = queue.peek()
                        if (nextToken == null || token.canFollow(nextToken)) {
                            list.add(token)
                        } else {
                            throw UnexpectedSymbolException("Unexpected symbol '$nextToken' at position $i of line '$line'")
                        }
                        i++
                    }
                    tokens.add(list)
                }
            }
        } catch (e: IOException) {
            throw LexicalAnalyzerIOException(e.message ?: "", e)
        } catch (e: ReflectiveOperationException) {
            throw LexicalAnalyzerIOException(e.message ?: "", e)
        }
    }

    companion object {
        // associate a single character with a token
        val oneCharacterTokenMap: Map<Char, KClass<out Token>> = mapOf(
            '(' to LBracket::class,
            ')' to RBracket::class,
            '∧' to And::class,
            '&' to And::class,
            ',' to Comma::class,
            '→' to MaterialImplication::class,
            '⊃' to MaterialImplication::class,
            '¬' to Not::class,
            '~' to Not::class,
            '!' to Not::class,
            '∨' to Or::class,
            '|' to Or::class,
            '∴' to Therefore::class
        )

        // associate a two-character sequence with a token, e.g., '=' with the assign token
        val twoCharactersTokenMap: Map<Char, Map<Char, KClass<out Token>>> = mapOf(
            ':' to mapOf('=' to Assign::class),
            '-' to mapOf('>' to MaterialImplication::class)
        )

        // associate a keyword with a token
        val keywords: Map<String, KClass<out Token>> = mapOf(
            "argument" to Argument::class,
            "assert" to Assert::class,
            "false" to False::class,
            "print" to Print::class,
            "therefore" to Therefore::class,
            "true" to True::class,
            "validate" to Validate::class,
            "valuate" to Valuate::class
        )
    }
}
