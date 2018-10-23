package util

import java.net.{URI, URL, URLDecoder}

import scala.collection.mutable
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

object YoutubeUrlParser {
  /*
  https://www.youtube.com/watch?v=1PkV-A5WTPk&index=6&list=RDq-t5nBbGhPM
  https://www.youtube.com/watch?v=8gO_lxThc1M
  https://youtu.be/8gO_lxThc1M?t=12
  https://youtu.be/1PkV-A5WTPk?list=RDq-t5nBbGhPM&t=19
  https://www.youtube.com/embed/1PkV-A5WTPk?start=33
   */

  def parseUrl(urlStr:String): YouTubeVideo = {
    val url = new URL(urlStr)
    val parameters = splitQuery(url)

    url.getHost.toLowerCase() match {
      case "youtu.be" => new YouTubeVideo(url.getPath.replace("/",""),parameters.get("t").map(_.toInt))
      case "www.youtube.com" => {
        if(url.getPath.toLowerCase().startsWith("/embed"))
          new YouTubeVideo(url.getPath.split("/")(2),parameters.get("t").map(_.toInt))
        else
          new YouTubeVideo(parameters("v"),parameters.get("t").map(_.toInt))
      }
    }
  }

  def splitQuery(url: URL):Map[String,String] = {
    val query_pairs = new mutable.LinkedHashMap[String,String]
    val query = url.getQuery
    if(query ==null) return Map.empty
    val pairs = query.split("&")
    for (pair <- pairs) {
      val idx = pair.indexOf("=")
      query_pairs.put(
        URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
        URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
      )
    }
    query_pairs.toMap
  }
}

case class YouTubeVideo(id:String,startTime:Option[Int]){
  //https://www.youtube.com/embed/1PkV-A5WTPk?start=33
  def embededUrl():String = s"https://www.youtube.com/embed/$id${startTime.map(x=>s"?start=$x").getOrElse("")}"
}
