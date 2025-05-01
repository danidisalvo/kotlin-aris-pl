package com.probendi.aris

import java.nio.file.Files
import java.nio.file.Path

/**
 * `Aris` is a propositional logic interpreter.
 *
 * © 2023-2025 Daniele Di Salvo
 */
fun main(Args: Array<String>) {

    val usage = "Usage: java -jar aris-pl-1.0.0-all.jar file"

    if (Args.size != 1) {
        println(usage)
    }

    Files.newBufferedReader(Path.of(Args[0])).use { reader ->
        val lexer = LexicalAnalyzer(reader)
        lexer.tokenize()
        val parser = Parser()
        parser.parse(lexer.tokens)
    }}
