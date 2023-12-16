import org.scalatest.*
import flatspec.*
import matchers.*
import org.scalactic.*
import org.scalatest.funsuite.AnyFunSuite
import better.files.*
import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*
import org.tinylog.Logger


class AzureCliSpec extends AnyFunSuite:

    test("Azure cli should be executed") {
        //cli.cliMain("listDatasource", "box-parameters.toml")
        //cli.cliMain("createDatasource", "box-parameters.toml")
        cli.cliMain("listDatasource", "parameters.toml")
        cli.cliMain("listDatasource", "box-parameters.toml")


    }
