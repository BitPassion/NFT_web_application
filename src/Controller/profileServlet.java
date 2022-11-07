package Controller;

import Domain.User;
import Service.UserService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class profileServlet extends HttpServlet {
    private UserService userService;
    private static String HOST = "http://localhost:8080/";
    private static String AUTHENTICATE = "authenticateServlet";

    @Override
    public void init() {this.userService = new UserService();}
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher dispatcher = req.getRequestDispatcher(req.getContextPath() + "WEB-INF/View/account/profile.jsp");
        dispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle url routing
        // for example: [currentServlet]/someurl/..
        if (req.getPathInfo() != null && req.getPathInfo().length() > 1) {
            // Remove the first character and then split the url /hello/world -> [hello, world]
            String urls[] = req.getPathInfo().substring(1).split("/");

            if (urls[0].equals("changePassword")) {
                passwordChange(req, resp);
            }

            else if (urls[0].equals("changePersonalInfo")) {
                personalInformationChange(req, resp);
            }

            else if (urls[0].equals("deleteAccount")) {
                deleteAccount(req, resp);
            }
        }
    }

    private void passwordChange(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //check that the oldPassword is correct
        User response = userService.authenticateUser(req.getParameter("username"), req.getParameter("oldPassword"));

        // return error if the old password confirmation fails or the update fails
        if(response == null || !userService.updateUserPassword(req.getParameter("username"), req.getParameter("newPassword"))) {
            resp.sendRedirect(req.getContextPath() + "/profile?errmsg=2");
        }
        else {
            resp.sendRedirect("/profile?errmsg=4");
        }
    }
    private void personalInformationChange(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User response = userService.authenticateUser(req.getParameter("usernameValidate"), req.getParameter("passwordValidate"));

        if (response != null) {
            String username = req.getParameter("username");
            String email = req.getParameter("email");
            String fName = req.getParameter("fName");
            String lName = req.getParameter("lName");
            int id = response.getId();

            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new RuntimeException("This user must have a valid session to change personal information.");
            }

            if (userService.updatePersonalInfo(req, fName, lName, email, username, id)) {
                //update this user's info
                resp.sendRedirect(req.getContextPath() + "/profile?errmsg=3");
                return;
            }
        }
        resp.sendRedirect(req.getContextPath() + "/profile?errmsg=1");
    }

    private void deleteAccount(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        // Handle unauthorized access (must be admin)
        User response = userService.authenticateUser(req.getParameter("usernameDelete"), req.getParameter("passwordDelete"));

        if (response != null) {
            boolean deletionSuccess = userService.deleteUserById(String.valueOf(response.getId()));
            if (deletionSuccess) {
                HttpSession session = req.getSession(false);
                session.invalidate();

                RequestDispatcher dispatcher = req.getRequestDispatcher(req.getContextPath() + "web/WEB-INF/View/dashboard.jsp");
                dispatcher.forward(req, resp);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/profile?errmsg=1");
        }
    }
}