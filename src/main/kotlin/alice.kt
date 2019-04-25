package rps

import jp.co.soramitsu.iroha.java.Transaction

fun main() {
    val choice = "scissors"

    val tx = Transaction.builder(aliceId)
        .transferAsset(aliceId, gameId, "$choice#game", "", "1")
        .setQuorum(1)
        .sign(aliceKeypair)
        .build()

    println(sendAndWait(tx))
}