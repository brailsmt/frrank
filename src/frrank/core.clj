(ns frrank.core
  (:require 
    [clojure.string :as str]
    [clj-http.client :as client]
    [compojure.route :as route]
    [hiccup.core :as hc]
    [hiccup.page :as page]))

(defn build-url [& parts] (str/join "/" parts))

(def api-root "api/v1.0")
(def staging-server "https://frc-staging-api.usfirst.org")
(def prod-server  "https://frc-api.usfirst.org")
(def auth-token "Basic YnJhaWxzbXQ6QTA2RjlERDAtNTI4OC00OTBGLUJFMDktQjM2MzRDMjlBOTJDCg==")

(defn team-ranking 
  [server year]
  (let [response (client/get (build-url [server api-root server year]) {:debug true :headers { :authorization auth-token }})]
  ))


(defn frrank 
  [server year]
  (let [response (client/get (build-url server api-root year) {:debug true :headers { :authorization auth-token }})]
    (page/html5 [:body [:h1 (str "FRC " year "!!!")] [:pre '(response)]])))

(defn -main 
  [& args]
  (let [page (frrank staging-server 2015)]
    (println page)
    (spit "out.html" page)))


;(defroutes routes
  ;GET "/" [] (team-ranking))
