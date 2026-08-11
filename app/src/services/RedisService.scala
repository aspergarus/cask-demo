package app

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
  val redisUrl: String = sys.env.getOrElse("REDIS_URL", "redis")
  val redisPort: Int = sys.env.getOrElse("REDIS_PORT", "6379").toInt;

  val pool = new JedisPool(new JedisPoolConfig(), redisUrl, redisPort)
  val cacheTTL = 600

  def store(key: String, value: String) = {
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

