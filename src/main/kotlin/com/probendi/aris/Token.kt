package com.probendi.aris

import kotlin.reflect.KClass

/**
 * A token parsed by the LexicalAnalyzer.
 *
 * © 2023-2025 Daniele Di Salvo
 */
abstract class Token(val validTokens: List<KClass<out Token>>, val value: String = "") {

    /**
     * Returns `true` if the given `next` token can follow this token.
     *
     * @param next the next token
     * @return if the given `next` token can follow this token
     */
    fun canFollow(next: Token): Boolean {
        return next::class in validTokens
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Token) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value
    }
}

class And : Token(listOf(Atom::class, LBracket::class, Not::class)) {
    override fun toString(): String {
        return "∧"
    }
}

class Argument : Token(listOf(Identifier::class)) {
    override fun toString(): String {
        return "argument"
    }
}

class Assert : Token(listOf(Identifier::class)) {
    override fun toString(): String {
        return "assert"
    }
}

class Assign : Token(
    listOf(Atom::class, False::class, LBracket::class, Not::class, True::class)) {
    override fun toString(): String {
        return ":="
    }
}

open class Atom (value: String) : Token(
    listOf(
        And::class,
        Assign::class,
        Comma::class,
        MaterialImplication::class,
        LBracket::class,
        Or::class,
        RBracket::class,
        Therefore::class
    ), value
)

class Comma : Token(listOf(Atom::class, LBracket::class, Not::class)) {
    override fun toString(): String {
        return ","
    }
}

class False : Token(listOf()) {
    override fun toString(): String {
        return "false"
    }
}

class Identifier(value: String) : Token(listOf(Assign::class), value)

class LBracket : Token(listOf(Atom::class, LBracket::class, Not::class)) {
    override fun toString(): String {
        return "("
    }
}

class MaterialImplication : Token(listOf(Atom::class, LBracket::class, Not::class)) {
    override fun toString(): String {
        return "→"
    }
}

class Not : Token(listOf(Atom::class, LBracket::class, Not::class)) {
    override fun toString(): String {
        return "¬"
    }
}

class Or : Token(listOf(Atom::class, LBracket::class, Not::class)) {
    override fun toString(): String {
        return "∨"
    }
}

class Print : Token(listOf(TString::class)) {
    override fun toString(): String {
        return "print"
    }
}

class RBracket : Token(
    listOf(
        And::class,
        MaterialImplication::class,
        Comma::class,
        Or::class,
        RBracket::class,
        Therefore::class
    ))
{
    override fun toString(): String {
        return ")"
    }
}

class Therefore : Token(listOf(Atom::class, LBracket::class, Not::class)) {
    override fun toString(): String {
        return "∴"
    }
}

class True : Token(listOf()) {
    override fun toString(): String {
        return "true"
    }
}

class TString(value: String) : Token(listOf(), value)

class Validate : Token(listOf(Identifier::class)) {
    override fun toString(): String {
        return "validate"
    }
}

class Valuate : Token(listOf(Identifier::class)) {
    override fun toString(): String {
        return "valuate"
    }
}
