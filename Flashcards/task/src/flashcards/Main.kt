package flashcards

import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random

fun main() {
    loop@ do {
        Logger.println("Input the action (add, remove, import, export, ask, exit, log):")
        when (Logger.readLine()) {
            "add" -> Deck.add()
            "remove" -> Deck.remove()
            "import" -> Deck.import()
            "export" -> Deck.export()
            "ask" -> Deck.ask()
            "exit" -> {
                Logger.println("Bye bye!")
                break@loop
            }
            "log" -> Logger.log()
        }
    } while (true)
}

object Deck {
    private val cards = mutableMapOf<String, String>()

    private fun <K, V> Map<K, V>.random(): Map.Entry<K, V> = entries.elementAt(Random.nextInt(size))

    private fun <K, V> Map<K, V>.getKey(value: V): K = filterValues { it == value }.keys.single()

    fun add() {
        Logger.println("The card:")
        val term = Logger.readLine()
        if (cards.containsKey(term)) {
            Logger.println("The card \"$term\" already exists.\n")
            return
        }
        Logger.println("The definition of the card:")
        val definition = Logger.readLine()
        if (cards.containsValue(definition)) {
            Logger.println("The definition \"$definition\" already exists.\n")
            return
        }
        cards[term] = definition
        Logger.println("The pair (\"$term\":\"$definition\") has been added.\n")
    }

    fun remove() {
        Logger.println("The card:")
        val term = Logger.readLine()
        if (!cards.containsKey(term)) {
            Logger.println("Can't remove \"$term\": there is no such card.\n")
            return
        }
        cards.remove(term)
        Logger.println("The card has been removed.\n")
    }

    fun import() {
        Logger.println("File name:")
        val fileName = Logger.readLine()
        try {
            val text = File(fileName).readText()
            val newCards = text.split("\n")
                    .associate { it.split(":").zipWithNext().single() }
            cards.putAll(newCards)
            Logger.println("${newCards.size} cards have been loaded.")
        } catch (e: FileNotFoundException) {
            Logger.println("File not found.\n")
        }
    }

    fun export() {
        Logger.println("File name:")
        val fileName = Logger.readLine()
        val text = cards.entries.joinToString("\n") { "${it.key }:${it.value}" }
        File(fileName).writeText(text)
        Logger.println("${cards.size} cards have been saved.\n")
    }

    fun ask() {
        Logger.println("How many times to ask?")
        val times = Logger.readLine().toInt()
        repeat(times) {
            val card = cards.random()
            Logger.println("Print the definition of \"${card.key}\":")
            val answer = Logger.readLine()
            if (answer == card.value) {
                Logger.println("Correct answer.")
            } else {
                Logger.print("Wrong answer. The correct one is \"${card.value}\"")
                if (cards.containsValue(answer)) {
                    val definition = cards.getKey(answer)
                    Logger.print(", you've just written the definition of \"$definition\"")
                }
                Logger.println(".")
            }
        }
    }
}

object Logger {
    private val log = mutableListOf<String>()

    fun log() {
        println("File name:")
        val fileName = readLine()
        File(fileName).writeText(log.joinToString(""))
        println("The log has been saved.")
    }

    fun print(message: Any?) {
        kotlin.io.print(message)
        log.add("$message")
    }

    fun println(message: Any?) {
        kotlin.io.println(message)
        log.add("$message\n")
    }

    fun readLine(): String {
        val message = kotlin.io.readLine()!!
        log.add("$message\n")
        return message
    }
}
