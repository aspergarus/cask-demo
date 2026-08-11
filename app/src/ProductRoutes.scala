package app

import java.time.Instant
import java.util.Base64

import scala.util.{Try, Success, Failure}
import scalasql.DbApi.Txn
import scalasql.Sc
import scalasql.Table
import scalasql.MySqlDialect._
import org.mariadb.jdbc.MariaDbDataSource
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtUpickle}

class ProductRoutes()(
  implicit cc: castor.Context,
  log: cask.Logger
) extends cask.Routes {

//  class loggedIn extends cask.RawDecorator {
//    def wrapFunction(request: cask.Request, delegate: Delegate) = {
//      var headers = request.headers.get("Authorization")
//      print("headers", headers)
//      delegate(request)
//      request.headers.get("Authorization").map(_.head) match {
//        case Some(header) => delegate(request, Map("customHeader" -> header))
//        case None =>
//          cask.router.Result.Success(
//            cask.model.Response(
//              s"Request is missing required header: 'X-CUSTOM-HEADER'",
//              400
//            )
//          )
//      }
//    }
//  }

  @cask.get("/product/:id")
  def getProduct(id: String) = {
    ujson.Obj(
      "id" -> id,
      "test-num" -> 123,
      "test-bool" -> true,
      "test-string" -> "FieldName",
      "test-list" -> Seq(1, 2, 3),
      "test-map" -> Map("options" -> 123, "permissions" -> 321, "extra" -> 333),
    )
  }

  @cask.postJson("/product")
  def createProduct(request: cask.Request, name: ujson.Value, description: ujson.Value, price: ujson.Value, priceUnit: ujson.Value) = {
    val token = request.headers.get("authorization").map(_.head) match {
      case Some(header: String) => header.split(" ").last
      case None => ""
    }

    val jwkService = new JWKSService(new RedisService())
    val userRoles = jwkService.fetchUserRoles(token)

    ujson.Obj(
      "userRoles" -> userRoles,
      "token" -> token,
      "_name" -> name,
      "_description" -> description,
      "_price" -> price,
      "_priceUnit" -> priceUnit
    )
  }

  initialize()
}
