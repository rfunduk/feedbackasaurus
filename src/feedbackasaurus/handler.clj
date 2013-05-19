(ns feedbackasaurus.handler
  (:use [ring.middleware session params multipart-params]
        [ring.middleware.session cookie]
        [hiccup.middleware :only [wrap-base-url]]
        [dieter.core :only [asset-pipeline]]
        [compojure.core])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [feedbackasaurus.views :as views]))

(defroutes main-routes
  (views/new-upload "/")
  (views/upload "/upload")
  (views/show "/show/:id")

  (views/fetch-comments "/comments/:id")
  (views/add-comment "/comments/:id")

  (views/set-name "/user")

  (route/resources "/")
  (route/not-found (views/not-found)))

(def app
  (-> #'main-routes
      (wrap-session {:cookie-attrs {:max-age 100000000000}
                     :store (cookie-store {:key "asdjfsdfadsfkks9"})})
      handler/site
      (asset-pipeline {:engine :v8})
      wrap-params
      wrap-multipart-params
      wrap-base-url
    )
  )
