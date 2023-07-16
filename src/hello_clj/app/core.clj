(ns hello-clj.app.core
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer [GET POST let-routes]]
            [hello-clj.middleware :as middleware]
            [compojure.route :as route]
            [rum.core :as rum]
            [hello-clj.ui :as ui]))


(defrecord Application [config                              ; configuration (unused)
                        database                            ; dependency
                        state]                              ; behavior
  component/Lifecycle
  (start [this]
    (assoc this :state "Running"))
  (stop [this]
    (assoc this :state "Stopped")))

(defn new-application
  [config]
  (component/using (map->Application {:config config})
                   [:database]))

(defn handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (rum/render-html (ui/page {}
                                      [:div
                                       [:button.bg-blue-500.hover:bg-blue-700.text-white.font-bold.py-2.px-4.rounded
                                        {:hx-post    "/"
                                         :hx-trigger "click"
                                         :hx-target  "#test"
                                         :hx-swap    "outerHTML"}
                                        "heool"]
                                       [:div#test]]))})

(defn h2 [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (rum/render-html [:div "Yay! Hello world"])})

(defn app-handler
  "Given the application component, return middleware for routing.

  We use let-routes here rather than the more usual defroutes because
  Compojure assumes that if there's a match on the route, the entire
  request will be handled by the function specified for that route.

  Since we need to deal with page rendering after the handler runs,
  and we need to pass in the application component at start up, we
  need to define our route handlers so that they can be parameterized."
  [application]
  (let-routes [wrap (middleware/middleware-stack application #'middleware/my-middleware)]
              (GET "/" [] (wrap #'handler))
              (POST "/" [] (wrap #'h2))
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
