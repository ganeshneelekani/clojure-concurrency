(ns clojure-concurrency.reduce)

(defn -map [f coll]
  (reduce 
   (fn [acc v]
     (conj acc (f v)))
   []
   coll))

(defn -filter [f coll]
  (reduce 
   (fn [acc v]
     (if (f v)
       (conj acc v)
       acc))
   []
   coll))

(defn -map [f]
  (fn [rf] 
    (fn 
      ([] (rf))
      ([acc] (rf acc))
      ([acc v]
       (rf acc (f v))))))

  (defn -filter [f]
    (fn [rf]
      (fn 
        ([] (rf))
        ([acc] (rf acc))
        ([acc v]
         (if (f v)
           (rf acc v)
           acc)))))

  (def inc-xf (comp (-map inc)
                    (-filter even?)))

;;(reduce (inc-xf conj) [] [ 1 2 3 4])

(let [rf (inc-xf conj)]
      (rf (reduce rf (rf) [1 2 3 4])))



