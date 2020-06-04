package flashcards

import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random

fun main() {
    loop@ do {
        Logger.println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        when (Logger.readLine()) {
            "add" -> Deck.add()
            "remove" -> Deck.remove()
            "import" -> Deck.import()
            "export" -> Deck.export()
            "ask" -> Deck.ask()
            "hardest card" -> Deck.hardestCard()
            "reset stats" -> Deck.resetStats()
            "exit" -> {
                Logger.println("Bye bye!")
                break@loop
            }
            "log" -> Logger.log()
        }
    } while (true)
}

object Deck {
    private val cardIndex = mutableMapOf<String, Card>()

    private fun <K, V> Map<K, V>.random(): Map.Entry<K, V> = entries.elementAt(Random.nextInt(size))

    private fun Map<String, Card>.containsCardWithDefinition(definition: String) =
            filterValues { it.definition == definition }.isNotEmpty()

    private fun Map<String, Card>.getCardWithDefinition(definition: String): Card =
            filterValues { it.definition == definition }.values.single()

    fun add() {
        Logger.println("The card:")
        val term = Logger.readLine()
        if (cardIndex.containsKey(term)) {
            Logger.println("The card \"$term\" already exists.\n")
            return
        }
        Logger.println("The definition of the card:")
        val definition = Logger.readLine()
        if (cardIndex.containsCardWithDefinition(definition)) {
            Logger.println("The definition \"$definition\" already exists.\n")
            return
        }
        cardIndex[term] = Card(term, definition)
        Logger.println("The pair (\"$term\":\"$definition\") has been added.\n")
    }

    fun remove() {
        Logger.println("The card:")
        val term = Logger.readLine()
        if (!cardIndex.containsKey(term)) {
            Logger.println("Can't remove \"$term\": there is no such card.\n")
            return
        }
        cardIndex.remove(term)
        Logger.println("The card has been removed.\n")
    }

    fun import() {
        Logger.println("File name:")
        val fileName = Logger.readLine()
        try {
            val text = File(fileName).readText()
            val records = text.split("\n")
            records.forEach {
                val (term, definition, mistakes) = it.split(":")
                cardIndex[term] = Card(term, definition, mistakes.toInt())
            }
            Logger.println("${records.size} cards have been loaded.\n")
        } catch (e: FileNotFoundException) {
            Logger.println("File not found.\n")
        }
    }

    fun export() {
        Logger.println("File name:")
        val fileName = Logger.readLine()
        val text = cardIndex.entries.joinToString("\n") {
            "${it.value.term}:${it.value.definition}:${it.value.mistakes}"
        }
        File(fileName).writeText(text)
        Logger.println("${cardIndex.size} cards have been saved.\n")
    }

    fun ask() {
        Logger.println("How many times to ask?")
        val times = Logger.readLine().toInt()
        repeat(times) {
            val card = cardIndex.random().value
            Logger.println("Print the definition of \"${card.term}\":")
            val answer = Logger.readLine()
            if (answer == card.definition) {
                Logger.println("Correct answer.")
            } else {
                Logger.print("Wrong answer. (The correct one is \"${card.definition}\"")
                if (cardIndex.containsCardWithDefinition(answer)) {
                    val term = cardIndex.getCardWithDefinition(answer).term
                    Logger.print(", you've just written the definition of \"$term\" card")
                }
                Logger.println(".)")
                card.mistakes++
            }
        }
    }

    fun hardestCard() {
        val maxErrors = cardIndex.maxBy { it.value.mistakes }?.value?.mistakes ?: 0
        if (maxErrors == 0) {
            Logger.println("There are no cards with errors.\n")
            return
        }
        val hardestTerms = cardIndex.filter { it.value.mistakes == maxErrors }.keys
        Logger.println(if (hardestTerms.size == 1) {
            "The hardest card is \"${hardestTerms.single()}\". " +
                    "You have $maxErrors errors answering it.\n"
        } else {
            "The hardest cards are ${hardestTerms.joinToString("\", \"", "\"", "\"")}. " +
                    "You have $maxErrors errors answering them.\n"
        })
    }

    fun resetStats() {
        cardIndex.forEach { it.value.mistakes = 0 }
        Logger.println("Card statistics has been reset.\n")
    }
}

class Card(val term: String, val definition: String, var mistakes: Int = 0)

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
