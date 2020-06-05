package flashcards

import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random

fun main() {
    Flashcards.run()
}

object Flashcards {
    private const val ACTION_PROMPT =
            "Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):"
    private val deck = Deck()
    private var remainingAsks = 0

    fun run() {
        loop@ do {
            if (remainingAsks == 0) {
                println(when (input(ACTION_PROMPT)) {
                    "add" -> add()
                    "remove" -> remove()
                    "import" -> import()
                    "export" -> export()
                    "ask" -> { ask(); continue@loop }
                    "hardest card" -> hardestCard()
                    "reset stats" -> resetStats()
                    "exit" -> break@loop
                    "log" -> Logger.log()
                    else -> "\n"
                })
            } else {
                println(nextAsk())
            }
            println("")
        } while (true)
        println("Bye bye!")
    }

    private fun add(): String {
        val term = input("The card:")
        if (deck.containsTerm(term)) {
            return "The card \"$term\" already exists."
        }
        val definition = input("The definition of the card:")
        if (deck.containsDefinition(definition)) {
            return "The definition \"$definition\" already exists."
        }
        deck.add(Card(term, definition))
        return "The pair (\"$term\":\"$definition\") has been added."
    }

    private fun remove(): String {
        val term = input("The card:")
        if (!deck.containsTerm(term)) {
            return "Can't remove \"$term\": there is no such card."
        }
        deck.remove(term)
        return "The card has been removed."
    }

    private fun import(): String {
        val fileName = input("File name:")
        return try {
            val cards = deck.import(fileName)
            "$cards cards have been loaded."
        } catch (e: FileNotFoundException) {
            "File not found."
        }
    }

    private fun export(): String {
        val fileName = input("File name:")
        val cards = deck.export(fileName)
        return "$cards cards have been saved."
    }

    private fun ask(): String {
        remainingAsks = input("How many times to ask?").toInt()
        return ""
    }

    private fun nextAsk(): String {
        val card = deck.random()
        val answer = input("Print the definition of \"${card.term}\":")
        remainingAsks--
        return if (answer == card.definition) {
            "Correct answer."
        } else {
            var response = "Wrong answer. (The correct one is \"${card.definition}\""
            val term = deck.withDefinition(answer)?.term
            if (term != null) {
                response += ", you've just written the definition of \"$term\" card"
            }
            card.mistakes++
            "$response.)"
        }
    }

    private fun hardestCard(): String {
        val cards = deck.hardestCard()
        val terms = cards.joinToString(separator = "\", \"", prefix = "\"", postfix = "\"") { it.term }
        val mistakes = cards.firstOrNull()?.mistakes
        return when (cards.size) {
            0 -> "There are no cards with errors."
            1 -> "The hardest card is $terms. You have $mistakes errors answering it."
            else -> "The hardest cards are $terms. You have $mistakes errors answering them."
        }
    }

    private fun resetStats(): String {
        deck.resetStats()
        return "Card statistics has been reset."
    }

    private fun input(prompt: String): String {
        println(prompt)
        return Logger.readLine()
    }

    private fun println(message: Any?) {
        Logger.println(message)
    }
}

class Deck {
    private val cardIndex = mutableMapOf<String, Card>()

    fun random() = cardIndex.entries.elementAt(Random.nextInt(cardIndex.size)).value

    fun containsDefinition(definition: String) =
            cardIndex.filterValues { it.definition == definition }.isNotEmpty()

    fun withDefinition(definition: String): Card? =
            cardIndex.filterValues { it.definition == definition }.values.singleOrNull()

    fun containsTerm(term: String) = cardIndex.containsKey(term)

    fun add(card: Card) {
        cardIndex[card.term] = card
    }

    fun remove(term: String) {
        cardIndex.remove(term)
    }

    fun import(fileName: String): Int {
        val text = File(fileName).readText()
        val records = text.split("\n")
        records.forEach {
            val (term, definition, mistakes) = it.split(":")
            cardIndex[term] = Card(term, definition, mistakes.toInt())
        }
        return records.size
    }

    fun export(fileName: String): Int {
        val text = cardIndex.entries.joinToString("\n") {
            "${it.value.term}:${it.value.definition}:${it.value.mistakes}"
        }
        File(fileName).writeText(text)
        return cardIndex.size
    }

    fun hardestCard(): List<Card> {
        val maxErrors = cardIndex.maxBy { it.value.mistakes }?.value?.mistakes ?: 0
        return if (maxErrors == 0) {
            listOf()
        } else {
            cardIndex.filter { it.value.mistakes == maxErrors }.values.toList()
        }
    }

    fun resetStats() {
        cardIndex.forEach { it.value.mistakes = 0 }
    }
}

class Card(val term: String, val definition: String, var mistakes: Int = 0)

object Logger {
    private val log = mutableListOf<String>()

    fun log(): String {
        println("File name:")
        val fileName = readLine()
        File(fileName).writeText(log.joinToString(""))
        return "The log has been saved."
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
