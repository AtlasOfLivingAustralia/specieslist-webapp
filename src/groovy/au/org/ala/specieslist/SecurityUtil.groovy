package au.org.ala.specieslist

class SecurityUtil {

    def localAuthService
    def authService

    boolean checkListAccess(String listId) {
        boolean canAccess = true

        if (!localAuthService.isAdmin()) {
            SpeciesList list = SpeciesList.findByDataResourceUid(listId)
            if (list.isPrivate) {
                String userId = authService.getUserId()
                if (!userId || (list.userId != userId && !list.editors?.contains(userId))) {
                    canAccess = false
                }
            }
        }

        canAccess
    }
}
