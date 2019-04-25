package rps

import jp.co.soramitsu.iroha.java.Transaction

fun main() {
    val choice = "paper"

    val tx = Transaction.builder(bobId)
        .transferAsset(bobId, gameId, "$choice#game", "", "1")
        .setQuorum(1)
        .sign(bobKeypair)
        .build()

    println(sendAndWait(tx))
}