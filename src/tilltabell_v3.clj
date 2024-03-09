(ns tilltabell-v3
  "Det ser ut som att den nya exporten är betydligt bättre
  \"Arkivhandlingar\" förekommer två gånger, men det är ett lösbart problem.
"
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]
            [instaparse.core :as insta]))

(def postläsare (insta/parser (io/file "posterv3-working.bnf")))



(def alla (postläsare (slurp "../allaplatser1.txt")))

(count alla)
;; 1722
