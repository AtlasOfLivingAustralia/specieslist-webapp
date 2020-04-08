// Place your Spring DSL code here
beans = {
    // Custom message source
    messageSource(org.springframework.context.support.ReloadableResourceBundleMessageSource) {
        basenames = [
                "file:///var/opt/atlas/i18n/specieslist-webapp/messages",
                "file:///opt/atlas/i18n/specieslist-webapp/messages",
                "file:grails-app/i18n/messages",
                "file:WEB-INF/classes/messages",
                "classpath:messages"
        ] as String[]
        defaultEncoding = "UTF-8"
        cacheSeconds = (60 * 60 * 6) // 6 hours
        useCodeAsDefaultMessage = false
    }
}
