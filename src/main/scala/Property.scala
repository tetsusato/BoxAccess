import better.files.File
import com.electronwill.nightconfig.toml.TomlParser
import scala.jdk.CollectionConverters._

class Property(filename: String):
    val res = File(filename).contentAsString
    val tomlParser = TomlParser()
    val params = tomlParser.parse(res)
    println(params)
    val serviceName = params.get("service_name").asInstanceOf[String]

    val adminKey = params.get("admin_key").asInstanceOf[String]
    val storageKey = params.get("storage_key").asInstanceOf[String]
    val cognitiveServiceKey = params.get("cognitive_service_key").asInstanceOf[String]
    val storageAccountName = params.get("storage_account_name").asInstanceOf[String]
    val storageConnectionString = s"DefaultEndpointsProtocol=https;AccountName=${storageAccountName};AccountKey=${storageKey};EndpointSuffix=core.windows.net"
    val endPoint = s"https://${serviceName}.search.windows.net"
    val indexFileName = params.get("index_file_name").asInstanceOf[String]
    val indexerFileName = params.get("indexer_file_name").asInstanceOf[String]
    val skillsetFileName = params.get("skillset_file_name").asInstanceOf[String]
//    val credentials = s"{ \"connectionString\": \"DefaultEndpointsProtocol=https;AccountName=${storageAccountName};AccountKey=${storageKey};EndpointSuffix=core.windows.net\"}"
    val subscriptionId = params.get("subscription_id").asInstanceOf[String]
    val resourceGroupName = params.get("resource_group_name").asInstanceOf[String]
