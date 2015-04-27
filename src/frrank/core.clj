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

;(def server staging-server)
(def server prod-server)

(def db-spec {:classname "com.mysql.jdbc.Driver"
              :subprotocol "mysql"
              :subname "//127.0.0.1:3306/frrank"
              :user "root"})

(defn- parse-json [json] (parse-string json #(keyword (str/lower-case %))))

(def current-year (.getYear (org.joda.time.DateTime.)))
(def std-header {:debug true :headers { :authorization auth-token }})

;(defn http-query
  ;[url]

(defn get-season-info
  "Queries the frrank database for season information, or queries it from FIRST and populates the table"
  [] ; parameterize this with year
  (if-let [data-from-dbms (seq (jdbc/query db-spec ["select * from season where year = ?" current-year]))]
    (first data-from-dbms)
    (let [{:keys [body]}                                  (client/get (build-url server api-root "2015") std-header)
          {:keys [kickoff frcchampionship] :as year-info} (parse-json body)
          kickoffdt (dtfmt/parse (dtfmt/formatters :date-time-no-ms) kickoff)
          champdt   (dtfmt/parse (dtfmt/formatters :date-hour-minute-second) frcchampionship)
          season-rec (assoc year-info 
                            :year (.getYear kickoffdt)
                            :kickoff (dtfmt/unparse (dtfmt/formatters :mysql) kickoffdt)
                            :frcchampionship (dtfmt/unparse (dtfmt/formatters :mysql) champdt))]
      (jdbc/insert! db-spec :season season-rec)
      season-rec)))

(defn insert-team
  [team-map]
  (if-not (seq (jdbc/query db-spec ["select teamnumber from team where teamnumber = ?" (:teamnumber team-map)]))
    (jdbc/insert! db-spec :team team-map)
    ; TODO: (jdbc/update! db-spec :team team-map)
    ))

(defn fetch-teams-api
  []
  (let [season     (get-season-info)
        page-range (map inc (range (/ (:teamcount season) 50)))
        base-url   (str (build-url server api-root "teams" current-year) "?page=")]
    (flatten (map #(:teams (parse-json (:body (client/get (str base-url %) std-header)))) page-range))))

(defn initial-team-load
  []
  (let [teams (fetch-teams-api)]
    (doseq [team teams] (insert-team team))))

(defn fetch-events-api
  "/api/v1.0/events/{season}{?eventCode}{?teamNumber}{&districtCode}{&excludeDistrict}"
  []
  (let [season (get-season-info)]
    (parse-json (:body (client/get (build-url server api-root "events" current-year) std-header)))))

(defn initial-event-load
  [events]
  (let [insertfn (fn insert-event [event]
                   (if-not (seq (jdbc/query db-spec ["select code from event where code = ?" (:code event)]))
                     (jdbc/insert! db-spec :event event)))]
    (map insertfn (:events events))))


(defn fetch-event-ranking
  "/api/v1.0/rankings/{season}/{eventCode}{?teamNumber}{&top}"
  [event-code]
  (let [season   (get-season-info)
        url      (build-url server api-root "rankings" current-year event-code)
        rankings (:rankings (parse-json (:body (client/get url std-header))))]
    (map #(assoc % :event_code event-code) rankings)))

(defn insert-rankings
  [rankings]
  (let [insertfn (fn insert-ranking [ranking] 
                   (if-not (seq (jdbc/query db-spec 
                                            ["select event_code, teamnumber from event_rank where event_code = ? and teamnumber = ?" 
                                             (:event_code ranking) (:teamnumber ranking)]))
                     (jdbc/insert! db-spec :event_rank ranking)))]
    (doseq [ranking rankings] (insertfn ranking))))

(defn initial-ranking-load
  []
  (let [eventcds (jdbc/query db-spec "select code from event where dateEnd < now()")]
    (doseq [{:keys [code]} eventcds] (insert-rankings (fetch-event-ranking code)))))


(defn init-database
  []
  (do
    (get-season-info)
    (initial-team-load)
    (initial-event-load (fetch-events-api))
    (initial-ranking-load)))

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
;  (let [page (frrank server 2015)]
;    (println page)
;    (spit "out.html" page)))
;
;
;;(defroutes routes
;  ;GET "/" [] (team-ranking))

