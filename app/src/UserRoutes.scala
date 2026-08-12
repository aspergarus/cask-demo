import java.time.Instant
import scalasql.DbApi.Txn
import scalasql.Sc
import scalasql.Table
import scalasql.MySqlDialect.*
import org.mariadb.jdbc.MariaDbDataSource
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtUpickle}
import ujson.Obj

case class UserRoutes()(
  implicit cc: castor.Context,
  log: cask.Logger
) extends cask.Routes {

  @cask.get("/user/:userName") // variable path segment, e.g. HOST/user/lihaoyi
  def getUserProfile(userName: String, segments: cask.RemainingPathSegments): String = {
    s"User ${userName.toUpperCase()}, ${segments.value}"
  }

  @cask.get("/user2/:userName") // allow unknown params, e.g. HOST/user2/foo?foo=bar&qux=baz
  def getUserProfileAllowUnknown(userName: String, params: cask.QueryParams): String = {
    s"User $userName " + params.value.get("param")
  }

  @cask.get("/user-json")
  def jsonEndpointObj(): Obj = {
    ujson.Obj(
      "test-num" -> 123,
      "test-bool" -> true,
      "test-string" -> "FieldName",
      "test-list" -> Seq(1,2,3),
      "test-map" -> Map("options" -> 123, "permissions" -> 321, "extra" -> 333),
    )
  }

  @cask.get("/jwt-test")
  def JwtTest(): String = {
    val claim = JwtClaim(
      expiration = Some(Instant.now.plusSeconds(157784760).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )
    val key = "secretKey"
    val algo = JwtAlgorithm.HS256

    val token = JwtUpickle.encode(claim, key, algo)
    token
  }

  initialize()
}
