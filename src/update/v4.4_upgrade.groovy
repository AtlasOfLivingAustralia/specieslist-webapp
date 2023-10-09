/**
 * It can be safely run multiple times. We can stop and rerun at any time
 */

import groovy.json.JsonSlurper

String listsServer = "http://localhost:8080/ws/update_4.4"
boolean completed = false;
int i = 0
int count = 3
println("*** Task starts ***")
while (!completed) {
    URL url = new URL(listsServer)
    HttpURLConnection con = (HttpURLConnection) url.openConnection()
    con.setRequestMethod("GET")
    con.setRequestProperty("Accept", "application/json");
    println("updating... ${i+1}")
    try(BufferedReader br = new BufferedReader(
            new InputStreamReader(con.getInputStream(), "utf-8"))) {

        def parser = new JsonSlurper()
        def result = parser.parse(br)
        println("${result.remaining} species are remained to be updated.  ${result.eclipsed} was used.")
        if (Integer.parseInt(result.remaining) <= 0) {
            completed = true
        }
    } catch(Exception e) {
        completed = true
        println("Exception: ${e}")
        println("The application is terminated due to runtime error!")
    }
    i++
    if (i >=count) {
        completed = true
    }
}

println("*** Task is completed ***")





