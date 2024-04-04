package xyz.dudedayaworks.result

interface Error {
    val cause: Exception? get() = null
}