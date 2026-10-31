import ujson.Arr.from

import java.time.Instant
import java.util.Base64

// Vendor
import cask.model.*

import org.mariadb.jdbc.MariaDbDataSource
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtUpickle}

import scala.util.{Failure, Success, Try}
import scalasql.DbApi.Txn
import scalasql.Table
import scalasql.MySqlDialect.*

import ujson.Obj

// Local
import decorators.Transactional
import entities.Product
import services.RedisService
import services.JWKSService

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
  def getProduct(id: String): Obj = {
    ujson.Obj(
      "id" -> id,
      "test-num" -> 123,
      "test-bool" -> true,
      "test-string" -> "FieldName",
      "test-list" -> Seq(1, 2, 3),
      "test-map" -> Map("options" -> 123, "permissions" -> 321, "extra" -> 333),
    )
  }

  @Transactional(Main.dbClient)
  @cask.postJson("/product")
  def createProduct(request: cask.Request, name: ujson.Value, description: ujson.Value, price: ujson.Value, priceUnit: ujson.Value)(txn: Txn): cask.Response[String] = {
    val token = request.headers.get("authorization").map(_.head) match {
      case Some(header: String) => header.split(" ").last
      case None => ""
    }

    val jwkService = new JWKSService(Main.redisService)
    val userRoles = jwkService.fetchUserRoles(token)

    if (userRoles != "Customer") {
      return cask.Response("Access denied", 401, Nil, Nil)
    }

    val body = request.text()
    val result = txn.run(
      Product
        .insert
        .columns(
          _.name := name.str,
          _.description := description.str,
          _.price := price.str.toInt,
          _.priceUnit := priceUnit.str,
        )
    )

    cask.Response(
      ujson.Obj(
      "result" -> result,
      "userRoles" -> userRoles,
      "token" -> token,
      "_name" -> name,
      "_description" -> description,
      "_price" -> price,
      "_priceUnit" -> priceUnit
    ).toString)
  }

  @Transactional(Main.dbClient)
  @cask.put("/product")
  def updateProduct(request: cask.Request, name: ujson.Value, description: ujson.Value, price: ujson.Value, priceUnit: ujson.Value)(txn: Txn): cask.Response[String] = {
    val token = request.headers.get("authorization").map(_.head) match {
      case Some(header: String) => header.split(" ").last
      case None => ""
    }

    val jwkService = new JWKSService(Main.redisService)
    val userRoles = jwkService.fetchUserRoles(token)

    if (userRoles != "Customer") {
      return cask.Response("Access denied", 401, Nil, Nil)
    }

    val body = request.text()
    val result = txn.run(
      Product
        .insert
        .columns(
          _.name := name.str,
          _.description := description.str,
          _.price := price.str.toInt,
          _.priceUnit := priceUnit.str,
        )
    )

    cask.Response(
      ujson.Obj(
        "result" -> result,
        "userRoles" -> userRoles,
        "token" -> token,
        "_name" -> name,
        "_description" -> description,
        "_price" -> price,
        "_priceUnit" -> priceUnit
      ).toString)
  }


  @Transactional(Main.dbClient)
  @cask.get("/products")
  def getProducts()(txn: Txn): String = {
    val result = txn.run(Product.select)
    upickle.default.write(result)
  }

  @Transactional(Main.dbClient)
  @cask.get("/products/:id")
  def getProductByID(id: String)(txn: Txn): String = {

    val result = txn.run(
      Product.select.filter(_.id === id)
    )

    result.lastOption match {
      case Some(product) => upickle.default.write(product)
      case None => "{}"
    }
  }

  initialize()
}
