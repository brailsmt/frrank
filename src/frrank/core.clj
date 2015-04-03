(ns frrank.core
  (:require 
    [clojure.string :as str]
    [clj-http.client :as client]
    [clj-time.core :as dt]
    [clj-time.format :as dtfmt]
    [compojure.route :as route]
    [clojure.java.jdbc :as jdbc]
    [clojure.pprint :as pp]
    [cheshire.core :refer :all]
    [hiccup.core :as hc]
    [hiccup.page :as page]))

(defn build-url [& parts] (str/join "/" parts))

(def api-root "api/v1.0")
(def staging-server "https://frc-staging-api.usfirst.org")
(def prod-server  "https://frc-api.usfirst.org")
(def auth-token "Basic YnJhaWxzbXQ6QTA2RjlERDAtNTI4OC00OTBGLUJFMDktQjM2MzRDMjlBOTJDCg==")

(def db-spec {:classname "com.mysql.jdbc.Driver"
              :subprotocol "mysql"
              :subname "//127.0.0.1:3306/frrank"
              :user "root"})

(defn- parse-json [json] (parse-string json #(keyword (str/lower-case %))))

(def current-year (.getYear (org.joda.time.DateTime.)))

(defn get-or-load-season-info
  "Queries the frrank database for season information, or queries it from FIRST and populates the table"
  [] ; parameterize this with year
  (if-let [data-from-dbms (seq (jdbc/query db-spec ["select * from season where year = ?" current-year]))]
    (first data-from-dbms)
    (let [{:keys [body]}                                  (client/get (build-url staging-server api-root "2015") {:headers { :authorization auth-token }})
          {:keys [kickoff frcchampionship] :as year-info} (parse-json body)
          kickoffdt (dtfmt/parse (dtfmt/formatters :date-time-no-ms) kickoff)
          champdt   (dtfmt/parse (dtfmt/formatters :date-hour-minute-second) frcchampionship)
          season-rec (assoc year-info 
                            :year (.getYear kickoffdt)
                            :kickoff (dtfmt/unparse (dtfmt/formatters :mysql) kickoffdt)
                            :frcchampionship (dtfmt/unparse (dtfmt/formatters :mysql) champdt))]
      (jdbc/insert! db-spec :season season-rec)
      season-rec)))

(def team-listing (atom {}))

;(defn team-list
;  [server year]
;  (let [page-range (map inc (range (/ (:teamCount year-info) 50)))
;        base-url (str (build-url server api-root "teams" year) "?page=")
;        tlist []
;        full-list (flatten (map #(cons tlist (:teams (parse-json (:body (client/get (str base-url %) {:debug true :headers { :authorization auth-token }}))))) page-range))]
;    (swap! (map 

;
;(defn team-ranking 
;  [server year]
;  (let [response (client/get (build-url server api-root server year) {:debug true :headers { :authorization auth-token }})]))
;
;
;(defn frrank 
;  [server year]
;  (let [nteams (:teamCount year-info)]
;    (prn nteams)))
;
;(defn -main 
;  [& args]
;  (let [page (frrank staging-server 2015)]
;    (println page)
;    (spit "out.html" page)))
;
;
;;(defroutes routes
;  ;GET "/" [] (team-ranking))
