/*
 * Copyright 2015 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package course;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bson.Document;
import spark.ModelAndView;
import spark.Request;
import spark.template.freemarker.FreeMarkerEngine;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static spark.Spark.*;

/**
 * This class encapsulates the controllers for the blog web application.  It delegates all interaction with MongoDB
 * to three Data Access Objects (DAOs).
 * <p/>
 * It is also the entry point into the web application.
 */
public class BlogController {
    private final Configuration cfg;
    private final UserDAO userDAO;
    private final SessionDAO sessionDAO;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            new BlogController("mongodb://localhost");
        }
        else {
            new BlogController(args[0]);
        }
    }

    public BlogController(String mongoURIString) throws IOException {
        final MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoURIString));
        final MongoDatabase blogDatabase = mongoClient.getDatabase("blog");

        userDAO = new UserDAO(blogDatabase);
        sessionDAO = new SessionDAO(blogDatabase);

        cfg = createFreemarkerConfiguration();
        port(8082);
        initializeRoutes();
    }

    private void initializeRoutes() throws IOException {
        // this is the blog home page
       get("/", (request, response) -> {
            String username = sessionDAO.findUserNameBySessionId(getSessionCookie(request));

            // this is where we would normally load up the blog data
            // but this week, we just display a placeholder.
            HashMap<String, String> root = new HashMap<>();

            return render(root, "blog_template.ftl");

        });

        // handle the signup post
        post("/signup", (request, response) -> {
            String email = request.queryParams("email");
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            String verify = request.queryParams("verify");

            HashMap<String, String> root = new HashMap<String, String>();
            root.put("username", StringEscapeUtils.escapeHtml4(username));
            root.put("email", StringEscapeUtils.escapeHtml4(email));

            if (validateSignup(username, password, verify, email, root)) {
                // good user
                System.out.println("Signup: Creating user with: " + username + " " + password);
                if (!userDAO.addUser(username, password, email)) {
                    // duplicate user
                    root.put("username_error", "Username already in use, Please choose another");
                    return render(root, "signup.ftl");
                }
                else {
                    // good user, let's start a session
                    String sessionID = sessionDAO.startSession(username);
                    System.out.println("Session ID is" + sessionID);

                    response.raw().addCookie(new Cookie("session", sessionID));
                    response.redirect("/welcome");
                    return null;
                }
            }
            else {
                // bad signup
                System.out.println("User Registration did not validate");
                return render(root, "signup.ftl");
            }

        });

        // present signup form for blog
        get("/signup", (request, response) -> {
            HashMap<String, String> root = new HashMap<>();

            // initialize values for the form.
            root.put("username", "");
            root.put("password", "");
            root.put("email", "");
            root.put("password_error", "");
            root.put("username_error", "");
            root.put("email_error", "");
            root.put("verify_error", "");

            return  render(root, "signup.ftl");
        });



        get("/welcome", (request, response) -> {
            String cookie = getSessionCookie(request);
            String username = sessionDAO.findUserNameBySessionId(cookie);

            if (username == null) {
                System.out.println("welcome() can't identify the user, redirecting to signup");
                response.redirect("/signup");
                return null;
            }
            else {
                HashMap<String, String> root = new HashMap<>();

                root.put("username", username);
                return render(root, "welcome.ftl");

            }

        });


        // present the login page
        get("/login", (request, response) -> {
            HashMap<String, String> root = new HashMap<>();

            root.put("username", "");
            root.put("login_error", "");
            return render(root, "login.ftl");
        });

        // process output coming from login form. On success redirect folks to the welcome page
        // on failure, just return an error and let them try again.
        post("/login", (request, response) -> {
            String username = request.queryParams("username");
            String password = request.queryParams("password");

            System.out.println("Login: User submitted: " + username + "  " + password);

            Document user = userDAO.validateLogin(username, password);

            if (user != null) {

                // valid user, let's log them in
                String sessionID = sessionDAO.startSession(user.get("_id").toString());

                if (sessionID == null) {
                    response.redirect("/internal_error");
                    return null;
                }
                else {
                    // set the cookie for the user's browser
                    response.raw().addCookie(new Cookie("session", sessionID));

                    response.redirect("/welcome");
                    return null;
                }
            }
            else {
                HashMap<String, String> root = new HashMap<>();


                root.put("username", StringEscapeUtils.escapeHtml4(username));
                root.put("password", "");
                root.put("login_error", "Invalid Login");
                return render(root, "login.ftl");
            }

        });


        // allows the user to logout of the blog
        get("/logout", (request, response) -> {
            String sessionID = getSessionCookie(request);

            if (sessionID == null) {
                // no session to end
                response.redirect("/login");
                return null;
            }
            else {
                // deletes from session table
                sessionDAO.endSession(sessionID);

                // this should delete the cookie
                Cookie c = getSessionCookieActual(request);
                c.setMaxAge(0);

                response.raw().addCookie(c);

                response.redirect("/login");
                return null;
            }

        });


        // used to process internal errors
        get("/internal_error", (request, response) -> {
            HashMap<String, String> root = new HashMap<>();

            root.put("error", "System has encountered an error.");
            return render(root, "error_template.ftl");
        });
    }

    // helper function to get session cookie as string
    private String getSessionCookie(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session")) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // helper function to get session cookie as string
    private Cookie getSessionCookieActual(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session")) {
                return cookie;
            }
        }
        return null;
    }

    // tags the tags string and put it into an array
    private ArrayList<String> extractTags(String tags) {

        // probably more efficent ways to do this.
        //
        // whitespace = re.compile('\s')

        tags = tags.replaceAll("\\s", "");
        String tagArray[] = tags.split(",");

        // let's clean it up, removing the empty string and removing dups
        ArrayList<String> cleaned = new ArrayList<String>();
        for (String tag : tagArray) {
            if (!tag.equals("") && !cleaned.contains(tag)) {
                cleaned.add(tag);
            }
        }

        return cleaned;
    }

    // validates that the registration form has been filled out right and username conforms
    public boolean validateSignup(String username, String password, String verify, String email,
                                  HashMap<String, String> errors) {
        String USER_RE = "^[a-zA-Z0-9_-]{3,20}$";
        String PASS_RE = "^.{3,20}$";
        String EMAIL_RE = "^[\\S]+@[\\S]+\\.[\\S]+$";

        errors.put("username_error", "");
        errors.put("password_error", "");
        errors.put("verify_error", "");
        errors.put("email_error", "");

        if (!username.matches(USER_RE)) {
            errors.put("username_error", "invalid username. try just letters and numbers");
            return false;
        }

        if (!password.matches(PASS_RE)) {
            errors.put("password_error", "invalid password.");
            return false;
        }


        if (!password.equals(verify)) {
            errors.put("verify_error", "password must match");
            return false;
        }

        if (!email.equals("")) {
            if (!email.matches(EMAIL_RE)) {
                errors.put("email_error", "Invalid Email Address");
                return false;
            }
        }

        return true;
    }

    private Configuration createFreemarkerConfiguration() {
        Configuration retVal = new Configuration();
        retVal.setClassForTemplateLoading(BlogController.class, "/freemarker");
        return retVal;
    }

    private String render(HashMap<String, String> model, String templatePath){
        return new FreeMarkerEngine(cfg).render(new ModelAndView(model, templatePath));
    }
}
