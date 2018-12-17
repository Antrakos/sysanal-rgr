interface Algorithm {
    val m: Int
    val n: Int
    val name: String
    fun generateLambda(num: Int): Int
    fun generateMu(num: Int): Int
    fun generateNu(num: Int): Int
}

fun findAlgorithm(id: Int): (Int, Int) -> Algorithm {
    return when (id) {
        1 -> ::MultichannelWithDecline
        7 -> ::MultichannelWithLimitedQueueTime
        else -> throw IllegalArgumentException("Unknown algorithm")
    }
}

class MultichannelWithDecline(override val m: Int, override val n: Int) : Algorithm {
    override val name: String
        get() = "Багатоканальна з відмовами"

    override fun generateLambda(num: Int) = num + 1

    override fun generateMu(num: Int) = 1

    override fun generateNu(num: Int) = 0
}


class MultichannelWithLimitedQueueTime(override val m: Int, override val n: Int) : Algorithm {
    override val name: String
        get() = "Багатоканальна з обмеженим часом перебування в черзі"

    override fun generateLambda(num: Int) = 1

    override fun generateMu(num: Int) = when {
        num >= n -> n
        else -> num + 1
    }

    override fun generateNu(num: Int) = if (num < n) 0 else num - n + 1
}
