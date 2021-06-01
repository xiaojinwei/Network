package com.cj.compiler

class ProcessingException(message: String, vararg args: Any) : RuntimeException(if (args.isNotEmpty()) {
    String.format(message, *args)
} else message) {
}