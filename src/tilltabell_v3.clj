(ns tilltabell-v3
  "skript för att importera rå-texter och med specificerad abnf-syntax
  konvertera dem först till en vektor-datastruktur som skrivs som EDN-fil, sedan exportera som CSV"
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]
            [instaparse.core :as insta]))

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
        ;; sorterar (lexikalt) så om löpnummer i början blir detta sorteringen
        (sort (map post->row posts))))

(comment
  (def platsläsare (insta/parser (io/file "posterv3-working.bnf")))
  (def alla-platser (platsläsare (slurp "allaplatser-with-v3.txt")))

  (assert (= 1722   (count alla-platser)) "läsning av platsfilen ska resultera i 1722 poster.")

  (spit "allaplatser-parse1.edn" (pr-str (vec alla-platser)))
  (with-open [writer (io/writer "allaplatser-parse1.csv")]
    (csv/write-csv writer
                   (posts-to-csv alla-platser))))

(comment
  ;; för att populera dokumentation tex
  (take 3 (posts-to-csv alla-platser))
(take 2 alla-platser))

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

(comment "FHP arkivförteckning-export"

(def arkivläsare (insta/parser (io/file "fhp-arkivv3.bnf")))
(def fhp-arkiv (arkivläsare (slurp "fhp-arkiv-1.txt")))
(spit "fhp-arkiv-1.edn" (pr-str (vec fhp-arkiv)))

(with-open [writer (io/writer "fhp-arkiv-1.csv")]
  (csv/write-csv writer
                 (into [(post->csv-header (first bibliografi))]
                       ;; Arkiv vill vi ha i samma ordning som pdf-en
                       (map post->row bibliografi))))

)
