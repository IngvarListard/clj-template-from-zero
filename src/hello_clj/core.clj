(ns hello-clj.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [GET POST let-routes]]
            [com.stuartsierra.component :as component]
            [compojure.coercions :refer [as-int]]
            [compojure.core :refer [GET POST let-routes]]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :as ring-defaults]
            [rum.core :as rum]
            [hello-clj.ui :as ui]))

(defrecord Application [config   ; configuration (unused)
                        database ; dependency
                        state]   ; behavior
  component/Lifecycle
  (start [this]
    (assoc this :state "Running"))
  (stop [this]
    (assoc this :state "Stopped")))

(defn new-application
  [config]
  (component/using (map->Application {:config config})
                   [
                    ;:database
                    ]))

(defrecord WebServer [handler-fn port        ; parameters
                      application            ; dependencies
                      http-server shutdown]  ; state
  component/Lifecycle
  (start [this]
    (if http-server
      this
      (assoc this
        :http-server (run-jetty (handler-fn application)
                                {:port port :join? false})
        :shutdown (promise)))))

(defn new-web-server
  [handler-fn port]
  (component/using (map->WebServer {:handler-fn handler-fn
                                    :port port})
                   [:application]))

(defn- add-app-component
  "Middleware to add your application component into the request. Use
  the same qualified keyword in your controller to retrieve it."
  [handler application]
  (fn [req]
    (handler (assoc req :application/component application))))

(defn my-middleware
  "This middleware runs for every request and can execute before/after logic.

  If the handler returns an HTTP response (like a redirect), we're done.
  Else we use the result of the handler to render an HTML page."
  [handler]
  (fn [req]
    (let [resp (handler req)]
      resp)))

(defn middleware-stack
  "Given the application component and middleware, return a standard stack of
  Ring middleware for a web application."
  [app-component app-middleware]
  (fn [handler]
    (-> handler
        (app-middleware)
        (add-app-component app-component)
        (ring-defaults/wrap-defaults (-> ring-defaults/site-defaults
                                         ;; disable XSRF for now
                                         (assoc-in [:security :anti-forgery] false)
                                         ;; support load balancers
                                         (assoc-in [:proxy] true))))))

(rum/defc my-comp [s]
  [:div s])

(defn handler [request]

  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (rum/render-html (ui/page {}
                                      [:button.bg-blue-500.hover:bg-blue-700.text-white.font-bold.py-2.px-4.rounded "heool"]))})

(defn my-handler
  "Given the application component, return middleware for routing.

  We use let-routes here rather than the more usual defroutes because
  Compojure assumes that if there's a match on the route, the entire
  request will be handled by the function specified for that route.

  Since we need to deal with page rendering after the handler runs,
  and we need to pass in the application component at start up, we
  need to define our route handlers so that they can be parameterized."
  [application]
  (let-routes [wrap (middleware-stack application #'my-middleware)]
    (GET  "/"                        []              (wrap #'handler))
    ;; horrible: application should POST to this URL!
    ;(GET  "/user/delete/:id{[0-9]+}" [id :<< as-int] (wrap #'user-ctl/delete-by-id))
    ;; add a new user:
    ;(GET  "/user/form"               []              (wrap #'user-ctl/edit))
    ;; edit an existing user:
    ;(GET  "/user/form/:id{[0-9]+}"   [id :<< as-int] (wrap #'user-ctl/edit))
    ;(GET  "/user/list"               []              (wrap #'user-ctl/get-users))
    ;(POST "/user/save"               []              (wrap #'user-ctl/save))
    ;; this just resets the change tracker but really should be a POST :)
    ;(GET  "/reset"                   []              (wrap #'user-ctl/reset-changes))
    (route/resources "/")
    (route/not-found "Not Found")))

(defn new-system
  "Build a default system to run. In the REPL:

  (def system (new-system 8888))

  (alter-var-root #'system component/start)

  (alter-var-root #'system component/stop)

  See the Rich Comment Form below."
  ([port] (new-system port true))
  ([port repl]
   (component/system-map :application (new-application {:repl repl})
                         ;:database    (model/setup-database)
                         :web-server  (new-web-server #'my-handler port))))

(defonce ^:private repl-system (atom nil))

(defn -main
  [& [port]]
  (let [port (or port (get (System/getenv) "PORT" 8080))
        port (cond-> port (string? port) Integer/parseInt)]
    (println "Starting up on port" port)
    ;; start the web server and application:
    (-> (component/start (new-system port false))
        ;; then put it into the atom so we can get at it from a REPL
        ;; connected to this application:
        (->> (reset! repl-system))
        ;; then wait "forever" on the promise created:
        :web-server :shutdown deref)))
