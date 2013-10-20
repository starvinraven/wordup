(defproject wordup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                  [org.clojure/tools.logging "0.2.6"]
                  [http-kit "2.1.12"]
                  [compojure "1.1.5"]
                  [clj-logging-config "1.9.10"]
                  [org.clojure/data.json "0.2.3"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler wordup.web.handlers/app :init wordup.core/init-app}
  :main wordup.core
  :profiles {:uberjar {:aot :all}})
