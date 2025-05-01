package com.probendi.aris

/**
 * An `com.probendi.aris.ArisException` is thrown when an error occurs executing `Aris`.
 *
 * © 2023-2025 Daniele Di Salvo
 */
open class ArisException(message: String, cause: Throwable? = null) : Exception(message, cause)

class LexicalAnalyzerIOException(message: String, cause: Throwable? = null) : ArisException(message, cause)

class MissingSymbolException(message: String, cause: Throwable? = null) : ArisException(message, cause)

class ParserException(message: String, cause: Throwable? = null) : ArisException(message, cause)

class UnexpectedCharacterException(message: String, cause: Throwable? = null) : ArisException(message, cause)

class UnexpectedEndOfLineException(message: String, cause: Throwable? = null) : ArisException(message, cause)

class UnexpectedSymbolException(message: String, cause: Throwable? = null) : ArisException(message, cause)
