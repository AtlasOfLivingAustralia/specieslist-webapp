// Place your Spring DSL code here
beans = {
    // Custom message source
    messageSource(org.springframework.context.support.ReloadableResourceBundleMessageSource) {
        basenames = [
                "file:///var/opt/atlas/i18n/specieslist-webapp/messages",
                "file:///opt/atlas/i18n/specieslist-webapp/messages",
                "WEB-INF/grails-app/i18n/messages"
        ] as String[]
        cacheSeconds = (60 * 60 * 6) // 6 hours
        useCodeAsDefaultMessage = false
    }
}
