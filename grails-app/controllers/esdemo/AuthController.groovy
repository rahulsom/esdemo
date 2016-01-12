package esdemo

class AuthController {

    def login(String user) {
        session.user = user
        response.sendRedirect request.getHeader('referer')
    }

    def logout() {
        session.invalidate()
        response.sendRedirect request.getHeader('referer')
    }
}
