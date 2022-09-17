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
import java.util.List;

public class userServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        this.userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Handle unauthorized access (must be logged in to access this file
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/account/login.jsp");
            return;
        }

        User loggedUser = (User) session.getAttribute("user");

        // Handle url routing
        if (req.getPathInfo() != null && req.getPathInfo().length() > 1) {
            // Remove the first character and then split the url /@servlet/hello/world/ -> [hello, world, ]
            String urls[] = req.getPathInfo().substring(1).split("/");

            // Admin area
            if(loggedUser.isAdmin()) {
                // Route -> /u/account-list
                if (urls[0].equals("account-list")) handleAccountList(req, resp);
                // Route -> /u/@username
                if(urls[0].startsWith("@")) {
                    User found = userService.findUserByUsername(urls[0].substring(1));

                    if (found != null) displayMessage(req, resp, "Display user profile for @" + found.getUsername());
                    else displayMessage(req, resp, "User could not be found!");
                }
            // Regular user
            } else if (!loggedUser.isAdmin()) {
                // Route -> /u/@username
                if(urls[0].startsWith("@")) {
                    User found = userService.findUserByUsername(urls[0].substring(1));

                    if (found != null && found.getUsername().equals(loggedUser.getUsername())) displayMessage(req, resp, "Display user profile for @" + found.getUsername());
                    else displayMessage(req, resp, "There was an error while trying to find the user!");
                }
            }

            displayMessage(req, resp,"Sorry, admin account is needed to access this page!");
        }

        resp.sendRedirect("/");
    }

    private void displayMessage(HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
        RequestDispatcher dispatcher = req.getRequestDispatcher(req.getContextPath() + "/WEB-INF/View/display-message.jsp?msg=" + msg);
        dispatcher.forward(req, resp);
    }

    private void handleAccountList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> users = userService.getAllUsers();

        req.setAttribute("users", users);
        RequestDispatcher dispatcher = req.getRequestDispatcher(req.getContextPath() + "/WEB-INF/View/account/account-list.jsp");
        dispatcher.forward(req, resp);
    }
}