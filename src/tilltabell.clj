(ns tilltabell
  "Konverteringsjobbet:

  Filemaker -> skriv lista av poster till pdf
  pdf -> text mha Mathematicas funktioner för detta

  text -> csv-fil

  schemat ska tas fram "
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]))

(def platser "resources/2024-03-06 platser.txt")

(def rader (line-seq (io/reader platser)))

(defn löpnr? [row] (str/starts-with? row "Löpnummer"))

(take 3 (partition-by löpnr? rader))


