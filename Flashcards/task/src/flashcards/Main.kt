package flashcards

fun main() {
    showCards(createCards())
}

fun createCards(): Map<String, String> {
    println("Input the number of cards:")
    val numCards = readLine()!!.toInt()
    val cards = mutableMapOf<String, String>()
    repeat(numCards) {
        println("The card #${it + 1}:")
        var term = readLine()!!
        while (cards.containsKey(term)) {
            println("The card \"$term\" already exists. Try again:")
            term = readLine()!!
        }
        println("The definition of the card #${it + 1}:")
        var definition = readLine()!!
        while (cards.containsValue(definition)) {
            println("The definition \"$definition\" already exists. Try again:")
            definition = readLine()!!
        }
        cards[term] = definition
    }
    return cards
}

fun showCards(cards: Map<String, String>) {
    cards.forEach {
        println("Print the definition of \"${it.key}\":")
        val answer = readLine()!!
        if (answer == it.value) {
            println("Correct answer.")
        } else {
            print("Wrong answer. The correct one is \"${it.value}\"")
            if (cards.containsValue(answer)) {
                val definition = cardByValue(cards, answer)
                print(", you've just written the definition of \"$definition\"")
            }
            println(".")
        }
    }
}

fun cardByValue(cards: Map<String, String>, value: String) =
        cards.filterValues { it == value }.keys.single()
