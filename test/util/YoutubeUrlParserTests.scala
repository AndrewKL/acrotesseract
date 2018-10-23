package util

import org.junit.Test
import org.junit.Assert._
import util.YoutubeUrlParser

class YoutubeUrlParserTests {
  @Test
  def youtubeUrlParsing_regular(): Unit = {
    /*
    https://www.youtube.com/watch?v=1PkV-A5WTPk&index=6&list=RDq-t5nBbGhPM
  https://www.youtube.com/watch?v=8gO_lxThc1M
  https://youtu.be/8gO_lxThc1M?t=12
  https://youtu.be/1PkV-A5WTPk?list=RDq-t5nBbGhPM&t=19
  https://www.youtube.com/embed/1PkV-A5WTPk?start=33
     */
    assertEquals("1PkV-A5WTPk", YoutubeUrlParser.parseUrl("https://www.youtube.com/watch?v=1PkV-A5WTPk&index=6&list=RDq-t5nBbGhPM").id)
    assertEquals(None, YoutubeUrlParser.parseUrl("https://www.youtube.com/watch?v=1PkV-A5WTPk&index=6&list=RDq-t5nBbGhPM").startTime)

    assertEquals("8gO_lxThc1M", YoutubeUrlParser.parseUrl("https://www.youtube.com/watch?v=8gO_lxThc1M").id)
    assertEquals(None, YoutubeUrlParser.parseUrl("https://www.youtube.com/watch?v=8gO_lxThc1M").startTime)
  }
  @Test
  def youtubeUrlParsing_shortened(): Unit ={
    assertEquals("8gO_lxThc1M", YoutubeUrlParser.parseUrl("https://youtu.be/8gO_lxThc1M?t=12").id)
    assertEquals(Some(12), YoutubeUrlParser.parseUrl("https://youtu.be/8gO_lxThc1M?t=12").startTime)

    assertEquals("1PkV-A5WTPk", YoutubeUrlParser.parseUrl("https://youtu.be/1PkV-A5WTPk?list=RDq-t5nBbGhPM&t=19").id)
    assertEquals(Some(19), YoutubeUrlParser.parseUrl("https://youtu.be/1PkV-A5WTPk?list=RDq-t5nBbGhPM&t=19").startTime)
  }
  @Test
  def youtubeUrlParsing_embeded(): Unit ={
    assertEquals("1PkV-A5WTPk",YoutubeUrlParser.parseUrl("https://www.youtube.com/embed/1PkV-A5WTPk?start=33").id)
    assertEquals(None,YoutubeUrlParser.parseUrl("https://www.youtube.com/embed/1PkV-A5WTPk?start=33").startTime)

  }

  @Test
  def formatEmbededLink(): Unit ={
    val ytv = YoutubeUrlParser.parseUrl("https://youtu.be/1PkV-A5WTPk?list=RDq-t5nBbGhPM&t=19")

    assertEquals("https://www.youtube.com/embed/1PkV-A5WTPk?start=19",ytv.embededUrl())
  }
}
