import kotlin.math.pow

data class Inputs(
        val lambda: Int,
        val mu: Int,
        val nu: Int
)

data class Averages(
        val queueSize: Double?,
        val queueTime: Double?,
        val workingChannels: Double,
        val A: Double,
        val q: Double
)

fun factorial(num: Int) = if (num == 0) 1 else (1..num).reduce(Int::times)

interface Algorithm {
    val m: Int
    val n: Int
    val name: String
    val hasQueue: Boolean
    fun generateLambda(num: Int): Int
    fun generateMu(num: Int): Int
    fun generateNu(num: Int): Int
    fun calculateP(data: Inputs): List<Double>
    fun calculateAverages(data: Inputs, p: List<Double>): Averages
}

fun findAlgorithm(id: Int): (Int, Int) -> Algorithm {
    return when (id) {
        1 -> ::MultichannelWithDecline
        3 -> ::MultichannelWithWait
        7 -> ::MultichannelWithLimitedQueueTime
        else -> throw IllegalArgumentException("Unknown algorithm")
    }
}

class MultichannelWithWait(override val m: Int, override val n: Int) : Algorithm {
    override fun calculateP(data: Inputs): List<Double> {
        val ro = data.lambda.toDouble() / data.mu
        val p0 = (0.rangeTo(n).map { ro.pow(it) / factorial(it) }.sum() + (ro.pow(n) / factorial(n)) * ((ro / n - (ro / n).pow(m + 1)) / (1 - (ro / n)))).pow(-1)
        return listOf(p0).plus(
                1.rangeTo(n).map { p0 * ro.pow(it) / factorial(it) }
        ).plus(
                1.rangeTo(m).map { p0 * ro.pow(n + it) / (n.toDouble().pow(it) * factorial(n)) }
        )
    }

    override fun calculateAverages(data: Inputs, p: List<Double>): Averages {
        val ro = data.lambda.toDouble() / data.mu
        val workingChannels = ro * (1 - p[0] * ro.pow(n + m) / (n.toDouble().pow(m) * factorial(n)))
        val queueSize = (p[0] * ro.pow(n + m) / (n * factorial(n))) * 1.rangeTo(m).map { it * (ro / n).pow(it - 1) }.sum()
        val q = (1 - p[0] * ro.pow(n + m) / (n.toDouble().pow(m) * factorial(n)))
        val A = data.lambda * (1 - p[0] * ro.pow(n + m) / (n.toDouble().pow(m) * factorial(n)))
        return Averages(queueSize = queueSize, queueTime = queueSize / data.lambda, workingChannels = workingChannels, q = q, A = A)
    }

    override val name: String
        get() = "Багатоканальна з очікуванням"
    override val hasQueue: Boolean
        get() = true

    override fun generateLambda(num: Int) = 1

    override fun generateMu(num: Int) = if (num < n) num + 1 else n

    override fun generateNu(num: Int) = 0
}
class MultichannelWithDecline(override val m: Int, override val n: Int) : Algorithm {
    override fun calculateP(data: Inputs): List<Double> {
        val ro = data.lambda.toDouble() / data.mu
        val p0 = 0.rangeTo(n).map { ro.pow(it) / factorial(it) }.sum().pow(-1)
        return listOf(p0).plus(
                1.rangeTo(n).map { p0 * ro.pow(it) / factorial(it) }
        )
    }

    override fun calculateAverages(data: Inputs, p: List<Double>): Averages {
        val ro = data.lambda.toDouble() / data.mu
        return Averages(queueSize = null, queueTime = null, workingChannels = ro * (1 - p.last()), q= 1 - p.last(), A=data.lambda*(1 - p.last()))
    }

    override val name: String
        get() = "Багатоканальна з відмовами"
    override val hasQueue: Boolean
        get() = false

    override fun generateLambda(num: Int) = 1

    override fun generateMu(num: Int) = num + 1

    override fun generateNu(num: Int) = 0
}


class MultichannelWithLimitedQueueTime(override val m: Int, override val n: Int) : Algorithm {
    override fun calculateAverages(data: Inputs, p: List<Double>): Averages {
        val queueSize = p.asSequence().drop(n + 1).mapIndexed { index, p -> (index + 1) * p }.sum()
        return Averages(queueTime = data.nu.toDouble().pow(-1), queueSize = queueSize, workingChannels = (data.lambda - data.nu * queueSize) / data.mu, q = 1 - data.nu * queueSize / data.lambda, A = data.lambda - data.nu * queueSize)
    }

    override fun calculateP(data: Inputs): List<Double> {
        val p0 = (0.rangeTo(n).map { (data.lambda.toDouble() / data.mu).pow(it) / factorial(it) }.sum() + ((data.lambda.toDouble() / data.mu).pow(n) / factorial(n)) * (1.rangeTo(m).map {
            data.lambda.toDouble().pow(it) / (1.rangeTo(it).map { n * data.mu + it * data.nu }.reduce(Int::times))
        }.sum())).pow(-1)
        return listOf(p0).plus(
                1.rangeTo(n).map { (data.lambda.toDouble() / data.mu).pow(it) / factorial(it) * p0 }
        ).plus(
                (1).rangeTo(m).map { (data.lambda.toDouble() / data.mu).pow(n) / factorial(n) * p0 * data.lambda.toDouble().pow(it) / (1.rangeTo(it).map { n * data.mu + it * data.nu }.reduce(Int::times)) }
        )
    }


    override val name: String
        get() = "Багатоканальна з обмеженим часом перебування в черзі"
    override val hasQueue: Boolean
        get() = true

    override fun generateLambda(num: Int) = 1

    override fun generateMu(num: Int) = when {
        num >= n -> n
        else -> num + 1
    }

    override fun generateNu(num: Int) = if (num < n) 0 else num - n + 1
}
