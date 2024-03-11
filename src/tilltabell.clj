(ns tilltabell
  "Very specific code for conversion jobs. Strange asserts etc."
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]
            [instaparse.core :as insta]))

(defn post->csv-header
  "extract the tags of all the sub entities for a given subentity, which are used as headers"
  [post]
  (mapv (comp name first) (rest post)))

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

(defn posts-to-csv [posts {:keys [sort?]}]
  ;;create vector of vectors, the first row is the colum headers
  (into [(post->csv-header (first posts))]
        ;; sorterar (lexikalt) så om löpnummer i början blir detta sorteringen
        (cond-> (map post->row posts)
          sort? sort)))

;; this has to be re-evaluated if there are changes made to the objekt.bnf file.
(def objekt-parser (insta/parser (io/file "objekt.bnf")))

(defn convert-objekt
  "this function is more of a batch jobb.
  it is not super practical for interactive work with the parser"
  [export-index]
  (let [objekts (objekt-parser (slurp "objekt-raw.txt"))]
    (assert (= 1722 (count objekts)) "parsing of all the files should render in 1722 posts")
    ;; export edn
    (spit (str "objekt-parse-" export-index ".edn")
          (pr-str (vec objekts)))
    ;; export csv
    (with-open [writer (io/writer (str "objekt-parse-" export-index ".csv"))]
      (csv/write-csv writer (posts-to-csv objekts) {:sort? true}))))

(comment
  (convert-objekt "3")
  )

(comment
  ;; för att populera dokumentation tex
  (def objekts (objekt-parser (slurp "objekt-raw.txt")))
  (count objekts)

  ;; first 2 parsed objekts
  (take 2 objekts)
  
  ;; three CSV- rows
  (take 3 (posts-to-csv objekts))
  )

(def bibliography-parser (insta/parser (io/file "bibliografi.bnf")))

(defn convert-bibliography [export-index]
  (let [bibliography-posts (bibliography-parser (slurp "bibliografi-raw.txt"))]
    ;; export edn
    (spit (str "bibliografi-parse-" export-index ".edn")
          (pr-str (vec bibliography-posts)))
    (with-open [writer (io/writer (str "bibliografi-parse-" export-index ".csv"))]
      (csv/write-csv writer (posts-to-csv bibliography-posts {:sort? false})))))


(comment
  (convert-bibliography "3")
  )

(def arkiv-parser (insta/parser (io/file "fhp-arkiv.bnf")))

(defn convert-arkiv [export-index]
  (let [archive-posts (arkiv-parser (slurp "fhp-arkiv-raw.txt"))]
    ;; export edn
    (spit (str "fhp-arkiv-parse-" export-index ".edn")
          (pr-str (vec archive-posts)))
    (with-open [writer (io/writer (str "fhp-arkiv-parse-" export-index ".csv"))]
      (csv/write-csv writer (posts-to-csv archive-posts {:sort? false})))))


(comment (convert-arkiv "3"))


(comment "example data for understanding conversion functions"
         
         ;; this is one post from the fhp-arkiv-1.edn:

         (def example-post [:post
                            [:objekt "Föreningen Folkets Hus i Hagalund (Hagalunds Folkets husförening" "upa)"] ;; the two strings here means that the string "upa" was on a new line in the PDF.
                            [:omfång
                             "A. 1 vol."
                             "B. 2 hm"]
                            [:tid "A. 1909-1933 B. 1902-1966"]
                            [:institution
                             "A. Arbetarrörelsens arkiv och bibliotek, Stockholm"
                             "B. Solna stads centralarkiv"] ;; two rows in the PDF
                            [:typavhandlingar
                             "A. styrelse- och revisionsberättelser, andelsbevis"
                             "B. protokoll, register, ämnesordnade handlingar"] ;; two rows in the PDF
                            [:anmärkning
                             "ARAB arkiv nr 3022 plac: 22/20"
                             "NAD"
                             "ARAB [Samling] Folkets husföreningar vol. 1"]])


         ;; the :keywords as strings in the first position of the vectors after the initial :post
         (post->csv-header example-post)
         ["objekt"
          "omfång"
          "tid"
          "institution"
          "typavhandlingar"
          "anmärkning"]

         ;; here all the multistring contents are concatenated with a CSV_ROW_BREAK character in between them.
         ;; this is a data row in the CSV-file.
         (post->row example-post)
         ["Föreningen Folkets Hus i Hagalund (Hagalunds Folkets husförening upa)"
          "A. 1 vol. B. 2 hm"
          "A. 1909-1933 B. 1902-1966"
          "A. Arbetarrörelsens arkiv och bibliotek, Stockholm B. Solna stads centralarkiv"
          "A. styrelse- och revisionsberättelser, andelsbevis B. protokoll, register, ämnesordnade handlingar"
          "ARAB arkiv nr 3022 plac: 22/20 NAD ARAB [Samling] Folkets husföreningar vol. 1"]        

         )
