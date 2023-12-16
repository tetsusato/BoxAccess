
import org.scalatest.*
import flatspec.*
import matchers.*
import org.scalactic.*
import org.scalatest.funsuite.AnyFunSuite
import better.files.*
import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*
import org.tinylog.Logger

import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxCCGAPIConnection
import com.box.sdk.BoxFolder
import com.box.sdk.BoxItem
import com.box.sdk.BoxLogger
import com.box.sdk.BoxUser
import com.box.sdk.BoxConfig
import com.box.sdk.BoxDeveloperEditionAPIConnection
import com.box.sdk.DeveloperEditionEntityType
import com.box.sdk.InMemoryLRUAccessTokenCache

import com.example.{BoxAccess, getName, getType}


class BoxAccessSpec extends AnyFunSuite:
    //val userId = "AutomationUser_1941832_GsYYcgfmS4@boxdevedition.com" // dev
    val userId = "AutomationUser_2072576_QtemBNenwC@boxdevedition.com" //prod
    test("Login should be executed") {
        //val configFile = File("config.json").contentAsString
        //Logger.tags("NOTICE").info("config={}", configFile)
        //val config = BoxConfig.readFrom(configFile)
        //val accessTokenCache = InMemoryLRUAccessTokenCache(100)
        //val api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config)
        //val api = BoxDeveloperEditionAPIConnection("21859980320", DeveloperEditionEntityType.USER, config, accessTokenCache)
        //val api = new BoxAPIConnection("X7mTZjkTZAFKeeeLo0IpqduyG4aF9Wol") // CMP-app dev-token
        //val api = BoxDeveloperEditionAPIConnection("CWOK0JpQn3zQV8WM2OqWRGbj9xDw3v6w")
        //val api = BoxCCGAPIConnection.userConnection("trqlpqz21zy6bnet65uwtadswcw5jt0z", "cEMzTPHWSPI7wkldjTXiYYEyvMPSy6nn", "pcatech003_admin@persol.co.jp")
        //val api = BoxCCGAPIConnection.applicationServiceAccountConnection("trqlpqz21zy6bnet65uwtadswcw5jt0z", "cEMzTPHWSPI7wkldjTXiYYEyvMPSy6nn", "pcatech003_admin")
        val ba = BoxAccess(userId)
        val ret = ba.login
        Logger.tags("NOTICE", "INFO").info("login={}", ret)
        //val folders = ba.listFolder()
        //Logger.tags("NOTICE", "INFO").info("folders={}", folders)
    }
    test("getFolders should be executed") {
        val ba = BoxAccess(userId)
        ba.login
        println(ba.getFolders().map(f => f.getInfo().getName))
    }
    test("getFoldersByName should be executed") {
        val ba = BoxAccess(userId)
        ba.login
        println(ba.getFolders("【関係者限】【社内限】K0001_検証").map(f => f.getInfo().getName))
    }
    test("pushCurrentFolder should be executed") {
        val ba = BoxAccess(userId)
        ba.login
        ba.pushCurrentFolder("【関係者限】【社内限】K0001_検証")
        println(ba.getFolders("コンプラツール提出用").map(f => f.getInfo().getName))
        ba.pushCurrentFolder("コンプラツール提出用")
        ba.describeCurrentFolder

        ba.pushCurrentFolder("【CMP0024】開発環境構築")
        val folder = ba.currentFolder

        ba.describeCurrentFolder
        println(ba.getFiles().map(f => f.getInfo().getName))
        ba.pushCurrentFolder("検証エビデンス (EMS制御)")

        ba.describeCurrentFolder
        println(ba.getFiles().map(f => f.getInfo().getName))
        ba.pushCurrentFolder(folder)

        ba.describeCurrentFolder
        println(ba.getFiles().map(f => f.getInfo().getName))

    }

    test("getFiles should be executed") {
        val ba = BoxAccess(userId)
        val targetFolderName = "検証エビデンス (EMS制御)"
        val targetFileName = "コピー禁止のデモ.mov"
        ba.login
        ba.pushCurrentFolder("【関係者限】【社内限】K0001_検証")
        ba.pushCurrentFolder("コンプラツール提出用")
        ba.pushCurrentFolder("【CMP0024】開発環境構築")
        println(ba.getFiles().map(f => f.getInfo().getName))
        println(ba.getFiles(targetFolderName).map(f => f.getInfo().getName))
        println(ba.getFiles(targetFolderName, targetFileName).map(f => f.getInfo().getName))
    }

    test("download should be executed") {
        val ba = BoxAccess(userId)
        ba.login
        ba.pushCurrentFolder("【関係者限】【社内限】K0001_検証")
        ba.pushCurrentFolder("コンプラツール提出用")
        ba.pushCurrentFolder("【CMP0024】開発環境構築")
        println(ba.getFiles("検証エビデンス (EMS制御)", "コピー禁止のデモ.mov").map(f => f.getInfo().getName))
        val fileName = ba.download("検証エビデンス (EMS制御)", "コピー禁止のデモ.mov")
        Logger.tags("NOTICE").info("filename={}", fileName)
    }

    test("getFilesRecursively should be executed") {
        val ba = BoxAccess(userId)
        ba.login
        val files = ba.getFilesRecursively(2, 3)
        val fileNameList = files.map(f => f.getInfo().getName())
        Logger.tags("NOTICE").info("files={}", fileNameList)
        assert(fileNameList == 
                 List("20220701_JIPDECSeminar_01.pdf", 
                            "20220701_JIPDECSeminar_02.pdf", 
                            "外国にある第三者へ個人データを移転する方法.txt")
        )
    }
    test("iterator should be executed") {
        val ba = BoxAccess(userId)
        val login = ba.login
        Logger.tags("NOTICE").info("login userInfo={}", login)
        val it = ba.iterator()
        Logger.tags("NOTICE").info("hasNext={}", it.hasNext)
        var next1Item = it.next
        Logger.tags("NOTICE").info("next={}", next1Item)
        Logger.tags("NOTICE").info("next type={}", next1Item)
        Logger.tags("NOTICE").info("next name={}", next1Item.getName())
        Logger.tags("NOTICE").info("it.hasNext={}", it.hasNext)
        val next2Item = it.next
        Logger.tags("NOTICE").info("next={}", next2Item)
        Logger.tags("NOTICE").info("next type={}", next2Item.getType())
        Logger.tags("NOTICE").info("next name={}", next2Item.getName())

    }
    test("iterator test should be executed") {
        val ba = BoxAccess(userId)
        val login = ba.login
        Logger.tags("NOTICE").info("login userInfo={}", login)
        val it = ba.iterator()
        for i <- Range(0, 30) do
              val nextItem = it.next
              println(nextItem)
              //println(nextItem.getInfo().getName())

              println(nextItem.getType())
              println(nextItem.getName())
    }
    /*
        [info] BoxAccessSpec:
        [info] - iterator heavy test should be executed
        [info] Run completed in 3 hours, 3 minutes, 46 seconds.
        [info] Total number of tests run: 1
        [info] Suites: completed 1, aborted 0
        [info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
        [info] All tests passed.
        [success] Total time: 11030 s (03:03:50), completed Nov 4, 2023, 5:32:22 AM

     */
    test("iterator heavy test should be executed") {
        val ba = BoxAccess(userId)
        val login = ba.login
        Logger.tags("NOTICE").info("login userInfo={}", login)
        val it = ba.iterator()
        while it.hasNext do
            val nextItem = it.next
            println(nextItem.getType())
            println(nextItem.getName())
    }
    test("download from the iterator should be executed") {
        val ba = BoxAccess(userId)
        val login = ba.login
        Logger.tags("NOTICE").info("login userInfo={}", login)
        val it = ba.iterator()
        val limit = 14
        var i = 0
        while it.hasNext && i < limit do
            val item = it.next
            println(item.getType())
            println(item.getName())
//            ba.download(item)
            i += 1
    }
