package au.org.ala.bie.webapp2

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class ExternalSiteController {

    def index() {}

    def genbankBase = "http://www.ncbi.nlm.nih.gov"
    def scholarBase = "http://scholar.google.com"

    def genbankCountRegex = """All \\(([0-9]{1,})\\)"""

    def genbank = {

        def searchStrings = params.list("s")
        def searchParams = "(" + searchStrings.join(") OR (") + ")"

        def url = (genbankBase + "/nuccore/?term=" + searchParams)
        Document doc = Jsoup.connect(url).get()
        Elements results = doc.select("div.rslt")

        def totalResults = doc.select("a[title=Total Results]").text()

        def matcher = totalResults =~ "All \\(([0-9]{1,})\\)"
        matcher.find()
        totalResults = matcher.group(1)

        def formattedResults = []
        results.each { result ->
            Elements titleEl = result.getElementsByClass("title")
            def linkTag = titleEl.get(0).getElementsByTag("a")
            def link = genbankBase + linkTag.get(0).attr("href")
            def title = linkTag.get(0).text()
            def description = result.select('p[class=desc]').text()
            def furtherDescription = result.select('dl[class=rprtid]').text()
            formattedResults << [link:link,title:title,description:description, furtherDescription:furtherDescription]
        }

        render(contentType: "text/json") {
            [total:totalResults, resultsUrl:url, results:formattedResults]
        }
    }

    def scholar = {

        def searchStrings = params.list("s")
        def searchParams = "\"" + searchStrings.join("\" OR \"") + "\""
        def url = scholarBase + "/scholar?hl=en&btnG=&as_sdt=1%2C5&as_sdtp=&q=" + URLEncoder.encode(searchParams, "UTF-8")
        println url
        Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").referrer("http://www.google.com")get()

        def totalResults = doc.select("div[id=gs_ab_md]").get(0).text()

        def matcher = totalResults =~ "About ([0-9\\,]{1,}) results \\([0-9\\.]{1,} sec\\)"
        matcher.find()
        totalResults = matcher.group(1)

        Elements results = doc.select("div[class=gs_r]")
        def formattedResults = []
        results.each { result ->
            def link = result.select("a").attr("href")
            if(!link.startsWith("http")){
                link =  scholarBase + link
            }
            def title = result.select("a").text()
            def description = result.select("div[class=gs_a]")?.get(0).text()

            def furthEl = result.select("div[class=gs_rs]")
            def furtherDescription = ""
            if(!furthEl.empty){
                furtherDescription = furthEl.get(0).text()
            }

            formattedResults << [link:link,title:title,description:description, furtherDescription:furtherDescription]
        }
        render(contentType: "text/json") {
            [total:totalResults, resultsUrl:url, results:formattedResults]
        }
    }
}
