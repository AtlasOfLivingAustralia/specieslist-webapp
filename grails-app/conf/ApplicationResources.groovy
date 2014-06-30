modules = {
    application {
        resource url:'js/application.js'
        resource url:'css/AlaBsAdditions.css'
    }
    fancybox {
        resource url:'/fancybox/jquery.fancybox-1.3.4.css'
        resource url:'fancybox/jquery.fancybox-1.3.4.pack.js'
//        resource url:'/fancybox/jquery.fancybox.css'
//        resource url:'/fancybox/jquery.fancybox.pack.js'
//        resource url:'/fancybox/blank.gif'
//        resource url:'/fancybox/fancybox_loading.gif'
//        resource url:'/fancybox/fancybox_overlay.png'
//        resource url:'/fancybox/fancybox_sprite.png'
    }

    baHashchange {
        dependsOn "jquery"
        resource url:[dir:'js', file:'jquery.ba-hashchange.min.js'], disposition: 'head'
    }

    amplify {
        resource url:[dir:'js', file:'amplify.js'], disposition: 'head'
    }

}