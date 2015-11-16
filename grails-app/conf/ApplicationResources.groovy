modules = {
    application {
        resource url:'js/application.js'
        resource url:'css/AlaBsAdditions.css'
    }
    fancybox {
        resource url:'js/fancybox/jquery.fancybox-1.3.4.css'
        resource url:'js/fancybox/jquery.fancybox-1.3.4.pack.js'
    }

    baHashchange {
        dependsOn "jquery"
        resource url:[dir:'js', file:'jquery.ba-hashchange.min.js']
    }

    amplify {
        resource url:[dir:'js', file:'amplify.js']
    }

    fileupload {
        dependsOn 'bootstrap'
        resource url:[dir:'js', file:'bootstrap-fileupload.min.js']
        resource url:[dir:'css', file:'bootstrap-fileupload.min.css']
    }
}