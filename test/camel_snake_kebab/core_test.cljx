(ns camel-snake-kebab.core-test
  (:require [clojure.string :refer [split]]
            [camel-snake-kebab.core :as csk]
            #+clj [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t])
  #+cljs (:require-macros [cemerick.cljs.test :refer [deftest is testing are]])
  #+clj (:import (clojure.lang ExceptionInfo)))

(def zip (partial map vector))

(deftest format-case-test
  (testing "examples"
    (are [x y] (= x y)
      'FluxCapacitor  (csk/->CamelCase 'flux-capacitor)
      "I_AM_CONSTANT" (csk/->SNAKE_CASE "I am constant")
      :object-id      (csk/->kebab-case :object_id)
      "X-SSL-Cipher"  (csk/->HTTP-Header-Case "x-ssl-cipher")
      :object-id      (csk/->kebab-case-keyword "object_id")))

  (testing "rejection of namespaced keywords and symbols"
    (is (thrown? ExceptionInfo (csk/->CamelCase (keyword "a" "b"))))
    (is (thrown? ExceptionInfo (csk/->CamelCase (symbol  "a" "b")))))

  (testing "all the type preserving functions"
    (let
      [inputs    ["FooBar"
                  "fooBar"
                  "FOO_BAR"
                  "Foo_bar"
                  "foo_bar"
                  "foo-bar"
                  "Foo_Bar"]
       functions [csk/->CamelCase
                  csk/->camelCase
                  csk/->SNAKE_CASE
                  csk/->Snake_case
                  csk/->snake_case
                  csk/->kebab-case
                  csk/->Camel_Snake_Case]
       formats   [identity keyword symbol]]

      (doseq [input inputs, format formats, [output function] (zip inputs functions)]
        (is (= (format output) (function (format input)))))))

  (testing "some of the type converting functions"
    (are [x y] (= x y)
      :FooBar   (csk/->CamelCaseKeyword  'foo-bar)
      "FOO_BAR" (csk/->SNAKE_CASE_STRING :foo-bar)
      'foo-bar  (csk/->kebab-case-symbol "foo bar"))))

(deftest http-header-case-test
  (are [x y] (= x (csk/->HTTP-Header-Case y))
    "User-Agent"       "user-agent"
    "DNT"              "dnt"
    "Remote-IP"        "remote-ip"
    "TE"               "te"
    "UA-CPU"           "ua-cpu"
    "X-SSL-Cipher"     "x-ssl-cipher"
    "X-WAP-Profile"    "x-wap-profile"
    "X-XSS-Protection" "x-xss-protection"))
