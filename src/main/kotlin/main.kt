package rps

import iroha.protocol.Endpoint
import iroha.protocol.TransactionOuterClass
import jp.co.soramitsu.iroha.java.*
import java.time.*


val adminKeypair = Utils.parseHexKeypair(
    "313a07e6384776ed95447710d15e59148473ccfc052a681317a72a69f2a49910",
    "f101537e319568c765b2cc89698325604991dca57b9716b58016b253506cab70"
)


val aliceKeypair = Utils.parseHexKeypair(
    "de909d5abd40c57106e8a113a0ae6b680564c312730026db73843a4c34db105a",
    "53dd526a43be5df521a8ac317edc3e86c072892c10e2b5345f695ba48874f24e"
)

val bobKeypair = Utils.parseHexKeypair(
    "150242632660e8889ed7bb291bd7eaf48871b3e6b13a39ee95601cc0a2646a90",
    "015c82c1484ac55bf8254d9f671504f6df1c4cbf9430f1af0c54acd5408b1dd7"
)



val adminId = "admin@game"
val aliceId = "alice@game"
val bobId = "bob@game"
val gameId = "game@game"

val irohaAPI = IrohaAPI("localhost", 50051)


fun getStatus(tx: TransactionOuterClass.Transaction): Endpoint.ToriiResponse = irohaAPI.txStatusSync(Utils.hash(tx))

fun sendAndWait(tx: TransactionOuterClass.Transaction): Endpoint.ToriiResponse {
    irohaAPI.transactionSync(tx)
    while (getStatus(tx).txStatus != Endpoint.TxStatus.COMMITTED
        && getStatus(tx).txStatus != Endpoint.TxStatus.REJECTED
    ) ;{
        println(getStatus(tx))
    }
    return getStatus(tx)
}



fun getChoice(userId : String) : String {
    return  irohaAPI
        .query(
            Query.builder(adminId, 1)
                .getAccountTransactions(userId, 1, null)
                .buildSigned(adminKeypair)
        )
        .transactionsPageResponse
        .transactionsList
        .first()
        .payload
        .reducedPayload
        .commandsList
        .first()
        .transferAsset
        .assetId
        .split("#")
        .first()
}

fun getResult(bobChoice : String, aliceChoice: String): String {
    val map = mapOf(
        0 to "tie",
        1 to "bob",
        2 to "alice",
        -1 to "error"
    )
    val res =  when (bobChoice) {
        "rock" -> when(aliceChoice) {
            "rock" -> 0
            "paper" -> 2
            "scissors" -> 1
            else -> -1
        }
        "paper" -> when(aliceChoice) {
            "rock" -> 1
            "paper" -> 0
            "scissors" -> 2
            else -> -1
        }
        "scissors" -> when(aliceChoice) {
            "rock" -> 2
            "paper" -> 1
            "scissors" -> 0
            else -> -1
        }
        else -> -1
    }
    return map[res]!!
}

fun main(){

    val bobChoice = getChoice(bobId)
    val aliceChoice = getChoice(aliceId)

    println(getResult(bobChoice, aliceChoice))

}

