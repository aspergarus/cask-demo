package decorators

import cask.model.Response.Raw
import cask.router.Result
import scalasql.DbClient.DataSource

class Transactional(dbClient: DataSource) extends cask.RawDecorator {
  def wrapFunction(pctx: cask.Request, delegate: Delegate): Result[Raw] = {
    dbClient.transaction { txn =>
      val res = delegate(pctx, Map("txn" -> txn))
      if (res.isInstanceOf[cask.router.Result.Error]) {
        txn.rollback()
      }

      res
    }
  }
}