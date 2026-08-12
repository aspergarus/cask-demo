package services

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}
import scala.util.Try
import scala.util.Using

//  // Direct access (throws exception if missing)
//  val url = sys.env("DATABASE_URL")
//
//  // Safe access with Option
//  val apiKey: Option[String] = sys.env.get("API_KEY")
//
//  // With default value
//  val port = sys.env.getOrElse("PORT", "8080")

class RedisService {
  private val redisUrl: String = sys.env.getOrElse("REDIS_URL", "redis")
  private val redisPort: Int = sys.env.getOrElse("REDIS_PORT", "6379").toInt;

  private val pool = new JedisPool(new JedisPoolConfig(), redisUrl, redisPort)
  private val cacheTTL = 600

  def store(key: String, value: String): Try[String] = {
    Using(pool.getResource) { jedis =>
      jedis.setex(key, cacheTTL, value)
    }
  }

  def retrieve(key: String): Try[Option[String]] = {
    Using(pool.getResource) { jedis =>
      Option(jedis.get(key))
    }
  }
}

