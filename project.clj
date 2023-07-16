(defproject hello-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-defaults "0.3.4"]
                 [compojure "1.7.0"]
                 [com.stuartsierra/component "1.1.0"]
                 [rum "0.12.10"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [com.mchange/c3p0 "0.9.5.5"]
                 [com.github.seancorfield/honeysql "2.4.1011"]
                 [org.xerial/sqlite-jdbc "3.39.3.0"]]
  :main ^:skip-aot hello-clj.core
  :resource-paths ["resources"
                   "target/resources"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
