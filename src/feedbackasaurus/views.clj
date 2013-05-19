(ns feedbackasaurus.views
  (:use [hiccup core page]
        compojure.core
        [dieter.core :only (link-to-asset)])
  (:require [feedbackasaurus.db :as db]
            [clojure.java.io :as io]
            [ring.util.response :as r])
  (:import [java.io File])
  )

(defn- with-session [original-session block]
  (let [
    {response :response, session :session} block
    user-id (db/ensure-user (if (:uid session) (:uid session) (:uid original-session)))
    new-session (merge original-session {:uid user-id} session)
    ]
    ; (println "RESPONSE:" response)
    (println "SESSION:" new-session)
    (-> (if (= (type response) java.lang.String) (r/response response) response)
        (assoc :session new-session))
    )
  )

(defn- layout [& body]
  (html5
    [:head
      [:title "Feedbackasaurus"]
      (include-js (link-to-asset "manifest.js.dieter" {:engine :v8}))
      (include-css (link-to-asset "manifest.css.dieter" {:engine :v8}))
      ]
    [:body
      [:div {:class "container"}
        [:h1 [:a {:href "/"} "Feedbackasaurus"]]
        body
        ]
      ]
    )
  )

(defn- existing-uploads [uid]
  (let [uploads (db/uploads-by-user uid)]
    [:ul {:id "uploads"}
      [:lh (if (empty? uploads) "No uploads!" "Your uploads:")]
      (for [upload uploads]
        [:li
          [:a {:href (str "/show/" (str (:_id upload)))} (:file-name upload)]
          ]
        )
      ]
    )
  )

(defn set-name-form [return-path]
  [:form {:action "/user" :method "post" :id "set-name-form"}
    [:input {:type "hidden" :name "return-path" :value (or return-path "/")}]
    [:label "What's your name?"]
    [:input {:name "name"}]
    [:input {:type "submit" :value "submit" :name "submit"}]
    ]
  )

(defn set-name [path]
  (POST path {session :session, params :params}
    (with-session session
      (do
        (db/set-user-name (:uid session) (:name params))
        {:response (r/redirect (:return-path params))}
        )
      )
    )
  )

(defn new-upload [path]
  (GET path {session :session}
    (with-session session
      (if (:uid session)
        {:response (layout
          (let [name (:name (db/user (:uid session)))]
            (if name
              [:p (str "Hello, " name)]
              (set-name-form)
              )
            )
          [:form {:id "new-upload" :action "/upload" :method "post" :enctype "multipart/form-data"}
            [:input {:name "file" :type "file" :size "20"}]
            [:input {:type "submit" :name "submit" :value "submit"}]]
          (existing-uploads (:uid session))
          )}
        {:response (r/redirect "/")}
        )
      )
    )
  )

(defn upload [path]
  (POST path {params :params, session :session}
    (with-session session
      (do
        (println params)
        (let [
          file (params :file)
          file-name (file :filename)
          size (file :size)
          actual-file (file :tempfile)
          dest (File. (format "%s/resources/public/uploads/%s"
                                (System/getProperty "user.dir")
                                file-name))
          ]
          (println file)
          (io/copy actual-file dest)
          (db/new-upload file-name (:uid session))
          {:response (r/redirect "/"), :session session}
          )
        )
      )
    )
  )

(defn show [path]
  (GET path [id :as {session :session}]
    (with-session session
      (do
        (let [
          upload (db/get-upload id)
          name (:name (db/user (:uid session)))
          ]
          {:response (layout
            (if name [:p (str "Hello, " name)] (set-name-form (str "/show/" id)))
            [:div {:id "upload-box" :class "clearfix"}
              [:img {:id "upload", :rel id, :src (str "/uploads/" (:file-name upload))}]
              [:form {:id "new-comment" :style "opacity: 0;"}
                [:textarea {:name "comment"}]
                [:input {:type "submit" :name "submit" :value "Post Comment"}]]]
            )}
          )
        )
      )
    )
  )

(defn fetch-comments [path]
  (GET path [id :as {session :session}]
    (with-session session
      {:response (db/get-upload-comments-json id)}
      )
    )
  )

(defn add-comment [path]
  (POST path [id :as {params :params, session :session}]
    (with-session session
      (do
        (println "COMMENT:" id (:uid session) (:comment params))
        {:response (db/add-comment-to-upload id (:uid session) (:comment params))}
        )
      )
    )
  )

(defn not-found []
  (html5
    [:head
      [:title "Feedbackasaurus Error"]
      (include-css (link-to-asset "manifest.css.dieter" {:engine :v8}))
      ]
    [:body
      [:h1 "Feedbackasaurus Error"]
      [:h2 "404"]]
      )
  )
