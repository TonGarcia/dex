package com.wavesplatform.it.sync.transactions

import com.wavesplatform.it.api.SyncHttpApi._
import com.wavesplatform.it.transactions.BaseTransactionSuite
import com.wavesplatform.it.util._
import com.wavesplatform.it.sync._
import com.wavesplatform.transaction.smart.script.ScriptCompiler
import org.scalatest.prop.TableDrivenPropertyChecks

class IssueTransactionSuite extends BaseTransactionSuite with TableDrivenPropertyChecks {
  val script                        = ScriptCompiler(s"""true""".stripMargin).explicitGet()._1.bytes.value.base64
  val supportedVersions: List[Byte] = List(1) // add "2" after IssueTransactionV2 activation

  test("asset issue changes issuer's asset balance; issuer's waves balance is decreased by fee") {
    for (v <- supportedVersions) {
      val assetName        = "myasset"
      val assetDescription = "my asset description"
      val (balance1, eff1) = notMiner.accountBalances(firstAddress)

      val issuedAssetId =
        sender
          .issue(firstAddress, assetName, assetDescription, someAssetAmount, 2, reissuable = true, issueFee, version = v, script = scriptText(v))
          .id
      nodes.waitForHeightAriseAndTxPresent(issuedAssetId)

      notMiner.assertBalances(firstAddress, balance1 - issueFee, eff1 - issueFee)
      notMiner.assertAssetBalance(firstAddress, issuedAssetId, someAssetAmount)
    }
  }

  test("Able to create asset with the same name") {
    for (v <- supportedVersions) {
      val assetName        = "myasset1"
      val assetDescription = "my asset description 1"
      val (balance1, eff1) = notMiner.accountBalances(firstAddress)

      val issuedAssetId =
        sender
          .issue(firstAddress, assetName, assetDescription, someAssetAmount, 2, reissuable = false, issueFee, version = v, script = scriptText(v))
          .id
      nodes.waitForHeightAriseAndTxPresent(issuedAssetId)

      val issuedAssetIdSameAsset =
        sender
          .issue(firstAddress, assetName, assetDescription, someAssetAmount, 2, reissuable = true, issueFee, version = v, script = scriptText(v))
          .id
      nodes.waitForHeightAriseAndTxPresent(issuedAssetIdSameAsset)

      notMiner.assertAssetBalance(firstAddress, issuedAssetId, someAssetAmount)
      notMiner.assertBalances(firstAddress, balance1 - 2 * issueFee, eff1 - 2 * issueFee)
    }
  }

  test("Not able to create asset when insufficient funds") {
    val assetName        = "myasset"
    val assetDescription = "my asset description"
    val eff1             = notMiner.accountBalances(firstAddress)._2
    val bigAssetFee      = eff1 + 1.waves

    assertBadRequestAndMessage(sender.issue(firstAddress, assetName, assetDescription, someAssetAmount, 2, reissuable = false, bigAssetFee),
                               "negative waves balance")
  }

  val invalidAssetValue =
    Table(
      ("assetVal", "decimals", "message"),
      (0l, 2, "negative amount"),
      (1l, 9, "Too big sequences requested"),
      (-1l, 1, "negative amount"),
      (1l, -1, "Too big sequences requested")
    )

  forAll(invalidAssetValue) { (assetVal: Long, decimals: Int, message: String) =>
    test(s"Not able to create asset total token='$assetVal', decimals='$decimals' ") {
      val assetName          = "myasset2"
      val assetDescription   = "my asset description 2"
      val decimalBytes: Byte = decimals.toByte
      assertBadRequestAndMessage(sender.issue(firstAddress, assetName, assetDescription, assetVal, decimalBytes, reissuable = false, issueFee),
                                 message)
    }
  }

  def scriptText(version: Int) = version match {
    case 2 => Some(script)
    case _ => None
  }

}