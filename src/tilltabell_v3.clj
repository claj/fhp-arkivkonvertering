(ns tilltabell-v3
  "Det ser ut som att den nya exporten är betydligt bättre
  \"Arkivhandlingar\" förekommer två gånger, men det är ett lösbart problem.
"
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]
            [instaparse.core :as insta]
            []clojure.data.csv :as csv]))

(defn post->csv-header
"extract the tags of all the sub entities"
[post]
 (mapv (comp name first ) (rest post)))

(def CSV_ROW_BREAK " ")

(defn texti [item]
  (assert (keyword? (first item)))
  (let [items (rest item)]
    ;; there is no way to add linebreaks in a csv-cell, put all text in one line if multiple strings.
    (clojure.string/join CSV_ROW_BREAK (map clojure.string/trim items))))

(defn post->row [post]
  ;; a post starts with a :post tag
  (assert (= :post (first post)))
  ;; the rest of the tags are already known and in a given order,
  ;; they are added by header
  (mapv texti (rest post)))

(defn posts-to-csv [posts]
  (into [(post->csv-header (first posts))]
        (sort (map post->row posts))))

(def postläsare (insta/parser (io/file "posterv3-working.bnf")))

(comment
  
  (def alla (postläsare (slurp "allaplatser-with-v3.txt")))

  (assert (= 1722   (count alla)))

  (spit "allaplatser-parse1.edn" (pr-str (vec alla)))
(with-open [writer (io/writer "allaplatser-parse1.csv")]
  (csv/write-csv writer
                 (posts-to-csv alla))))

(comment 
)
(comment (post->csv-header (first alla)))






;;(post->row (first alla))



(def csv-data (posts-to-csv alla))
(comment 
(take 3 csv-data)
(take 2 alla))

(comment

(def bibläsare (insta/parser (io/file "bibliografiv3.bnf")))
(def bibliografi (bibläsare (slurp "bibliografi-1.txt")))

(spit "bibliografi-1.edn" (pr-str (vec bibliografi)))

(take 3 (posts-to-csv bibliografi))

(with-open [writer (io/writer "bibliografi-1.csv")]
  ;; no other sorting for bibliografi
  (csv/write-csv writer
                 (into [(post->csv-header (first bibliografi))]
                       (map post->row bibliografi))))

)

(take 3 (map post->row bibliografi))

(comment with-)

