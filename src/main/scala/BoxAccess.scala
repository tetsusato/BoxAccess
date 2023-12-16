package com.example

import better.files.*
import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*
import org.tinylog.Logger

import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxCCGAPIConnection
import com.box.sdk.BoxFolder
import com.box.sdk.{BoxItem, BoxItemIterator}
import com.box.sdk.BoxItemIterator
import com.box.sdk.BoxLogger
import com.box.sdk.BoxUser
import com.box.sdk.BoxConfig
import com.box.sdk.BoxDeveloperEditionAPIConnection
import com.box.sdk.DeveloperEditionEntityType
import com.box.sdk.InMemoryLRUAccessTokenCache
import com.box.sdk.BoxFile

import java.io.FileOutputStream
import scala.collection.mutable.ListBuffer
import com.electronwill.nightconfig.toml.TomlParser

import scala.collection.mutable.Queue

extension(b: BoxFile)
    def getName() =
        b.getInfo().getName()
    def getType() =
        b.getInfo().getType()


class BoxAccess(userId: String):
    extension(b: BoxItem#Info)
       def toFile =
           BoxFile(api, b.getID())
    extension(b: BoxItem)
       def toFile =
           BoxFile(api, b.getID())


    val filename = "boxaccess.conf"
    private val res =
        try
             scala.io.Source.fromFile(filename).mkString
        catch
            case e: Exception => Logger.tags("NOTICE", "INFO").error("fromFile {}", e)
                                 Logger.tags("NOTICE", "INFO").info("Trying fromResource", "")
                                 scala.io.Source.fromResource(filename).mkString
    private val tomlParser = TomlParser()
    private val params = tomlParser.parse(res)
    private val downloadDir = params.get("download_dir").asInstanceOf[String]

    //val api = new BoxAPIConnection("QtwG07QCcjNH6IZWsquBQUTOvtvk8mBI") // CMP-app dev-token
    val configFile = File("config.json").contentAsString
    //val configFile = File("config.json.testmi").contentAsString
    Logger.tags("NOTICE", "INFO").info("config={}", configFile)
    val config = BoxConfig.readFrom(configFile)
    val accessTokenCache = InMemoryLRUAccessTokenCache(100)
    //val userId = "AutomationUser_1941832_GsYYcgfmS4@boxdevedition.com" // dev
    //val userId = "AutomationUser_2072576_QtemBNenwC@boxdevedition.com" // prod
    //val api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config, accessTokenCache)
    val api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config)
    //val api = BoxDeveloperEditionAPIConnection.getUserConnection(userId, config, accessTokenCache)
    Logger.tags("NOTICE", "INFO").info("api={}", api)
    val rootFolder = BoxFolder.getRootFolder(api)//.asScala
    Logger.tags("NOTICE", "DEBUG").debug("root={}", rootFolder.getInfo().getName())
    //Logger.tags("DEBUG").debug("api={}", api)
    var currentFolder = rootFolder
    val sep = ","
    var currentPathList = scala.collection.mutable.ListBuffer(currentFolder)
    //var currentPathStr = s"${sep}${currentFolder.getInfo().getName()}"
    var currentPathStr = currentPathList.map(f => f.getInfo().getName()).mkString(sep)
    def login =
        //val userInfo = BoxUser.getCurrentUser(api).getInfo()
        val userInfo = BoxUser.getCurrentUser(api).getInfo("name", "owned_by")
        Logger.tags("NOTICE").info("user={}", userInfo.getName)
        Logger.tags("NOTICE").info("user={}", userInfo)
        //listFolder(rootFolder, depth = 2, limit = 3)
        //getFiles(rootFolder)
        userInfo
    def listFolder(depth: Int = 3, limit: Int = 10): Unit =
        listFolder(rootFolder, depth, limit)
    /**
      * listFolder 標準出力にファイルリストを出力
      * @param folder A start folder
      * @param depth  Maximum depth in case that traverse folders recursively
      * @param limit Miximum number of files to output in a folder
      * */
    def listFolder(folder: BoxFolder, depth: Int, limit: Int): Unit =
        Logger.tags("DEBUG").debug("** folder name: {}(depth={}) ...", folder.getInfo().getName(), depth)
        val childFolders = folder.getChildren().asScala.take(limit)
        //Logger.tags("NOTICE", "INFO").info("folder child={}", childFolders.map(f => f.getName()))
        childFolders.foreach{f =>

            Logger.tags("DEBUG").debug("item info: {}", f.getJson())
            if f.getType == "file" then
                Logger.tags("NOTICE").debug("file found: {}", f.getJson())
                val file = BoxFile(api, f.getID())
                val info = file.getInfo()
                println(s"file created at=${file.getInfo().getCreatedAt()}")
                println(s"owned by =${file.getInfo().getOwnedBy().getName()}")
                println(s"modified by =${file.getInfo().getModifiedBy().getName()}")
                println(s"modified at =${file.getInfo().getModifiedAt()}")
                //val os = FileOutputStream(info.getName())
                //Logger.tags("NOTICE", "INFO").info("downloading... {}", info.getName())
                //file.download(os)                
                //os.close()
            if f.getType == "folder" then
                Logger.tags("NOTICE", "INFO").info("folder found: {}", f.getJson())
                val childFolder = f.getResource()
                if depth > 0 then
                    Logger.tags("NOTICE", "INFO").info("Go into folder: {}", f.getJson())
                    listFolder(childFolder.asInstanceOf[BoxFolder], depth - 1, limit)
                else
                    Logger.tags("NOTICE", "INFO").info("Too deep. ignore: {}", f.getJson())
        }
        
    def getFolders(): Iterable[BoxFolder] =
        getFolders(currentFolder)
    def getFolders(folder: BoxFolder): Iterable[BoxFolder] =
        folder.getChildren().asScala.filter{f =>
            f.getType() == "folder"
        }.map(f => f.getResource().asInstanceOf[BoxFolder])
    def getFolders(folderName: String): Iterable[BoxFolder] =
        val folder = getFolderList(currentFolder)
                         .filter(f => f.getInfo().getName() == folderName).head
        getFolders(folder)
    // currentFolder内のファイル一覧を取得する
    def getFiles(): Iterable[BoxFile] =
        getFiles(currentFolder)
    // folder内のファイル一覧を取得する
    def getFiles(folder: BoxFolder): Iterable[BoxFile] =
        Logger.tags("NOTICE").debug("children={}",folder.getChildren().asScala.map(f => f.getName()))
        folder.getChildren().asScala.filter{f =>
            f.getType() == "file"
        }.map(f => BoxFile(api, f.getID()))
    // folderName内のファイル一覧を取得する
    def getFiles(folderName: String): Iterable[BoxFile] =
        val folder = getFolderList(currentFolder)
                         .filter(f => f.getInfo().getName() == folderName).head
        getFiles(folder)

    // ファイル名で検索して取得する
    def getFiles(folderName: String, fileName: String): Iterable[BoxFile] =
        val folder = currentFolder.getChildren().asScala
                         .filter(f => f.getType() == "folder")
                         .map(f => f.getResource().asInstanceOf[BoxFolder])
                         .filter(f => f.getInfo().getName() == folderName).head
        getFiles(folder, fileName)
    def getFiles(folder: BoxFolder, fileName: String): Iterable[BoxFile] =
        folder.getChildren().asScala.filter{f =>
            f.getType() == "file" && f.getName() == fileName
        }.map(f => BoxFile(api, f.getID()))
    /**
      * Recursively retrieve files from rootFolder according to the depth and limit
      * parameters.
      * @param depth The maximum depth of directories to be searched.
      * @param limit The maximum number of files to retrieve from a folder
      * @return List[BoxFile]
      */
    def getFilesRecursively(depth: Int, limit: Int): ListBuffer[BoxFile] =
        getFilesRecursively(rootFolder, depth, limit)

    /**
      * 
      * Recursively retrieve files from folder according to the depth and limit
      * parameters.
      *
      * @param folder
      * @param depth
      * @param limit
      * @return List[BoxFile]
      */
    def getFilesRecursively(folder: BoxFolder, depth: Int, limit: Int): ListBuffer[BoxFile] =
        val childFolders = folder.getChildren().asScala.take(limit)
        childFolders.map{f =>
            f.getType match
              case "file" =>
                val file = BoxFile(api, f.getID())
                val info = file.getInfo()
                Logger.tags("INFO").debug("file={}", info.getName)
                ListBuffer(file)
              case  "folder" =>
                val childFolder = f.getResource().asInstanceOf[BoxFolder]
                Logger.tags("INFO").debug("folder={}", childFolder.getInfo().getName())
                val list = 
                    if depth > 0 then
                        getFilesRecursively(childFolder, depth - 1, limit)
                    else
                        ListBuffer.empty[BoxFile]
                list
        }.foldLeft(ListBuffer.empty[BoxFile])((f, g) => f ++ g)


    def getFolderList(folder: BoxFolder): Iterable[BoxFolder] =
        folder.getChildren().asScala
             .filter(f => f.getType() == "folder")
             .map(f => f.getResource().asInstanceOf[BoxFolder])

    /**
      * Search ''folderName'' from the ''currentFolder'' and set ''currentFolder''
      * to the ''folderName''.
      * If ''folderName'' is not found in the ''currentFolder'',
      *
      * @param folderName
      */
    def pushCurrentFolder(folderName: String): Unit =
        val folder = currentFolder.getChildren().asScala
                         .filter(f => f.getType() == "folder")
                         .map(f => f.getResource().asInstanceOf[BoxFolder])
                         .filter(f => f.getInfo().getName() == folderName).head
        currentFolder = folder
        currentPathStr += s"${sep}${currentFolder.getInfo().getName()}"
    def pushCurrentFolder(folder: BoxFolder): Unit =
        currentFolder = folder
        Logger.tags("NOTICE").debug("parent={}", folder.getInfo().getParent().getResource().asInstanceOf[BoxFolder].getInfo().getName())
        currentPathStr = (getParentFolder(folder) :+ currentFolder)
                           .map(f => f.getInfo().getName()).mkString(",")

    def getParentFolder(folder: BoxFolder): ListBuffer[BoxFolder] =
        val parent = folder.getInfo().getParent().getResource().asInstanceOf[BoxFolder]
        println(parent.getInfo().getName())
        if parent.getInfo().getParent() != null then
            getParentFolder(parent) :+ parent
        else
            ListBuffer(rootFolder, parent)

    def describeCurrentFolder =
        println(s"****** current folder = ${currentFolder}")
        println(s"       name = ${currentFolder.getInfo().getName}")

    def download(fileName: String): String =
        download(currentFolder, fileName)

    /**
      * 
      * Search for the ''fileName'' from the ''folder'', and download the ''fileName''
      * from the ''folder''.
      * If ''fileName'' is not found, 
      *
      * @param folder
      * @param fileName
      * @return
      */
    def download(folder: BoxFolder, fileName: String): String =
        val file = folder.getChildren().asScala.filter{f =>
            f.getType() == "file" && f.getName() == fileName
        }.map(f => BoxFile(api, f.getID())).head
        Logger.tags("NOTICE", "INFO").info("downloading {}", fileName)
        val downloadedFileName = s"${currentPathStr}${sep}${fileName}"
        Logger.tags("NOTICE").info("output file={}", downloadedFileName)
        val os = FileOutputStream(downloadedFileName)
        file.download(os)
        downloadedFileName

    /**
      * Search for the ''folderName'' from the ''currentFolder'', and download the ''fileName''
      * from the ''folderName''
      * なんか使い道がよく分からなくなってきた
      * なぜfolderNameを探す必要があるのか
      * @param folderName
      * @param fileName
      * @return
      */
    def download(folderName: String, fileName: String): String =
        val folder = currentFolder.getChildren().asScala
                         .filter(f => f.getType() == "folder")
                         .map(f => f.getResource().asInstanceOf[BoxFolder])
                         .filter(f => f.getInfo().getName() == folderName).head
        download(folder, fileName)

    def download(item: BoxItem#Info): String =
        val itemType = item.getType()
        itemType match {
          case _: "file" => 
                Logger.tags("DEBUG").debug("download {} as file", item)
          case _: "folder" => 
                Logger.tags("DEBUG").debug("folder {} was detected", item)

          case _ =>
        }

        val fileName = item.getName()
        Logger.tags("NOTICE", "INFO").info("downloading {}", fileName)

        val file = BoxFile(api, item.getID())
        val downloadedFileName = s"${currentPathStr}${sep}${fileName}"
        Logger.tags("NOTICE").info("output file={}", downloadedFileName)
        val os = FileOutputStream(downloadedFileName)
        file.download(os)
        downloadedFileName

    def iterator(folder: BoxFolder = rootFolder, maxDepth: Int = 0, maxFiles: Int = 0) =
        class BoxFileIterator(folder: BoxFolder, maxDepth: Int, maxFiles: Int)
                   extends Iterator[BoxFile]:
            private val folders = Queue(folder)
            currentFolder = folder
            private var depth = 0
            private var files = 0
            Logger.tag("DEBUG").debug("current folder is {}", currentFolder.getInfo().getName())
            private var files = Queue.empty[BoxFile]
            enqueueFilesFolders()
            import scala.collection.mutable.ListBuffer

            //println(s"files = ${files}")
            //println(s"folders = ${folders}")
            override def hasNext: Boolean = {
              while (files.isEmpty && folders.nonEmpty) {
                files = 0
                // If files is empty, all files were proceeded in the current path
                // In other words, required process for the current path was finished.
                // So, the information of the current path should be removed.
                currentPathList.trimEnd(1)
                enqueueFilesFolders()
              }
              files.nonEmpty
            }

            override def next(): BoxFile = {
              if (!hasNext) {
                throw new NoSuchElementException("No more files")
              }
              files += 1
              files.dequeue()
            }

            /**
              * Dequeue the element from the queue "folders", and all items in that folder
              * to either the queue "files" or the queue "folders".
              */
            private def enqueueFilesFolders(): Unit = {
              depth += 1
              if depth > maxDepth then
                  True
              else
                  val folder = folders.dequeue()
                  Logger.tags("DEBUG").debug("FOLDER={}", folder.getInfo().getName())
                  currentFolder = folder
                  Logger.tag("DEBUG").debug("current folder is {}", currentFolder.getInfo().getName())
                  currentPathList.addOne(folder)
                  Logger.tag("DEBUG").debug("current folder list is {}", currentPathList.map(f => f.getInfo().getName()))
                  val filesFoldersIt = folder.getChildren().asScala.iterator
                  while filesFoldersIt.hasNext do
                      val itemInfo = filesFoldersIt.next()
                      val itemType = itemInfo.getType()
                      itemType match {
                        case _: "file" => files.enqueue(itemInfo.toFile)
                              //            println(s"~~~~~~~~~~~~~~~~~~~ push ${itemInfo} as file")
                        case _: "folder" => folders.enqueue(itemInfo.getResource.asInstanceOf[BoxFolder])
                              //            println(s"~~~~~~~~~~~~~~~~~~~ push ${itemInfo} as folder")
                        case _ =>
                      }
                  }

        new BoxFileIterator(folder: BoxFolder, maxDepth: Int, limit: Int)


