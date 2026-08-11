package app

import scalasql.DbApi.Txn
import scalasql.Sc
import scalasql.Table
import scalasql.MySqlDialect._
import org.mariadb.jdbc.MariaDbDataSource

case class MinimalRoutes()(
  implicit cc: castor.Context,
  log: cask.Logger
) extends cask.Routes {

  val dataSource = new MariaDbDataSource;
  dataSource.setUrl("jdbc:mariadb://mariadb:3306/common_db");
  dataSource.setUser("regular");
  dataSource.setPassword("mypassword");

  lazy val mysqlClient = new scalasql.DbClient.DataSource(
    dataSource,
    config = new scalasql.Config {}
  )

  class transactional extends cask.RawDecorator{
    def wrapFunction(pctx: cask.Request, delegate: Delegate) = {
      mysqlClient.transaction { txn =>
        val res = delegate(pctx, Map("txn" -> txn))
        if (res.isInstanceOf[cask.router.Result.Error]) txn.rollback()
        res
      }
    }
  }

  case class Todo[T[_]](id: T[Int], checked: T[Boolean], text: T[String])
  object Todo extends scalasql.Table[Todo] {
    given todoRW: upickle.default.ReadWriter[Todo[Sc]] = upickle.default.macroRW[Todo[Sc]]
  }

//  mysqlClient.getAutoCommitClientConnection.updateRaw(
//    """
//    CREATE TABLE IF NOT EXISTS todo (
//      id INTEGER PRIMARY KEY AUTO_INCREMENT,
//      checked BOOLEAN,
//      text TEXT
//    );
//    """.stripMargin)
//
//
//  mysqlClient.getAutoCommitClientConnection.updateRaw(
//    """
//    INSERT INTO todo (checked, text) VALUES
//    (1, 'Get started with Cask'),
//    (0, 'Profit!');
//    """.stripMargin)

  @cask.get("/")
  def home(params: cask.QueryParams) = index(params)

  @cask.get("/app")
  def homeApp(params: cask.QueryParams) = index(params)

  private def index(params: cask.QueryParams) = {
    val html = new String(
      getClass.getResourceAsStream("/public/index.html").readAllBytes(),
      "UTF-8"
    )

    cask.Response(
      data = html,
      headers = Seq("Content-Type" -> "text/html")
    )
  }

  // Search by http://localhost:10000/static/file/index.html or http://localhost:10000/static/file/example.txt
  @cask.staticResources("/static")
  def staticFileRoutes() = "public"

  @cask.get("/statics/:foo")
  def getDynamics(foo: String) = {
    s"dynamic route $foo"
  }

  @cask.post("/do-thing")
  def doThing(request: cask.Request) = {
    request.text().reverse
  }

  @cask.get("/article/:articleId") // Mandatory query param, e.g. HOST/article/123?param=bar
  def getArticle(articleId: Int, param: String) = {
    s"Article $articleId $param"
  }

  @cask.postJson("/json-obj")
  def jsonEndpointObj(value1: ujson.Value, value2: Seq[Int]) = {
    ujson.Obj(
      "value1" -> value1,
      "test" -> value1,
      "value2" -> value2
    )
  }

  @cask.get("/read-cookie")
  def readCookies(username: cask.Cookie) = {
    username.value
  }

  @cask.get("/store-cookie")
  def storeCookies() = {
    cask.Response(
      "Cookies Set!",
      cookies = Seq(cask.Cookie("username", "the_username"))
    )
  }

  @cask.get("/login")
  def login() = {
    cask.Redirect("/sign-in")
  }

  @cask.get("/sign-in")
  def singIn() = {
    cask.Abort(401)
  }

  initialize()
}

object MinimalRoutesMain extends cask.Main {
  override def port = 8080
  override def host = "0.0.0.0"

  val allRoutes = Seq(MinimalRoutes(), UserRoutes(), ProductRoutes())
}
