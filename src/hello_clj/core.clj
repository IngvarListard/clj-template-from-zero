(ns hello-clj.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [hello-clj.app.core :refer [app-handler new-application]]
            [hello-clj.web-server :refer [new-web-server]]))

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
                         :web-server (new-web-server #'app-handler port))))

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
