(ns hello-clj.db
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(def ^:private db-spec
  "SQLite database connection spec."
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "resources/db/db.sqlite3"
   :create      true})

(defn pool
  [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               ;(.setUser (:user spec))
               ;(.setPassword (:password spec))
               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    cpds))
(defrecord Database [db-spec                                ; configuration
                     datasource]                            ; state

  component/Lifecycle
  (start [this]
    (if datasource
      this                                                  ; already initialized
      (assoc this :datasource (pool db-spec))))
  (stop [this]
    (assoc this :datasource nil))

  ;; allow the Database component to be "called" with no arguments
  ;; to produce the underlying datasource object
  clojure.lang.IFn
  (invoke [_] datasource))

(defn setup-database [] (map->Database {:db-spec db-spec}))