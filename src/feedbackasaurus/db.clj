(ns feedbackasaurus.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.json]
            [cheshire.core :as json])
  (:import [org.bson.types ObjectId])
  (:use monger.operators))

(mg/connect!
  (mg/server-address "127.0.0.1" 27017)
  (mg/mongo-options :threads-allowed-to-block-for-connection-multiplier 300))

(mg/set-db! (mg/get-db "feedbackasaurus"))

(defn new-upload [file-name uid]
  (mc/insert "uploads" {
    :_id (ObjectId.)
    :uid (ObjectId. uid)
    :file-name file-name
    :comments []
    })
  )

(defn uploads-by-user [uid]
  (mc/find-maps "uploads" {:uid (ObjectId. uid)})
  )

(defn get-upload [id]
  (mc/find-one-as-map "uploads" {:_id (ObjectId. id)})
  )

(defn get-upload-comments-json [id]
  (json/generate-string (:comments (get-upload id)))
  )

(defn add-comment-to-upload [id uid comment]
  (println "Pushing comment" comment "onto upload:" id "with user:" uid)
  (let [comment (assoc comment
                    :uid (ObjectId. uid)
                    :_id (ObjectId.))]
    (mc/update "uploads"
      {:_id (ObjectId. id)}
      {$push {:comments comment}})
    (json/generate-string comment)
    )
  )

(defn user [id]
  (if id
    (mc/find-one-as-map "users" {:_id (ObjectId. id)})
    nil
    )
  )

(defn set-user-name [id name]
  (mc/update "users" {:_id (ObjectId. id)} {:name name})
  )

(defn ensure-user [id]
  (if id
    id
    (let [uid (ObjectId.)]
      (mc/insert "users" {:_id uid})
      (str uid))
    )
  )
