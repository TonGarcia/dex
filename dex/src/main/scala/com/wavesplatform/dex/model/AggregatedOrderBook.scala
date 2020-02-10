package com.wavesplatform.dex.model

import com.wavesplatform.dex.domain.model.{Amount, Price}
import com.wavesplatform.dex.domain.order.OrderType

import scala.collection.immutable.TreeMap
import AggregatedOrderBook.SideOps

class AggregatedOrderBook private (asks: TreeMap[Price, Amount], bids: TreeMap[Price, Amount]) {
  def append(tpe: OrderType, price: Price, amount: Amount): AggregatedOrderBook =
    if (tpe == OrderType.SELL) AggregatedOrderBook(asks.append(price, amount), bids)
    else AggregatedOrderBook(asks, bids.append(price, amount))
}

object AggregatedOrderBook {
  private val asksOrdering = Ordering[Price]
  private val bidsOrdering = asksOrdering.reverse

  val empty: AggregatedOrderBook = new AggregatedOrderBook(
    bids = TreeMap.empty(asksOrdering),
    asks = TreeMap.empty(bidsOrdering)
  )

  private def apply(asks: TreeMap[Price, Amount], bids: TreeMap[Price, Amount]): AggregatedOrderBook =
    new AggregatedOrderBook(asks, bids)

  private implicit final class SideOps(val self: TreeMap[Price, Amount]) extends AnyVal {
    def append(price: Price, amount: Amount): TreeMap[Price, Amount] = self.updated(price, self.getOrElse(price, 0L) + amount)
  }
}
