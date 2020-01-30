package com.wavesplatform.dex.api

import com.wavesplatform.dex.test.matchers.DiffMatcherWithImplicits
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class ApiErrorSpec extends AnyFreeSpec with Matchers with DiffMatcherWithImplicits {

  // Note, we removed "result" : null , because it effectively equal to a missing field
  private val json =
    """{
  "error" : 3148040,
  "message" : "The order 979P14dmPrcmcYhLeMpJFMuDDchdBeL9ouMPUvvYu1YU has already been placed",
  "template" : "The order {{id}} has already been placed",
  "params" : {
    "id" : "979P14dmPrcmcYhLeMpJFMuDDchdBeL9ouMPUvvYu1YU"
  },
  "success" : false,
  "status" : "OrderRejected"
}"""

  private val message: ApiError = ApiError(
    error = 3148040,
    message = "The order 979P14dmPrcmcYhLeMpJFMuDDchdBeL9ouMPUvvYu1YU has already been placed",
    template = "The order {{id}} has already been placed",
    params = Json.obj("id" -> "979P14dmPrcmcYhLeMpJFMuDDchdBeL9ouMPUvvYu1YU"),
    status = "OrderRejected"
  )

  "backward JSON compatibility" - {
    "serialization" in {
      Json.parse(json).as[ApiError] should matchTo(message)
    }

    "deserialization" in {
      Json.prettyPrint(Json.toJson(message)) should matchTo(json)
    }
  }
}
