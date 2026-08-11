package app

import java.security.cert.CertificateFactory
import java.io.ByteArrayInputStream
import java.util.Base64
import java.security.interfaces.RSAPublicKey

import scala.util.{Success, Failure}
import sttp.client4.quick.*
import sttp.client4.Response
import sttp.model.*
import ujson.*
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtUpickle}

class JWKSService(redisClient: RedisService) {

  def fetchUserRoles(token: String): String = {
    val kid = getJwtKid(token)
    val publicKeyPem = getPublicKeyPemByKid(kid)
    val parsedJwt = JwtUpickle.decodeJson(token, publicKeyPem, Seq(JwtAlgorithm.RS256))
    val userRoles = parsedJwt match {
      case Success(value) => value("myshop/roles")(0).str
      case Failure(e) => println(e); ""
    }
    userRoles
  }

  private def getJwtKid(token: String): String = {
    val header = ujson.read(
      new String(
        Base64.getUrlDecoder.decode(token.split("\\.", 2)(0)),
        java.nio.charset.StandardCharsets.UTF_8
      )
    )

    header("kid").str
  }

  private def getPublicKeyPemByKid(kid: String) = {
    val x5c = getSecretByKid(kid)
    val certBytes = Base64.getDecoder.decode(x5c)
    val certificate =
      CertificateFactory
        .getInstance("X.509")
        .generateCertificate(
          new ByteArrayInputStream(certBytes)
        )

    val publicKey = certificate.getPublicKey

    val encoded = Base64.getEncoder.encodeToString(publicKey.getEncoded)

    s"""-----BEGIN PUBLIC KEY-----
       |${encoded.grouped(64).mkString("\n")}
       |-----END PUBLIC KEY-----""".stripMargin
  }

  private def getSecretByKid(kid: String): String = {
    redisClient.retrieve("jwks") match {
      case Success(Some(value)) =>
        findSecretInJson(kid, value)

      case Success(None) | Failure(_) =>
        val newJWKS = getFromAuth0()
        redisClient.store("jwks", newJWKS)

        findSecretInJson(kid, newJWKS)
    }
  }

  private def findSecretInJson(searchKid: String, value: String): String = {
    val json = ujson.read(value)

    json("keys").arr
      .find(key => key("kid").str == searchKid)
      .map(key => key("x5c")(0).str)
      .getOrElse {
        throw new Exception(s"Can't get valid secret from auth0 for kid = $searchKid. Call administrator")
      }
  }

  private def getFromAuth0() = {
    val authUrl: String = sys.env.getOrElse("AUTH0_URL", "")
    printf("Auth0: %s", authUrl)

    if (authUrl.isEmpty) {
      throw new Exception("Auth0 url is not set")
    }

    val response: Response[String] = quickRequest
      .get(uri"${authUrl}")
      .send()

    if (response.code != StatusCode.Ok) {
      throw new Exception("Can't get it from internet")
    }

    response.body
  }
}