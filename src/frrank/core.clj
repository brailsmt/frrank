(ns frrank.core
  (:require 
    [clojure.string :as str]
    [clj-http.client :as client]
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

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/frrank"
               :user "frrank"
               :password "frrank"})

(defn create-season-tbl
  "Create the season table"
  []
  (jdbc/create-table
    :season
    [:year :integer "primary key"]
    [:frcChampionship :datetime]
    [:eventCount :integer]
    [:gameName "varchar(128)"]
    [:kickoff :datetime]
    [:rookieStart :integer]
    [:teamCount :integer]))

(defn create-teams-tbl
  "Create the teams table"
  []
  (jdbc/create-table
    :teams
    [:teamNumber :integer "primary key"]
    [:rookieYear :integer]
    [:stateProv "varchar(10)"]
    [:robotName "varchar(256)"]
    [:city "varchar(50)"]
    [:nameFull "varchar(1024)"]
    [:nameShort "varchar(256)"]
    [:districtCode "varchar(16)"]
    [:country "varchar(32)"]))

(defn create-database
  [dbconn]
  (jdbc/with-connection dbconn (jdbc/transaction 
      (create-season-tbl)
      (create-teams-tbl)
      )))


(defn- parse-json [json] (parse-string json (fn [k] (keyword k))))
(def year-info 
  (let [response (client/get (build-url staging-server api-root "2015") {:headers { :authorization auth-token }})]
        (assoc (parse-json (:body response)) :full response)))

(def team-listing (atom {}))

(defn team-list
  [server year]
  (let [page-range (map inc (range (/ (:teamCount year-info) 50)))
        base-url (str (build-url server api-root "teams" year) "?page=")
        tlist []
        full-list (flatten (map #(cons tlist (:teams (parse-json (:body (client/get (str base-url %) {:debug true :headers { :authorization auth-token }}))))) page-range))]
    (swap! (map 

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
