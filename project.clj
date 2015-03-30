(defproject frrank "0.1.0-SNAPSHOT"
  :description "FRRank:  Display FIRST Robotics Rankings"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [clj-http "1.0.1"]
                 [compojure "1.3.2"]
                 [cheshire "5.4.0"]
                 [hiccup "1.0.5"]]
  :main frrank.core)
