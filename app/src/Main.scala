import services.RedisService
import cask.main.Routes
import scalasql.MySqlDialect.*
import org.mariadb.jdbc.MariaDbDataSource

object Main extends cask.Main {
  override def port = 8080
  override def host = "0.0.0.0"

  val dataSource = new MariaDbDataSource;
  dataSource.setUrl("jdbc:mariadb://mariadb:3306/common_db");
  dataSource.setUser("regular");
  dataSource.setPassword("mypassword");

  lazy val dbClient = new scalasql.DbClient.DataSource(
    dataSource,
    config = new scalasql.Config {}
  )

  val redisService = new RedisService()

  val allRoutes: Seq[Routes] = Seq(MinimalRoutes(), UserRoutes(), ProductRoutes())
}

