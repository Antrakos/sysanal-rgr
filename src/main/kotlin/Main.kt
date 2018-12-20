import kotlinx.html.*
import kotlinx.html.stream.createHTML
import spark.kotlin.Http
import spark.kotlin.ignite

fun main(args: Array<String>) {
    val http: Http = ignite()
    http.staticFiles.location("/public")

    http.get("/random") {
        val m = queryMap().get("m").integerValue() ?: 0
        val n0 = queryMap().get("n0").integerValue() ?: 0
        val lambda = queryMap().get("lambda").integerValue() ?: 0
        val n = queryMap().get("n").integerValue() ?: 0
        val values = generateSequence(n0) { (it * lambda).rem(m) }
                .take(n)
                .toList()
        createHTML().html {
            body {
                +values.joinToString(", ")
            }
        }
    }

    http.get("/calc") {
        val m = queryMap().get("m").integerValue() ?: 0
        val n = queryMap().get("n").integerValue() ?: 0
        val alg = queryMap().get("alg").integerValue() ?: 0
        val inputs = Inputs(
                lambda = queryMap().get("lambda").integerValue() ?: 0,
                mu = queryMap().get("mu").integerValue() ?: 0,
                nu = queryMap().get("nu").integerValue() ?: 0
        )
        val algorithm = findAlgorithm(alg)(m, n)
        val p = algorithm.calculateP(inputs)
        val (queueSize, queueTime, workingChannels, A, q, w) = algorithm.calculateAverages(inputs, p)
        val size = if (algorithm.hasQueue) n + m else n
        createHTML().html {
            head {
                link("css/main.css", rel = "stylesheet")
            }
            body {
                h2 {
                    +algorithm.name
                }
                (0..size).forEach { num ->
                    if (num < size) printArrow(left = 11 + (80) * (num + 1) + 110 * num, top = 0, text = format(algorithm.generateLambda(num), "λ"))
                    if (num < size) printBackwardArrow(left = 11 + (80) * (num + 1) + 110 * num, top = 40, text = format(algorithm.generateMu(num), "µ") + format(algorithm.generateNu(num), "ν", prefix = " + "))
                    printBlock(left = 10 + (190 * num), top = 20, text = num.toString())
                }
                table {
                    style = "margin-top: 150px; position: absolute; padding: 5px;"
                    thead {
                        tr {
                            if (queueSize != null) {
                                th {
                                    +"Середнє число заявок в черзі"
                                }
                            }
                            if (queueTime != null) {
                                th {
                                    +"Середній час очікування в черзі"
                                }
                            }
                            th {
                                +"Середня кількість заянятих каналів"
                            }
                            if (q != null) {
                                th {
                                    +"Відносна пропускна здатність"
                                }
                            }
                            th {
                                +"Абсолютна пропускна здатність"
                            }
                            if (w != null) {
                                th {
                                    +"Середня кількість несправностей"
                                }
                            }
                            p.mapIndexed { index, _ ->
                                th {
                                    +"P"
                                    sub {
                                        +index.toString()
                                    }
                                }
                            }
                        }
                    }
                    tbody {
                        tr {
                            if (queueSize != null) {
                                td {
                                    +String.format("%.2f", queueSize)
                                }
                            }
                            if (queueTime != null) {
                                td {
                                    +String.format("%.2f", queueTime)
                                }
                            }
                            td {
                                +String.format("%.2f", workingChannels)
                            }
                            if (q != null) {
                                td {
                                    +String.format("%.2f", q)
                                }
                            }
                            td {
                                +String.format("%.2f", A)
                            }
                            if (w != null) {
                                td {
                                    +String.format("%.2f", w)
                                }
                            }
                            p.map {
                                td {
                                    +String.format("%.2f", it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun format(value: Int, metric: String, prefix: String = "") = when (value) {
    0 -> ""
    1 -> prefix + metric
    else -> "$prefix$value $metric"
}

fun FlowContent.printBlock(left: Int, top: Int, text: String, width: Int = 80, height: Int = 40) {
    div {
        style = "margin-left: $left; margin-top: $top; width: $width; height: $height; border: 1px solid black; position: absolute; text-align: center;"
        span {
            style = "vertical-align: middle; font-size: 24px; position: static;"
            +"S"
            sub {
                +text
            }
        }
    }
}

fun FlowContent.printArrow(left: Int, top: Int, text: String? = null) {
    div("arrow") {
        style = "margin-left: $left; margin-top: $top; position: absolute; font-size: 21px; text-align: center;"
        if (text != null)
            +"$text"
        div("line")
        div("point")
    }
}

fun FlowContent.printBackwardArrow(left: Int, top: Int, text: String? = null) {
    div("arrow") {
        style = "margin-left: $left; margin-top: $top; position: absolute; font-size: 21px; text-align: center;"
        div("backward-line")
        div("backward-point")
        if (text != null)
            +text.toString()
    }
}
