package rps

import iroha.protocol.Endpoint
import iroha.protocol.TransactionOuterClass
import jp.co.soramitsu.iroha.java.*
import kotlinx.coroutines.*
import java.lang.Math.random
import java.lang.Math.round
import java.lang.Thread.sleep
import java.time.Instant
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.system.measureTimeMillis

const val USERS = 1

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


val adminId = "admin@test"
val aliceId = "alice@game"
val bobId = "bob@game"
val gameId = "game@game"

val irohaAPI = IrohaAPI("localhost", 50051)

val lst: LinkedList<Long> = LinkedList()

fun getStatus(tx: TransactionOuterClass.Transaction): Endpoint.ToriiResponse = irohaAPI.txStatusSync(Utils.hash(tx))

fun sendAndWait(tx: TransactionOuterClass.Transaction): Endpoint.ToriiResponse {
    irohaAPI.transactionSync(tx)
    while (getStatus(tx).txStatus != Endpoint.TxStatus.COMMITTED
        && getStatus(tx).txStatus != Endpoint.TxStatus.REJECTED
    ) {
        println(getStatus(tx))
    }
    return getStatus(tx)
}


fun ring() {
    val time = System.currentTimeMillis()
    try {
        while (time - lst.peek() > 10000)
            lst.remove()
    } catch (ex: Exception) {
    }
}

var txs = 0

fun listen() {
    val query = BlocksQueryBuilder(adminId, Instant.now(), 1)
        .buildSigned(adminKeypair)

    irohaAPI.blocksQuery(query).blockingSubscribe { block ->
        //        println(block)
        val time = System.currentTimeMillis()
        val bl = block
            .blockResponse
            .block
            .blockV1
            .payload
        val committed_hashes = bl
            .transactionsList
            .map {
                jp.co.soramitsu.iroha.java.Utils.hash(it)
            }
        val rejected_hashes = bl.rejectedTransactionsHashesList

        val hashes = committed_hashes + rejected_hashes
        txs += hashes.size
        repeat(hashes.size) {
            lst.add(time)
        }
        //println(time)


    }

}

fun main() {

    val am = "%.4f".format(random())
    val tx = Transaction.builder(adminId)
        .transferAsset(adminId, "test@test", "coin#test", "", am)
        .sign(adminKeypair)
        .build()
    sendAndWait(tx)
    return
//    repeat(USERS) {
//        println("Repeat")
//        GlobalScope.launch(Dispatchers.IO) {
//            while (true) {
//                val am = "%.4f".format(random())
//                val tx = Transaction.builder(adminId)
//                    .transferAsset(adminId, "test@test", "coin#test", "", am)
//                    .sign(adminKeypair)
//                    .build()
//                irohaAPI.transactionSync(tx)
//                sendAndWait(tx)
//                sleep(10000 / 1000)
//
//            }
//        }
//
//    }

    GlobalScope.launch {
        listen()
    }
    fixedRateTimer("", false, 0L, 500) {
        println(txs)
        println(lst.size / 10)
        ring()

    }


}

