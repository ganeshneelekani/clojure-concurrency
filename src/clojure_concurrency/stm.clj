(ns clojure-concurrency.stm)

(defn deposit [balance amount]
  (dosync
   (println "Ready to deposit..." amount " " @balance)
   (let [current-balance @balance]
     (println "simulating delay in deposit...")
     (. Thread sleep 2000)
     (alter balance + amount)
     (println "done with deposit of" amount))))

(defn withdraw [balance amount]
  (dosync
   (println "Ready to withdraw..." amount " " @balance)
   (let [current-balance @balance]
     (println "simulating delay in withdraw...")
     (. Thread sleep 2000)
     (alter balance - amount)
     (println "done with withdraw of" amount))))

(def balance1 (ref 100))

(def checking-balance (ref 500))
(def savings-balance (ref 600))
(defn withdraw-account [from-balance constraining-balance amount]
  (dosync
   (let [total-balance (+ @from-balance @constraining-balance)]
     (. Thread sleep 1000)
     (if (>= (- total-balance amount) 1000)
       (alter from-balance - amount)
       (println "Sorry, can't withdraw due to constraint violation")))))

(comment

  (def balance (ref 0))
  (println "Balance is" @balance)
  (ref-set balance 100)

  (dosync
   (ref-set balance 100))
  (println "Balance is now" @balance)

  (println "Balance1 is" @balance1)

  ;;
  )
(future (deposit balance1 20))

(future (withdraw balance1 10))

(. Thread sleep 10000)
(println "Balance1 now is" @balance1)


(println "checking-balance is" @checking-balance)
(println "savings-balance is" @savings-balance)
(println "Total balance is" (+ @checking-balance @savings-balance))
(future (withdraw-account checking-balance savings-balance 100))
(future (withdraw-account savings-balance checking-balance 100))


(. Thread sleep 2000)
(println "checking-balance is" @checking-balance)
(println "savings-balance is" @savings-balance)
(println "Total balance is" (+ @checking-balance @savings-balance))