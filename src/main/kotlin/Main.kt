import kotlinx.html.*
import kotlinx.html.stream.createHTML
import spark.kotlin.Http
import spark.kotlin.ignite

fun main(args: Array<String>) {
    val http: Http = ignite()
    http.staticFiles.location("/public")

    http.get("/") {
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
        val (queueSize, queueTime, workingChannels) = algorithm.calculateAverages(inputs, p)
        createHTML().html {
            head {
                link("css/main.css", rel = "stylesheet")
            }
            body {
                (0..m + n).forEach { num ->
                    if (num < n + m) printArrow(left = 11 + (80) * (num + 1) + 90 * num, top = 0, text = format(algorithm.generateLambda(num), "λ"))
                    if (num < n + m) printBackwardArrow(left = 11 + (80) * (num + 1) + 90 * num, top = 40, text = format(algorithm.generateMu(num), "µ") + format(algorithm.generateNu(num), "ν", prefix = " + "))
                    printBlock(left = 10 + (170 * num), top = 20, text = num.toString())
                }
                table {
                    style = "margin-top: 150px; position: absolute; padding: 5px;"
                    thead {
                        tr {
                            th {
                                +"Середнє число заявок в черзі"
                            }
                            th {
                                +"Середнє час очікування в черзі"
                            }
                            th {
                                +"Середня кількість заянятих каналів"
                            }
                            p.mapIndexed { index, p ->
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
                            td {
                                +String.format("%.2f", queueSize)
                            }
                            td {
                                +String.format("%.2f", queueTime)
                            }
                            td {
                                +String.format("%.2f", workingChannels)
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
        style = "margin-left: $left; margin-top: $top; position: absolute; font-size: 17px; text-align: center;"
        if (text != null)
            +"$text"
        div("line")
        div("point")
    }
}

fun FlowContent.printBackwardArrow(left: Int, top: Int, text: String? = null) {
    div("arrow") {
        style = "margin-left: $left; margin-top: $top; position: absolute; font-size: 17px; text-align: center;"
        div("backward-line")
        div("backward-point")
        if (text != null)
            +text.toString()
    }
}
