package flashcards

import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random

fun main() {
    loop@ do {
        println("Input the action (add, remove, import, export, ask, exit):")
        when (readLine()!!) {
            "add" -> Deck.add()
            "remove" -> Deck.remove()
            "import" -> Deck.import()
            "export" -> Deck.export()
            "ask" -> Deck.ask()
            "exit" -> {
                println("Bye bye!")
                break@loop
            }
        }
    } while (true)
}

object Deck {
    private val cards = mutableMapOf<String, String>()

    private fun <K, V> Map<K, V>.random(): Map.Entry<K, V> = entries.elementAt(Random.nextInt(size))

    private fun <K, V> Map<K, V>.getKey(value: V): K = filterValues { it == value }.keys.single()

    fun add() {
        println("The card:")
        val term = readLine()!!
        if (cards.containsKey(term)) {
            println("The card \"$term\" already exists.\n")
            return
        }
        println("The definition of the card:")
        val definition = readLine()!!
        if (cards.containsValue(definition)) {
            println("The definition \"$definition\" already exists.\n")
            return
        }
        cards[term] = definition
        println("The pair (\"$term\":\"$definition\") has been added.\n")
    }

    fun remove() {
        println("The card:")
        val term = readLine()!!
        if (!cards.containsKey(term)) {
            println("Can't remove \"$term\": there is no such card.\n")
            return
        }
        cards.remove(term)
        println("The card has been removed.\n")
    }

    fun import() {
        println("File name:")
        val fileName = readLine()!!
        try {
            val text = File(fileName).readText()
            val newCards = text.split("\n")
                    .associate { it.split(":").zipWithNext().single() }
            cards.putAll(newCards)
            println("${newCards.size} cards have been loaded.")
        } catch (e: FileNotFoundException) {
            println("File not found.\n")
        }
    }

    fun export() {
        println("File name:")
        val fileName = readLine()!!
        val text = cards.entries.joinToString("\n") { "${it.key }:${it.value}" }
        File(fileName).writeText(text)
        println("${cards.size} cards have been saved.\n")
    }

    fun ask() {
        println("How many times to ask?")
        val times = readLine()!!.toInt()
        repeat(times) {
            val card = cards.random()
            println("Print the definition of \"${card.key}\":")
            val answer = readLine()!!
            if (answer == card.value) {
                println("Correct answer.")
            } else {
                print("Wrong answer. The correct one is \"${card.value}\"")
                if (cards.containsValue(answer)) {
                    val definition = cards.getKey(answer)
                    print(", you've just written the definition of \"$definition\"")
                }
                println(".")
            }
        }
    }
}
