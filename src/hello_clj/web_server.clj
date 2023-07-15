(ns hello-clj.web-server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [com.stuartsierra.component :as component]))

(defrecord WebServer [handler-fn port                       ; parameters
                      application                           ; dependencies
                      http-server shutdown]                 ; state
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
                                    :port       port})
                   [:application]))
